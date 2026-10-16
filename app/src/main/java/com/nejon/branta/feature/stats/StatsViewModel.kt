package com.nejon.branta.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nejon.branta.data.model.DeckType
import com.nejon.branta.data.repository.CardRepository
import com.nejon.branta.data.repository.DeckRepository
import com.nejon.branta.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId

// ---------------------------------------------------------------------------
// STATS VIEW MODEL
// - Calculates collection, learning, and practice numbers from all decks and cards.
// ---------------------------------------------------------------------------
class StatsViewModel(
    private val deckRepository: DeckRepository = AppContainer.deckRepository,
    private val cardRepository: CardRepository = AppContainer.cardRepository
) : ViewModel() {

    // Recalculates the stats whenever decks or cards change
    val uiState: StateFlow<StatsUiState> = combine(
        deckRepository.getDecks(),
        cardRepository.getAllCards()
    ) { decks, cards ->
        // Start of today in the phone's time zone, so "today" matches the user's clock
        val zone = ZoneId.systemDefault()
        val startOfToday = LocalDate.now(zone).atStartOfDay(zone).toInstant()

        StatsUiState(
            deckCount = decks.size,
            noteDeckCount = decks.count { it.type == DeckType.NOTE },
            qaDeckCount = decks.count { it.type == DeckType.QUESTION_ANSWER },
            totalCards = cards.size,
            learnedCards = cards.count { it.isLearned },
            learnedToday = cards.count { card ->
                val learnedAt = card.learnedAt
                learnedAt != null && learnedAt >= startOfToday
            },
            gotItTotal = cards.sumOf { it.gotItCount },
            skipTotal = cards.sumOf { it.skipCount },
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(isLoading = true)
    )
}