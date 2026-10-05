package com.nejon.branta.feature.decks

import com.nejon.branta.data.model.Deck

data class DecksUiState(
    val decks: List<Deck> = emptyList(),
    val isLoading: Boolean = false
)