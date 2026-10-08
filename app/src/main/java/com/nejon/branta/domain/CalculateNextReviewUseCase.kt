package com.nejon.branta.domain

import com.nejon.branta.data.model.Card
import com.nejon.branta.data.model.CardRating
import java.time.Instant
import kotlin.math.max
import kotlin.math.roundToInt

// ---------------------------------------------------------------------------
// CALCULATE NEXT REVIEW USE CASE (SM-2 Spaced Repetition Algorithm)
// - Core business logic for calculating card interval, ease factor, and repetition count.
// ---------------------------------------------------------------------------
class CalculateNextReviewUseCase {

    operator fun invoke(
        card: Card,
        rating: CardRating,
        now: Instant = Instant.now()
    ): Card {
        val q = rating.quality // SM-2 quality score (1=AGAIN, 3=HARD, 4=GOOD, 5=EASY)

        // 1. Calculate new Repetition count (q < 3 = failed recall)
        val newRepetition = if (q < 3) 0 else card.repetition + 1

        // 2. Calculate new Ease Factor (SM-2 formula with minimum bound of 1.3f)
        val newEaseFactor = max(
            1.3f,
            card.easeFactor + (0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f))
        )

        // 3. Calculate new Interval (in days)
        val newInterval = when {
            q < 3 -> 1 // AGAIN resets interval to 1 day
            newRepetition == 1 -> 1
            newRepetition == 2 -> 6
            else -> (card.interval * newEaseFactor).roundToInt()
        }

        // Return brand new updated Card copy
        return card.copy(
            interval = newInterval,
            repetition = newRepetition,
            easeFactor = newEaseFactor,
            lastReviewedAt = now,
            updatedAt = now
        )
    }
}
