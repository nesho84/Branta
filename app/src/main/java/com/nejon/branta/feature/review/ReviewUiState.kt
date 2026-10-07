package com.nejon.branta.feature.review

import com.nejon.branta.data.model.Card
import com.nejon.branta.data.model.Deck

// ---------------------------------------------------------------------------
// REVIEW UI STATE
// - Immutable state container for the Review Session screen.
// - Tracks cards queue, current top card index, reveal status, & completion.
// ---------------------------------------------------------------------------
data class ReviewUiState(
    val deck: Deck? = null,                      // Deck details
    val cardsToReview: List<Card> = emptyList(), // Queue of cards to review
    val currentCardIndex: Int = 0,               // Index of currently visible top card
    val isBackRevealed: Boolean = false,         // True if back answer is revealed
    val reviewedCount: Int = 0,                  // Total cards reviewed in this session
    val isLoading: Boolean = false               // True while loading initial data
) {
    // Computed property: Returns currently active card at top of stack (null if empty/done)
    val currentCard: Card?
        get() = cardsToReview.getOrNull(currentCardIndex)

    // Computed property: Returns true when all cards in session have been answered
    val isSessionCompleted: Boolean
        get() = !isLoading && cardsToReview.isNotEmpty() && currentCardIndex >= cardsToReview.size
}