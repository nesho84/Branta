package com.nejon.branta.domain

import com.nejon.branta.data.model.Card
import com.nejon.branta.data.model.CardRating
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.roundToInt

// ---------------------------------------------------------------------------
// CALCULATE NEXT REVIEW USE CASE (SM-2 Spaced Repetition Algorithm)
// - Core business logic for calculating card interval, ease factor,
//   repetition count, and due date based on user recall rating.
// ---------------------------------------------------------------------------
class CalculateNextReviewUseCase {

    // 'operator fun invoke' allows calling class instance like a function: useCase(card, rating)
    operator fun invoke(
        card: Card,
        rating: CardRating,
        now: Instant = Instant.now()
    ): Card {
        val q = rating.quality // Quality score (1=AGAIN, 2=HARD, 3=GOOD, 4=EASY)

        // 1. Calculate new Repetition count
        val newRepetition = if (q < 2) 0 else card.repetition + 1

        // 2. Calculate new Ease Factor (SM-2 formula with minimum bound of 1.3f)
        val newEaseFactor = max(
            1.3f,
            card.easeFactor + (0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f))
        )

        // 3. Calculate new Interval (in days)
        val newInterval = when {
            q < 2 -> 1 // AGAIN resets interval to 1 day
            newRepetition == 1 -> 1
            newRepetition == 2 -> 6
            else -> (card.interval * newEaseFactor).roundToInt()
        }

        // 4. Calculate new Due Date (now + newInterval days)
        val newDueDate = now.plus(newInterval.toLong(), ChronoUnit.DAYS)

        // Return brand new updated Card copy
        return card.copy(
            interval = newInterval,
            repetition = newRepetition,
            easeFactor = newEaseFactor,
            dueDate = newDueDate,
            lastReviewedAt = now,
            updatedAt = now
        )
    }
}