package com.nejon.branta.data.model

import java.time.Instant
import java.util.UUID

// ---------------------------------------------------------------------------
// CARD DOMAIN MODEL
// - Pure Kotlin immutable data class representing an individual card.
// - Holds its text, an optional answer (Q&A decks), and learning progress.
// ---------------------------------------------------------------------------
data class Card(
    val id: String = UUID.randomUUID().toString(), // Unique UUID string identifier
    val deckId: String,                            // Foreign key linking card to parent Deck
    val text: String,                              // The note, or the question
    val answer: String? = null,                    // The answer (only for cards in Q&A decks)
    val gotItCount: Int = 0,                       // How many times "Got it" was pressed
    val skipCount: Int = 0,                        // How many times "Skip" was pressed
    val learnedAt: Instant? = null,                // First time "Got it" was pressed (null = not learned yet)
    val createdAt: Instant = Instant.now(),        // Creation timestamp
    val updatedAt: Instant = Instant.now()         // Last update timestamp
) {
    // A card counts as learned after its first "Got it"
    val isLearned: Boolean
        get() = learnedAt != null
}
