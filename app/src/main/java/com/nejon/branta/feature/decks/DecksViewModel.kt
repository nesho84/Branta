package com.nejon.branta.feature.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nejon.branta.data.model.Deck
import com.nejon.branta.data.repository.DeckRepository
import com.nejon.branta.data.repository.InMemoryDeckRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DecksViewModel(private val deckRepository: DeckRepository = InMemoryDeckRepository()) :
    ViewModel() {
    val uiState: StateFlow<DecksUiState> = deckRepository.getDecks()
        .map { decks -> DecksUiState(decks = decks, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DecksUiState(isLoading = true)
        )

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

    fun deleteDeck(deckId: String) {
        viewModelScope.launch {
            deckRepository.deleteDeck(deckId)
        }
    }
}