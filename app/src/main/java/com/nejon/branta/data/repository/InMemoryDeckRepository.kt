package com.nejon.branta.data.repository

import com.nejon.branta.data.model.Deck
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class InMemoryDeckRepository : DeckRepository {
    private val _decks = MutableStateFlow(
        listOf<Deck>(
            Deck(
                id = "1",
                name = "Kotlin Basics",
                description = "Core concepts of Kotlin language"
            ),
            Deck(id = "2", name = "Android Jetpack Compose", description = "Declarative UI toolkit")
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