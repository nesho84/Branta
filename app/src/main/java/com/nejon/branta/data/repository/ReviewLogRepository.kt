package com.nejon.branta.data.repository

import com.nejon.branta.data.model.ReviewLog
import kotlinx.coroutines.flow.Flow

// ---------------------------------------------------------------------------
// REVIEW LOG REPOSITORY CONTRACT
// - Abstract interface for recording and observing review history.
// ---------------------------------------------------------------------------
interface ReviewLogRepository {
    fun getAllReviewLogs(): Flow<List<ReviewLog>> // Stream of every review ever logged
    suspend fun insertReviewLog(log: ReviewLog)   // Async append one review entry
}
