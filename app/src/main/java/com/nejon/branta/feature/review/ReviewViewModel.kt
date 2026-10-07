package com.nejon.branta.feature.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nejon.branta.data.model.CardRating
import com.nejon.branta.data.repository.CardRepository
import com.nejon.branta.data.repository.DeckRepository
import com.nejon.branta.data.repository.InMemoryCardRepository
import com.nejon.branta.data.repository.InMemoryDeckRepository
import com.nejon.branta.domain.CalculateNextReviewUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// REVIEW VIEW MODEL
// - Manages study session state, card reveal status, & card review ratings.
// - Uses CalculateNextReviewUseCase (SM-2) to update card scheduling parameters.
// ---------------------------------------------------------------------------
class ReviewViewModel(
    private val deckId: String,
    private val cardRepository: CardRepository = InMemoryCardRepository(),
    private val deckRepository: DeckRepository = InMemoryDeckRepository(),
    private val calculateNextReview: CalculateNextReviewUseCase = CalculateNextReviewUseCase()
) : ViewModel() {

    // Internal session progress state holders
    private val _currentCardIndex = MutableStateFlow(0)
    private val _isBackRevealed = MutableStateFlow(false)
    private val _reviewedCount = MutableStateFlow(0)

    // Combine repository flows and internal session state into a hot StateFlow<ReviewUiState>
    val uiState: StateFlow<ReviewUiState> = combine(
        deckRepository.getDeckById(deckId),
        cardRepository.getCardsForDeck(deckId),
        _currentCardIndex,
        _isBackRevealed,
        _reviewedCount
    ) { deck, cards, index, isRevealed, reviewedCount ->
        ReviewUiState(
            deck = deck,
            cardsToReview = cards,
            currentCardIndex = index,
            isBackRevealed = isRevealed,
            reviewedCount = reviewedCount,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReviewUiState(isLoading = true)
    )

    // Flips the current card to reveal the back answer
    fun revealAnswer() {
        _isBackRevealed.value = true
    }

    // Process user rating for the current card using SM-2 algorithm
    fun answerCard(rating: CardRating) {
        val currentCard = uiState.value.currentCard ?: return

        // 1. Calculate updated scheduling parameters (interval, easeFactor, dueDate)
        val updatedCard = calculateNextReview(currentCard, rating)

        viewModelScope.launch {
            // 2. Persist updated card to repository
            cardRepository.updateCard(updatedCard)

            // 3. Advance session to next card
            _isBackRevealed.value = false
            _currentCardIndex.update { it + 1 }
            _reviewedCount.update { it + 1 }
        }
    }

    // Resets session index to review cards again
    fun restartSession() {
        _currentCardIndex.value = 0
        _isBackRevealed.value = false
        _reviewedCount.value = 0
    }
}