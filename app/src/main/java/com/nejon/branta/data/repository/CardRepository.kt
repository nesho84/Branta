package com.nejon.branta.data.repository

import com.nejon.branta.data.model.Card
import kotlinx.coroutines.flow.Flow

interface CardRepository {
    fun getCardsForDeck(deckId: String): Flow<List<Card>>
    fun getCardById(id: String): Flow<Card?>
    suspend fun insertCard(card: Card)
    suspend fun updateCard(card: Card)
    suspend fun deleteCard(id: String)
    suspend fun deleteCardsForDeck(deckId: String)
}