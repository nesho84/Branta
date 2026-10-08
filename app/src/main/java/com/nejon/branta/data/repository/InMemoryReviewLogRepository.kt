package com.nejon.branta.data.repository

import com.nejon.branta.data.model.ReviewLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ---------------------------------------------------------------------------
// IN-MEMORY REVIEW LOG REPOSITORY
// - Temporary reactive in-memory storage for review history (starts empty).
// ---------------------------------------------------------------------------
class InMemoryReviewLogRepository : ReviewLogRepository {

    // MutableStateFlow holds reactive list of review entries in memory
    private val _logs = MutableStateFlow<List<ReviewLog>>(emptyList())

    override fun getAllReviewLogs(): Flow<List<ReviewLog>> {
        return _logs.asStateFlow()
    }

    override suspend fun insertReviewLog(log: ReviewLog) {
        _logs.update { it + log }
    }
}