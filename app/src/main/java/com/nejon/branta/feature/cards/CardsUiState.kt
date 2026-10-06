package com.nejon.branta.feature.cards

import com.nejon.branta.data.model.Card
import com.nejon.branta.data.model.Deck

// ---------------------------------------------------------------------------
// CARDS UI STATE
// - Immutable data class holding the entire UI state snapshot for CardsScreen.
// - Single Source of Truth for the screen.
// ---------------------------------------------------------------------------
data class CardsUiState(
    var deck: Deck? = null,              // Parent deck details (null until loaded)
    var cards: List<Card> = emptyList(), // Flashcards belonging to this deck
    var isLoading: Boolean = false       // True while loading from repository
)