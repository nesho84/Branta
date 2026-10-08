package com.nejon.branta.feature.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nejon.branta.data.model.Card
import com.nejon.branta.data.repository.CardRepository
import com.nejon.branta.data.repository.DeckRepository
import com.nejon.branta.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// CARDS VIEW MODEL
// - Manages UI state for viewing, adding, editing, and deleting flashcards in a deck.
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

    // Inserts a new flashcard into the current deck
    fun createCard(front: String, back: String?) {
        if (front.isBlank()) return
        viewModelScope.launch {
            cardRepository.insertCard(
                Card(
                    deckId = deckId,
                    front = front.trim(),
                    back = back?.trim()?.ifBlank { null }
                )
            )
        }
    }

    // Updates an existing flashcard's front and back text
    fun updateCard(cardId: String, front: String, back: String?) {
        if (front.isBlank()) return
        val currentCard = uiState.value.cards.find { it.id == cardId } ?: return
        viewModelScope.launch {
            cardRepository.updateCard(
                currentCard.copy(
                    front = front.trim(),
                    back = back?.trim()?.ifBlank { null }
                )
            )
        }
    }

    // Deletes a flashcard by ID
    fun deleteCard(cardId: String) {
        viewModelScope.launch {
            cardRepository.deleteCard(cardId)
        }
    }
}
