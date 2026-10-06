package com.nejon.branta.data.model

// ---------------------------------------------------------------------------
// CARD TYPE ENUM
// - Defines supported flashcard formats in the application.
// ---------------------------------------------------------------------------
enum class CardType {
    BASIC,    // Standard Front -> Back card
    REVERSED, // Bidirectional card (Front -> Back and Back -> Front)
    CLOZE     // Fill-in-the-blank card format
}
