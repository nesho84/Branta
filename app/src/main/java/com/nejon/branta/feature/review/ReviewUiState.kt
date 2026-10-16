package com.nejon.branta.feature.review

import com.nejon.branta.data.model.Card
import com.nejon.branta.data.model.Deck

// ---------------------------------------------------------------------------
// REVIEW UI STATE
// - Immutable state container for the Review Session screen.
// - Each card in the deck is shown once, in order: "Card 3 of 10".
// ---------------------------------------------------------------------------
data class ReviewUiState(
    val deck: Deck? = null,                // Deck details
    val cards: List<Card> = emptyList(),   // All cards in this session
    val currentIndex: Int = 0,             // Position of the card being shown (0 = first)
    val gotItInSession: Int = 0,           // How many times "Got it" was pressed this session
    val isAnswerShown: Boolean = false,    // True after a Q&A card is tapped to show its answer
    val isLoading: Boolean = false         // True while loading the deck's cards
) {
    // The card on screen right now (null once every card has been shown)
    val currentCard: Card?
        get() = cards.getOrNull(currentIndex)

    // Finished when the deck had cards and we've moved past the last one
    val isFinished: Boolean
        get() = !isLoading && cards.isNotEmpty() && currentIndex >= cards.size
}
