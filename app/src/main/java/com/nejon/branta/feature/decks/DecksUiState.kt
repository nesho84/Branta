package com.nejon.branta.feature.decks

import com.nejon.branta.data.model.Deck

// Per-deck progress numbers shown on each deck card
data class DeckProgress(
    val totalCards: Int = 0,    // Total cards in this deck
    val learnedCards: Int = 0   // Cards that are learned (Got it at least once)
) {
    // Learned share as 0f...1f for LinearProgressIndicator
    val fraction: Float
        get() = if (totalCards > 0) learnedCards.toFloat() / totalCards else 0f

    // Learned share as a whole percentage for labels
    val percentage: Int
        get() = (fraction * 100).toInt()
}

// ---------------------------------------------------------------------------
// DECKS UI STATE
// - Immutable data class representing the complete UI state for DecksScreen.
// - Includes deckProgress map (deckId -> total + learned cards).
// ---------------------------------------------------------------------------
data class DecksUiState(
    val decks: List<Deck> = emptyList(),                      // List of decks to display on screen
    val deckProgress: Map<String, DeckProgress> = emptyMap(), // Map of deckId -> progress
    val isLoading: Boolean = false                            // True while loading initial data
)
