package com.nejon.branta.feature.stats

// A card the learner keeps struggling with (low SM-2 ease factor)
data class StrugglingCard(
    val cardId: String,
    val front: String,
    val deckName: String,
    val easeFactor: Float
)

// ---------------------------------------------------------------------------
// STATS UI STATE
// - Immutable state snapshot for the Learning Statistics Dashboard.
// - Global, cross-deck insights only (per-deck progress lives on DecksScreen).
// ---------------------------------------------------------------------------
data class StatsUiState(
    // Overview
    val totalDecks: Int = 0,                    // Total active decks
    val totalCards: Int = 0,                    // Total flashcards across all decks
    val masteredCards: Int = 0,                 // Total cards mastered (repetition > 0)
    val overallMasteryPercentage: Int = 0,      // Overall mastery % (0 to 100%)

    // Card maturity (each card is in exactly one bucket)
    val newCards: Int = 0,                      // Never reviewed
    val learningCards: Int = 0,                 // Reviewed, interval < 21 days
    val matureCards: Int = 0,                   // Interval >= 21 days (long-term memory)

    // Recent activity (counts individual ratings from the review log)
    val reviewsToday: Int = 0,                  // Ratings given today
    val reviewsThisWeek: Int = 0,               // Ratings given in the last 7 days

    // Needs attention
    val strugglingCards: List<StrugglingCard> = emptyList(), // Hardest cards, lowest ease first

    val isLoading: Boolean = false              // True while calculating stats
)
