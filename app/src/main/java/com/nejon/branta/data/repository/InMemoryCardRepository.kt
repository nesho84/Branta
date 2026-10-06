package com.nejon.branta.data.repository

import com.nejon.branta.data.model.Card
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

// ---------------------------------------------------------------------------
// IN-MEMORY CARD REPOSITORY
// - Temporary reactive in-memory implementation of CardRepository.
// - Seeded with sample cards belonging to sample decks.
// ---------------------------------------------------------------------------
class InMemoryCardRepository : CardRepository {

    // MutableStateFlow holds reactive list of cards in memory
    private val _cards = MutableStateFlow(
        listOf(
            Card(
                id = "101",
                deckId = "1",
                front = "What is 'val' in Kotlin?",
                back = "A read-only (immutable) variable declaration."
            ),
            Card(
                id = "102",
                deckId = "1",
                front = "What is a data class?",
                back = "A class that holds data and auto-generates equals, hashCode, toString, and copy methods."
            ),
            Card(
                id = "201",
                deckId = "2",
                front = "What is @Composable?",
                back = "An annotation marking a function as a declarative UI component."
            )
        )
    )

    // Filters cards stream to emit only cards belonging to deckId
    override fun getCardsForDeck(deckId: String): Flow<List<Card>> {
        return _cards.map { cards -> cards.filter { it.deckId == deckId } }
    }

    // Maps cards stream to emit single card matching ID
    override fun getCardById(id: String): Flow<Card?> {
        return _cards.map { cards -> cards.find { it.id == id } }
    }

    // Atomically appends new card
    override suspend fun insertCard(card: Card) {
        _cards.update { current -> current + card }
    }

    // Replaces matching card instance
    override suspend fun updateCard(card: Card) {
        _cards.update { current ->
            current.map { if (it.id == card.id) card else it }
        }
    }

    // Removes card matching ID
    override suspend fun deleteCard(id: String) {
        _cards.update { current -> current.filterNot { it.id == id } }
    }

    // Removes all cards belonging to deckId
    override suspend fun deleteCardsForDeck(deckId: String) {
        _cards.update { current -> current.filterNot { it.deckId == deckId } }
    }
}
