package com.nejon.branta.data.repository

import com.nejon.branta.data.model.Deck
import kotlinx.coroutines.flow.Flow

interface DeckRepository {
    fun getDecks(): Flow<List<Deck>>
    fun getDeckById(id: String): Flow<Deck?>
    suspend fun insertDeck(deck: Deck)
    suspend fun updateDeck(deck: Deck)
    suspend fun deleteDeck(id: String)
}