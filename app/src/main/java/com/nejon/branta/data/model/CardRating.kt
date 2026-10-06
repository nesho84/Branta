package com.nejon.branta.data.model

// ---------------------------------------------------------------------------
// CARD RATING ENUM
// - User recall quality score during spaced repetition review sessions.
// - Maps each rating to an integer quality score required by SM-2 algorithm.
// ---------------------------------------------------------------------------
enum class CardRating(val quality: Int) {
    AGAIN(1), // Total blackout; failed recall (resets interval)
    HARD(2),  // Recalled with significant effort (small interval increase)
    GOOD(3),  // Standard successful recall (normal interval increase)
    EASY(4)   // Effortless recall (large interval increase)
}
