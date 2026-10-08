package com.nejon.branta.data.repository

import com.nejon.branta.data.model.Card
import kotlinx.coroutines.flow.Flow

// ---------------------------------------------------------------------------
// CARD REPOSITORY CONTRACT
// - Abstract interface defining flashcard CRUD operations and reactive queries.
// ---------------------------------------------------------------------------
interface CardRepository {
    fun getAllCards(): Flow<List<Card>>                  // Stream of all cards across all decks
    fun getCardsForDeck(deckId: String): Flow<List<Card>> // Stream of cards for a deck
    fun getCardById(id: String): Flow<Card?>              // Stream emitting matching card or null
    suspend fun insertCard(card: Card)                    // Async insert card
    suspend fun updateCard(card: Card)                    // Async update card
    suspend fun deleteCard(id: String)                    // Async delete card by ID
    suspend fun deleteCardsForDeck(deckId: String)        // Async delete all cards in a deck
}
