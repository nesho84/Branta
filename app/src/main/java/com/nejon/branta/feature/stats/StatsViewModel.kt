package com.nejon.branta.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nejon.branta.data.repository.CardRepository
import com.nejon.branta.data.repository.DeckRepository
import com.nejon.branta.data.repository.ReviewLogRepository
import com.nejon.branta.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

// ---------------------------------------------------------------------------
// STATS VIEW MODEL
// - Calculates global learning insights: mastery, card maturity, recent activity,
//   and the cards the learner is struggling with most.
// ---------------------------------------------------------------------------
class StatsViewModel(
    private val deckRepository: DeckRepository = AppContainer.deckRepository,
    private val cardRepository: CardRepository = AppContainer.cardRepository,
    private val reviewLogRepository: ReviewLogRepository = AppContainer.reviewLogRepository
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        deckRepository.getDecks(),
        cardRepository.getAllCards(),
        reviewLogRepository.getAllReviewLogs()
    ) { decks, cards, logs ->
        // 1. Overview
        val totalCards = cards.size
        val masteredCards = cards.count { it.repetition > 0 }
        val overallMasteryPercentage = if (totalCards > 0) (masteredCards * 100) / totalCards else 0

        // 2. Maturity: New + Learning + Mature always equals totalCards
        val newCards = cards.count { it.lastReviewedAt == null }
        val matureCards = cards.count { it.interval >= MATURE_INTERVAL_DAYS }
        val learningCards = totalCards - newCards - matureCards

        // 3. Activity windows in local time, so "today" matches the user's clock
        val zone = ZoneId.systemDefault()
        val startOfToday = LocalDate.now(zone).atStartOfDay(zone).toInstant()
        val startOfWeek = startOfToday.minus(6, ChronoUnit.DAYS) // today + previous 6 days
        val reviewsToday = logs.count { !it.reviewedAt.isBefore(startOfToday) }
        val reviewsThisWeek = logs.count { !it.reviewedAt.isBefore(startOfWeek) }

        // 4. Struggling: reviewed at least once and ease dropped below threshold
        val deckNames = decks.associate { it.id to it.name }
        val strugglingCards = cards
            .filter { it.lastReviewedAt != null && it.easeFactor < STRUGGLING_EASE_FACTOR }
            .sortedBy { it.easeFactor }
            .take(MAX_STRUGGLING_CARDS)
            .map { card ->
                StrugglingCard(
                    cardId = card.id,
                    front = card.front,
                    deckName = deckNames[card.deckId] ?: "Unknown deck",
                    easeFactor = card.easeFactor
                )
            }

        StatsUiState(
            totalDecks = decks.size,
            totalCards = totalCards,
            masteredCards = masteredCards,
            overallMasteryPercentage = overallMasteryPercentage,
            newCards = newCards,
            learningCards = learningCards,
            matureCards = matureCards,
            reviewsToday = reviewsToday,
            reviewsThisWeek = reviewsThisWeek,
            strugglingCards = strugglingCards,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(isLoading = true)
    )

    private companion object {
        const val MATURE_INTERVAL_DAYS = 21     // Anki convention: 21+ days = long-term memory
        const val STRUGGLING_EASE_FACTOR = 2.3f // Default is 2.5; below 2.3 = repeatedly hard
        const val MAX_STRUGGLING_CARDS = 5
    }
}
