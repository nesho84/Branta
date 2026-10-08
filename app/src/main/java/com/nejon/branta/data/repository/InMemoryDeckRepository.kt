package com.nejon.branta.data.repository

import com.nejon.branta.data.model.Deck
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

// ---------------------------------------------------------------------------
// IN-MEMORY DECK REPOSITORY
// - Temporary reactive in-memory storage seeded with 6 rich learning decks.
// ---------------------------------------------------------------------------
class InMemoryDeckRepository : DeckRepository {

    // MutableStateFlow holds reactive list of 6 learning decks in memory
    private val _decks = MutableStateFlow(
        listOf(
            Deck(
                id = "1",
                name = "Kotlin Fundamentals",
                description = "Core language concepts, null safety, and data types"
            ),
            Deck(
                id = "2",
                name = "Jetpack Compose UI",
                description = "Declarative UI toolkit, state, and modifiers"
            ),
            Deck(
                id = "3",
                name = "Coroutines & Flow",
                description = "Asynchronous programming and reactive streams"
            ),
            Deck(
                id = "4",
                name = "Android Architecture",
                description = "Google's Clean Architecture guide and ViewModels"
            ),
            Deck(
                id = "5",
                name = "Android Jetpack Libraries",
                description = "Room, Navigation Compose, and WorkManager"
            ),
            Deck(
                id = "6",
                name = "Material Design 3",
                description = "Dynamic colors, typography, and accessibility guidelines"
            )
        )
    )

    override fun getDecks(): Flow<List<Deck>> {
        return _decks.asStateFlow()
    }

    override fun getDeckById(id: String): Flow<Deck?> {
        return _decks.map { decks -> decks.find { it.id == id } }
    }

    override suspend fun insertDeck(deck: Deck) {
        _decks.update { current -> current + deck }
    }

    override suspend fun updateDeck(deck: Deck) {
        _decks.update { current ->
            current.map { if (it.id == deck.id) deck else it }
        }
    }

    override suspend fun deleteDeck(id: String) {
        _decks.update { current -> current.filterNot { it.id == id } }
    }
}
