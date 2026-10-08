package com.nejon.branta.feature.review

import com.nejon.branta.data.model.Card
import com.nejon.branta.data.model.Deck

// ---------------------------------------------------------------------------
// REVIEW UI STATE
// - Immutable state container for the Review Session screen.
// - Tracks active queue of cards remaining in session & session progress.
// ---------------------------------------------------------------------------
data class ReviewUiState(
    val deck: Deck? = null,                       // Deck details
    val activeCards: List<Card> = emptyList(),    // Active queue of remaining cards in session
    val totalCardsInSession: Int = 0,             // Total cards initially in session
    val reviewedCount: Int = 0,                   // Total cards reviewed so far
    val isBackRevealed: Boolean = false,          // True if back answer is revealed
    val isLoading: Boolean = false                // True while loading initial data
) {
    // Current top card at front of active queue (null if empty or completed)
    val currentCard: Card?
        get() = activeCards.firstOrNull()

    // Session is completed when loading is false, initial queue was not empty, but activeCards is now empty
    val isSessionCompleted: Boolean
        get() = !isLoading && totalCardsInSession > 0 && activeCards.isEmpty()
}
