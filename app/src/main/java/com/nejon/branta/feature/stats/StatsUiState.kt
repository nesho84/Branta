package com.nejon.branta.feature.stats

// ---------------------------------------------------------------------------
// STATS UI STATE
// - Immutable state snapshot for the Stats screen.
// - Every number is for all decks together.
// ---------------------------------------------------------------------------
data class StatsUiState(
    // Collection
    val deckCount: Int = 0,                // All decks
    val noteDeckCount: Int = 0,            // How many of them are Note decks
    val qaDeckCount: Int = 0,              // How many of them are Q&A decks
    val totalCards: Int = 0,               // All cards

    // Learning
    val learnedCards: Int = 0,             // Cards learned so far (Got it at least once)
    val learnedToday: Int = 0,             // Cards learned for the first time today

    // Practice
    val gotItTotal: Int = 0,               // All "Got it" presses added together
    val skipTotal: Int = 0,                // All "Skip" presses added together

    val isLoading: Boolean = false
) {
    // Cards not learned yet
    val stillToLearn: Int
        get() = totalCards - learnedCards

    // Learned share as 0f..1f for LinearProgressIndicator
    val learnedFraction: Float
        get() = if (totalCards > 0) learnedCards.toFloat() / totalCards else 0f

    // Learned share as a whole percentage for labels
    val learnedPercentage: Int
        get() = (learnedFraction * 100).toInt()
}