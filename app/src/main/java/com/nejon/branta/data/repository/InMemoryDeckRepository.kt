package com.nejon.branta.data.repository

import com.nejon.branta.data.model.Deck
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

// ---------------------------------------------------------------------------
// IN-MEMORY DECK REPOSITORY
// - Temporary reactive in-memory implementation of DeckRepository.
// - Seeded with initial sample decks for immediate testing without Room DB.
// ---------------------------------------------------------------------------
class InMemoryDeckRepository : DeckRepository {

    // MutableStateFlow holds reactive list of decks in memory
    private val _decks = MutableStateFlow(
        listOf(
            Deck(
                id = "1",
                name = "Kotlin Basics",
                description = "Core concepts of Kotlin language"
            ),
            Deck(
                id = "2",
                name = "Android Jetpack Compose",
                description = "Declarative UI toolkit"
            )
        )
    )

    // Returns read-only StateFlow as Flow
    override fun getDecks(): Flow<List<Deck>> {
        return _decks.asStateFlow()
    }

    // Maps decks stream to emit single deck matching ID
    override fun getDeckById(id: String): Flow<Deck?> {
        return _decks.map { decks -> decks.find { it.id == id } }
    }

    // Atomically appends new deck
    override suspend fun insertDeck(deck: Deck) {
        _decks.update { current -> current + deck }
    }

    // Replaces matching deck instance
    override suspend fun updateDeck(deck: Deck) {
        _decks.update { current ->
            current.map { if (it.id == deck.id) deck else it }
        }
    }

    // Removes deck matching ID
    override suspend fun deleteDeck(id: String) {
        _decks.update { current -> current.filterNot { it.id == id } }
    }
}
