package com.nejon.branta.data.model

import java.time.Instant
import java.util.UUID

data class Deck(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String="",
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)