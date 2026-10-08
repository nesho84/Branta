package com.nejon.branta.data.model

import java.time.Instant
import java.util.UUID

// ---------------------------------------------------------------------------
// REVIEW LOG DOMAIN MODEL
// - One immutable entry per rating given during a review session.
// - Card only stores its LAST review time; this log keeps the full history
//   (needed for "reviews today", streaks, heatmaps, etc.).
// ---------------------------------------------------------------------------
data class ReviewLog(
    val id: String = UUID.randomUUID().toString(), // Unique UUID string identifier
    val cardId: String,                            // Card that was reviewed
    val deckId: String,                            // Deck the card belonged to
    val rating: CardRating,                        // Rating the user gave
    val reviewedAt: Instant = Instant.now()        // When the rating happened
)