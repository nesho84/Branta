package com.nejon.branta.di

import com.nejon.branta.data.repository.CardRepository
import com.nejon.branta.data.repository.DeckRepository
import com.nejon.branta.data.repository.InMemoryCardRepository
import com.nejon.branta.data.repository.InMemoryDeckRepository

// ---------------------------------------------------------------------------
// APP CONTAINER (Manual Dependency Injection)
// - Holds ONE shared instance of each repository for the whole app, so every
//   ViewModel reads and writes the same data.
// - Swap the InMemory* implementations for Room-backed ones here later
//   (or replace this object with Hilt modules).
// ---------------------------------------------------------------------------
object AppContainer {
    val deckRepository: DeckRepository by lazy { InMemoryDeckRepository() }
    val cardRepository: CardRepository by lazy { InMemoryCardRepository() }
}
