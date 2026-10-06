package com.nejon.branta.feature.decks

import com.nejon.branta.data.model.Deck

// ---------------------------------------------------------------------------
// DECKS UI STATE
// - Immutable data class representing the complete UI state for DecksScreen.
// - Acts as the Single Source of Truth for the screen.
// - Properties use 'val' (immutable) so Jetpack Compose reliably detects state
//   updates when a new copy is emitted.
// ---------------------------------------------------------------------------
data class DecksUiState(
    val decks: List<Deck> = emptyList(), // List of decks to display on screen
    val isLoading: Boolean = false       // True while loading initial data
)