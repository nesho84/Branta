package com.nejon.branta.data.model

import java.time.Instant
import java.util.UUID

// ---------------------------------------------------------------------------
// CARD DOMAIN MODEL
// - Pure Kotlin immutable data class representing an individual flashcard.
// - Holds front prompt, optional back answer, card type, and SM-2 scheduling parameters.
// ---------------------------------------------------------------------------
data class Card(
    val id: String = UUID.randomUUID().toString(), // Unique UUID string identifier
    val deckId: String,                            // Foreign key linking card to parent Deck
    val front: String,                             // Front side prompt / term
    val back: String? = null,                      // Back side answer (null if read-and-repeat card)
    val cardType: CardType = CardType.BASIC,       // Type of card (BASIC, REVERSED, or CLOZE)
    val interval: Int = 0,                         // Days until next review (SM-2)
    val repetition: Int = 0,                       // Consecutive successful review count (SM-2)
    val easeFactor: Float = 2.5f,                  // Difficulty multiplier, starting at 2.5 (SM-2)
    val dueDate: Instant = Instant.now(),          // Instant when card is due for review
    val lastReviewedAt: Instant? = null,           // Instant of last review session
    val createdAt: Instant = Instant.now(),        // Creation timestamp
    val updatedAt: Instant = Instant.now()         // Last update timestamp
)
