package com.nejon.branta.data.model

// ---------------------------------------------------------------------------
// CARD RATING ENUM
// - User recall quality score during spaced repetition review sessions.
// - Maps each rating onto the SM-2 0..5 quality scale (q >= 3 = successful recall).
//   On that scale q = 4 leaves the ease factor unchanged, q = 5 raises it, q <= 3 lowers it.
// ---------------------------------------------------------------------------
enum class CardRating(val quality: Int) {
    AGAIN(1), // Total blackout; failed recall (resets interval, ease -0.54)
    HARD(3),  // Recalled with significant effort (ease -0.14)
    GOOD(4),  // Standard successful recall (ease unchanged)
    EASY(5)   // Effortless recall (ease +0.10)
}