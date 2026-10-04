package com.nejon.branta.data.model

import java.time.Instant
import java.util.UUID

data class Card(
    val id: String = UUID.randomUUID().toString(),
    val deckId: String,
    val front: String,
    val back: String? = null,
    val cardType: CardType = CardType.BASIC,
    val interval: Int = 0,
    val repetition: Int = 0,
    val easeFactor: Float = 2.5f,
    val dueDate: Instant = Instant.now(),
    val lastReviewedAt: Instant? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)