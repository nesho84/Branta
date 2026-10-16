package com.nejon.branta.feature.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nejon.branta.data.model.Card
import com.nejon.branta.data.model.DeckType
import com.nejon.branta.data.repository.CardRepository
import com.nejon.branta.data.repository.DeckRepository
import com.nejon.branta.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant

// A card in a Note deck needs its text; a card in a Q&A deck needs a question and an answer.
// Used by the card dialog (to enable the Save button) and by the ViewModel (as a safety check).
fun isCardInputValid(deckType: DeckType, text: String, answer: String): Boolean =
    text.isNotBlank() && (deckType == DeckType.NOTE || answer.isNotBlank())

// ---------------------------------------------------------------------------
// CARDS VIEW MODEL
// - Manages UI state for viewing, adding, editing, and deleting cards in a deck.
// ---------------------------------------------------------------------------
class CardsViewModel(
    private val deckId: String,
    private val cardRepository: CardRepository = AppContainer.cardRepository,
    private val deckRepository: DeckRepository = AppContainer.deckRepository
) : ViewModel() {

    // Combine deck details flow and cards list flow into a hot StateFlow<CardsUiState>
    val uiState: StateFlow<CardsUiState> = combine(
        deckRepository.getDeckById(deckId),
        cardRepository.getCardsForDeck(deckId)
    ) { deck, cards ->
        CardsUiState(
            deck = deck,
            cards = cards,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CardsUiState(isLoading = true)
    )

    // Inserts a new card; its fields follow the deck's type
    fun createCard(text: String, answer: String) {
        val deckType = uiState.value.deck?.type ?: return
        if (!isCardInputValid(deckType, text, answer)) return
        viewModelScope.launch {
            cardRepository.insertCard(
                Card(
                    deckId = deckId,
                    text = text.trim(),
                    answer = answerFor(deckType, answer)
                )
            )
        }
    }

    // Updates an existing card's text and answer (learning progress is kept)
    fun updateCard(cardId: String, text: String, answer: String) {
        val deckType = uiState.value.deck?.type ?: return
        if (!isCardInputValid(deckType, text, answer)) return
        val currentCard = uiState.value.cards.find { it.id == cardId } ?: return
        viewModelScope.launch {
            cardRepository.updateCard(
                currentCard.copy(
                    text = text.trim(),
                    answer = answerFor(deckType, answer),
                    updatedAt = Instant.now()
                )
            )
        }
    }

    // Deletes a card by ID
    fun deleteCard(cardId: String) {
        viewModelScope.launch {
            cardRepository.deleteCard(cardId)
        }
    }

    // Cards in Note decks never store an answer
    private fun answerFor(deckType: DeckType, answer: String): String? =
        if (deckType == DeckType.NOTE) null else answer.trim()
}
