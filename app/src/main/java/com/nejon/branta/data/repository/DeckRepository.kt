package com.nejon.branta.data.repository

import com.nejon.branta.data.model.Deck
import kotlinx.coroutines.flow.Flow

// ---------------------------------------------------------------------------
// DECK REPOSITORY CONTRACT
// - Abstract interface defining deck CRUD operations and reactive data streams.
// ---------------------------------------------------------------------------
interface DeckRepository {
    fun getDecks(): Flow<List<Deck>>         // Stream of all decks
    fun getDeckById(id: String): Flow<Deck?> // Stream emitting matching deck or null
    suspend fun insertDeck(deck: Deck)       // Async insert/save deck
    suspend fun updateDeck(deck: Deck)       // Async update existing deck
    suspend fun deleteDeck(id: String)       // Async delete deck by ID
}
