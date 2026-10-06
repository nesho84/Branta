package com.nejon.branta.data.model

import java.time.Instant
import java.util.UUID

// ---------------------------------------------------------------------------
// DECK DOMAIN MODEL
// - Pure Kotlin immutable data class representing a flashcard deck entity.
// - Independent of database or framework libraries.
// ---------------------------------------------------------------------------
data class Deck(
    val id: String = UUID.randomUUID().toString(), // Unique UUID string identifier
    val name: String,                              // Deck title (required)
    val description: String = "",                  // Deck description (optional)
    val createdAt: Instant = Instant.now(),        // Creation timestamp (UTC)
    val updatedAt: Instant = Instant.now()         // Last update timestamp (UTC)
)
