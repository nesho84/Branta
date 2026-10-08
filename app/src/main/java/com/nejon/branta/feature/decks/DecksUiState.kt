package com.nejon.branta.feature.decks

import com.nejon.branta.data.model.Deck

// Per-deck progress numbers shown on each deck card
data class DeckProgress(
    val totalCards: Int = 0,    // Total cards in this deck
    val masteredCards: Int = 0  // Cards with repetition > 0
) {
    // Mastery as 0f...1f for LinearProgressIndicator
    val fraction: Float
        get() = if (totalCards > 0) masteredCards.toFloat() / totalCards else 0f

    // Mastery as a whole percentage for labels
    val percentage: Int
        get() = (fraction * 100).toInt()
}

// ---------------------------------------------------------------------------
// DECKS UI STATE
// - Immutable data class representing the complete UI state for DecksScreen.
// - Includes cardCounts map (deckId -> count of cards in that deck).
// ---------------------------------------------------------------------------
data class DecksUiState(
    val decks: List<Deck> = emptyList(),                      // List of decks to display on screen
    val deckProgress: Map<String, DeckProgress> = emptyMap(), // Map of deckId -> progress
    val isLoading: Boolean = false                            // True while loading initial data
)
