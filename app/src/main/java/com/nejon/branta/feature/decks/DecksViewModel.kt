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

// ---------------------------------------------------------------------------
// DECKS VIEW MODEL
// - Manages UI state and business events for DecksScreen.
// - Survives Android configuration changes (like screen rotation).
// - Exposes a reactive StateFlow<DecksUiState> observed by Compose UI.
// ---------------------------------------------------------------------------
class DecksViewModel(
    // Default constructor parameter instantiates InMemoryDeckRepository (no 'new' keyword in Kotlin)
    private val deckRepository: DeckRepository = InMemoryDeckRepository()
) : ViewModel() {

    // Transforms repository Flow<List<Deck>> into a hot StateFlow<DecksUiState>
    val uiState: StateFlow<DecksUiState> = deckRepository.getDecks()
        // Convert raw List<Deck> into a DecksUiState container
        .map { decks -> DecksUiState(decks = decks, isLoading = false) }
        // stateIn turns cold Flow into hot StateFlow cached in viewModelScope
        .stateIn(
            scope = viewModelScope, // Scope tied to ViewModel lifecycle (auto-canceled on destroy)
            started = SharingStarted.WhileSubscribed(5_000), // Grace period keeps flow alive during screen rotation
            initialValue = DecksUiState(isLoading = true) // Initial emission before repository responds
        )

    // Inserts a new deck into the repository inside a coroutine
    fun createDeck(name: String, description: String = "") {
        if (name.isBlank()) return
        // viewModelScope.launch starts a background coroutine bound to ViewModel lifecycle
        viewModelScope.launch {
            deckRepository.insertDeck(
                Deck(
                    name = name.trim(),
                    description = description.trim()
                )
            )
        }
    }

    // Removes a deck from the repository by ID
    fun deleteDeck(deckId: String) {
        // Launches background coroutine to call suspend repository function
        viewModelScope.launch {
            deckRepository.deleteDeck(deckId)
        }
    }
}