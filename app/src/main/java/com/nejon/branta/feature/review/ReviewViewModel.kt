package com.nejon.branta.feature.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nejon.branta.data.model.Card
import com.nejon.branta.data.repository.CardRepository
import com.nejon.branta.data.repository.DeckRepository
import com.nejon.branta.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

// ---------------------------------------------------------------------------
// REVIEW VIEW MODEL
// - Runs one study session: every card in the deck is shown once.
// - "Got it" (button or swipe right): counts it and marks the card learned.
// - "Skip" (button or swipe left): counts the skip and moves on.
// ---------------------------------------------------------------------------
class ReviewViewModel(
    private val deckId: String,
    private val cardRepository: CardRepository = AppContainer.cardRepository,
    private val deckRepository: DeckRepository = AppContainer.deckRepository
) : ViewModel() {

    // Session state owned by this ViewModel (the deck is added in uiState below)
    private val _session = MutableStateFlow(ReviewUiState(isLoading = true))

    init {
        loadSession()
    }

    // Combine deck details with the session state into StateFlow<ReviewUiState>
    val uiState: StateFlow<ReviewUiState> = combine(
        deckRepository.getDeckById(deckId),
        _session
    ) { deck, session ->
        session.copy(deck = deck)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReviewUiState(isLoading = true)
    )

    // Takes a one-time snapshot of the deck's cards and starts at the first one
    private fun loadSession() {
        viewModelScope.launch {
            _session.value = ReviewUiState(isLoading = true)
            val cards = cardRepository.getCardsForDeck(deckId).first()
            _session.value = ReviewUiState(cards = cards, isLoading = false)
        }
    }

    // Flips a Q&A card to show its answer
    fun showAnswer() {
        _session.update { it.copy(isAnswerShown = true) }
    }

    // "Got it": count it, mark the card learned (first time only), go to the next card
    fun gotIt() {
        val card = _session.value.currentCard ?: return
        val now = Instant.now()
        saveCard(
            card.copy(
                gotItCount = card.gotItCount + 1,
                learnedAt = card.learnedAt ?: now, // keep the first learned date
                updatedAt = now
            )
        )
        _session.update { it.copy(gotItInSession = it.gotItInSession + 1) }
        moveToNextCard()
    }

    // "Skip": count the skip, go to the next card
    fun skip() {
        val card = _session.value.currentCard ?: return
        saveCard(
            card.copy(
                skipCount = card.skipCount + 1,
                updatedAt = Instant.now()
            )
        )
        moveToNextCard()
    }

    // Starts the same deck again from the first card
    fun restartSession() {
        loadSession()
    }

    // Saves the updated card in the background
    private fun saveCard(card: Card) {
        viewModelScope.launch {
            cardRepository.updateCard(card)
        }
    }

    // Shows the next card (with its answer hidden again)
    private fun moveToNextCard() {
        _session.update { it.copy(currentIndex = it.currentIndex + 1, isAnswerShown = false) }
    }
}
