package com.nejon.branta.feature.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nejon.branta.data.model.Deck
import com.nejon.branta.data.repository.CardRepository
import com.nejon.branta.data.repository.DeckRepository
import com.nejon.branta.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// DECKS VIEW MODEL
// - Manages UI state and business events for DecksScreen.
// - Combines deck list flow and card list flow to calculate live card counts per deck.
// ---------------------------------------------------------------------------
class DecksViewModel(
    private val deckRepository: DeckRepository = AppContainer.deckRepository,
    private val cardRepository: CardRepository = AppContainer.cardRepository
) : ViewModel() {

    // Combine deck list flow and card list flow to compute live card counts per deck
    val uiState: StateFlow<DecksUiState> = combine(
        deckRepository.getDecks(),
        cardRepository.getAllCards()
    ) { decks, cards ->
        // Group cards by deck once, then derive total + mastered counts per deck
        val progress = cards.groupBy { it.deckId }.mapValues { (_, deckCards) ->
            DeckProgress(
                totalCards = deckCards.size,
                masteredCards = deckCards.count { it.repetition > 0 }
            )
        }
        DecksUiState(
            decks = decks,
            deckProgress = progress,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DecksUiState(isLoading = true)
    )

    // Inserts a new deck into the repository inside a coroutine
    fun createDeck(name: String, description: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            deckRepository.insertDeck(
                Deck(
                    name = name.trim(),
                    description = description.trim()
                )
            )
        }
    }

    // Updates an existing deck's name and description
    fun updateDeck(deckId: String, name: String, description: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            deckRepository.updateDeck(
                Deck(
                    id = deckId,
                    name = name.trim(),
                    description = description.trim()
                )
            )
        }
    }

    // Removes a deck from the repository by ID and purges its cards
    fun deleteDeck(deckId: String) {
        viewModelScope.launch {
            deckRepository.deleteDeck(deckId)
            cardRepository.deleteCardsForDeck(deckId)
        }
    }
}
