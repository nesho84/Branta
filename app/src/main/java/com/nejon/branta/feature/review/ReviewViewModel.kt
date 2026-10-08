package com.nejon.branta.feature.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nejon.branta.data.model.Card
import com.nejon.branta.data.model.CardRating
import com.nejon.branta.data.model.ReviewLog
import com.nejon.branta.data.repository.CardRepository
import com.nejon.branta.data.repository.DeckRepository
import com.nejon.branta.data.repository.ReviewLogRepository
import com.nejon.branta.di.AppContainer
import com.nejon.branta.domain.CalculateNextReviewUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

// Internal holder for atomic review session progress
private data class SessionProgress(
    val activeCards: List<Card> = emptyList(),
    val totalCards: Int = 0,
    val reviewedCount: Int = 0,
    val isRevealed: Boolean = false,
    val isLoading: Boolean = true
)

// ---------------------------------------------------------------------------
// REVIEW VIEW MODEL
// - Clean Queue-based Study Session ViewModel.
// - "Got It!" (GOOD) removes top card from active queue.
// - "Try Again" (AGAIN) moves top card to back of active queue.
// ---------------------------------------------------------------------------
class ReviewViewModel(
    private val deckId: String,
    private val cardRepository: CardRepository = AppContainer.cardRepository,
    private val deckRepository: DeckRepository = AppContainer.deckRepository,
    private val reviewLogRepository: ReviewLogRepository = AppContainer.reviewLogRepository,
    private val calculateNextReview: CalculateNextReviewUseCase = CalculateNextReviewUseCase()
) : ViewModel() {

    // Atomic session progress state
    private val _progress = MutableStateFlow(SessionProgress())

    init {
        loadSession()
    }

    // Loads clean snapshot of deck cards into session queue
    private fun loadSession() {
        viewModelScope.launch {
            _progress.update { it.copy(isLoading = true) }
            val initialCards = cardRepository.getCardsForDeck(deckId).first()
            _progress.value = SessionProgress(
                activeCards = initialCards,
                totalCards = initialCards.size,
                reviewedCount = 0,
                isRevealed = false,
                isLoading = false
            )
        }
    }

    // Combine repository deck info and session progress into StateFlow<ReviewUiState>
    val uiState: StateFlow<ReviewUiState> = combine(
        deckRepository.getDeckById(deckId),
        _progress
    ) { deck, progress ->
        ReviewUiState(
            deck = deck,
            activeCards = progress.activeCards,
            totalCardsInSession = progress.totalCards,
            reviewedCount = progress.reviewedCount,
            isBackRevealed = progress.isRevealed,
            isLoading = progress.isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReviewUiState(isLoading = true)
    )

    // Flips current top card to reveal back answer
    fun revealAnswer() {
        _progress.update { it.copy(isRevealed = true) }
    }

    // Process user rating for current top card
    fun answerCard(rating: CardRating) {
        val currentCard = uiState.value.currentCard ?: return

        // 1. Calculate updated scheduling parameters
        val updatedCard = calculateNextReview(currentCard, rating)

        viewModelScope.launch {
            // 2. Persist updated card parameters to repository
            cardRepository.updateCard(updatedCard)

            // 3. Record this rating in the review history (feeds Stats activity)
            reviewLogRepository.insertReviewLog(
                ReviewLog(
                    cardId = updatedCard.id,
                    deckId = updatedCard.deckId,
                    rating = rating,
                    reviewedAt = updatedCard.lastReviewedAt ?: Instant.now()
                )
            )

            // 4. Atomically update session progress and active card queue
            _progress.update { current ->
                val newCards = if (rating == CardRating.AGAIN) {
                    // "Try Again" / Swipe Left -> Move UPDATED card to back of queue,
                    // so the next rating builds on the AGAIN penalty instead of overwriting it
                    if (current.activeCards.isEmpty()) current.activeCards
                    else current.activeCards.drop(1) + updatedCard
                } else {
                    // "Got It!" / Swipe Right -> Remove top card from queue
                    if (current.activeCards.isEmpty()) current.activeCards
                    else current.activeCards.drop(1)
                }

                current.copy(
                    activeCards = newCards,
                    reviewedCount = current.reviewedCount + 1,
                    isRevealed = false
                )
            }
        }
    }

    // Restarts session with fresh queue snapshot
    fun restartSession() {
        loadSession()
    }
}
