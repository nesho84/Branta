package com.nejon.branta.data.repository

import com.nejon.branta.data.model.Card
import com.nejon.branta.data.model.CardType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

// ---------------------------------------------------------------------------
// IN-MEMORY CARD REPOSITORY
// - Temporary reactive in-memory storage seeded with 25+ rich Android flashcards.
// ---------------------------------------------------------------------------
class InMemoryCardRepository : CardRepository {

    // MutableStateFlow holds reactive list of 25+ flashcards in memory
    private val _cards = MutableStateFlow(
        listOf(
            // Deck 1: Kotlin Fundamentals (5 Cards)
            Card(
                id = "101",
                deckId = "1",
                front = "What is the difference between 'val' and 'var'?",
                back = "'val' declares a read-only (immutable) variable, while 'var' declares a mutable variable.",
                cardType = CardType.BASIC
            ),
            Card(
                id = "102",
                deckId = "1",
                front = "How does Kotlin enforce Null Safety?",
                back = "Types are non-null by default (e.g. String). Nullable types require a question mark (e.g. String?).",
                cardType = CardType.REVERSED
            ),
            Card(
                id = "103",
                deckId = "1",
                front = "In Kotlin, a {{c1::data class}} auto-generates equals, hashCode, toString, and copy methods.",
                back = "Data Class Cloze Deletion",
                cardType = CardType.CLOZE
            ),
            Card(
                id = "104",
                deckId = "1",
                front = "What is Smart Casting in Kotlin?",
                back = "The Kotlin compiler automatically casts an object to a type after checking 'is Type' without explicit casting.",
                cardType = CardType.BASIC
            ),
            Card(
                id = "105",
                deckId = "1",
                front = "What is an Extension Function?",
                back = "Extension functions allow adding new methods to existing classes without modifying source code or inheriting.",
                cardType = CardType.BASIC
            ),

            // Deck 2: Jetpack Compose UI (5 Cards)
            Card(
                id = "201",
                deckId = "2",
                front = "What is Recomposition in Jetpack Compose?",
                back = "Recomposition is the process of re-executing @Composable functions when state changes to update the UI.",
                cardType = CardType.BASIC
            ),
            Card(
                id = "202",
                deckId = "2",
                front = "Why does Modifier order matter?",
                back = "Modifiers are evaluated sequentially from top to bottom as a chain of wrapper operations.",
                cardType = CardType.REVERSED
            ),
            Card(
                id = "203",
                deckId = "2",
                front = "The {{c1::remember}} function preserves a state value across recompositions in Compose.",
                back = "Remember Cloze Deletion",
                cardType = CardType.CLOZE
            ),
            Card(
                id = "204",
                deckId = "2",
                front = "What is the difference between LazyColumn and Column?",
                back = "LazyColumn virtualizes items and only renders what is currently visible on screen (like FlatList).",
                cardType = CardType.BASIC
            ),
            Card(
                id = "205",
                deckId = "2",
                front = "What is State Hoisting in Compose?",
                back = "State hoisting is a pattern of moving state to a caller to make a component stateless and reusable.",
                cardType = CardType.BASIC
            ),

            // Deck 3: Coroutines & Flow (5 Cards)
            Card(
                id = "301",
                deckId = "3",
                front = "What is a 'suspend' function?",
                back = "A function that can pause execution without blocking the underlying thread, called only from a coroutine.",
                cardType = CardType.BASIC
            ),
            Card(
                id = "302",
                deckId = "3",
                front = "What happens to coroutines in 'viewModelScope' on screen destroy?",
                back = "All active coroutines launched in viewModelScope are automatically canceled to prevent memory leaks.",
                cardType = CardType.BASIC
            ),
            Card(
                id = "303",
                deckId = "3",
                front = "What is the difference between Flow and StateFlow?",
                back = "Flow is a cold stream without memory state. StateFlow is a hot stream that always holds the latest state value.",
                cardType = CardType.BASIC
            ),
            Card(
                id = "304",
                deckId = "3",
                front = "What does the 'combine' operator do in Kotlin Coroutines?",
                back = "'combine' listens to multiple flows simultaneously and emits a new merged value whenever any flow updates.",
                cardType = CardType.BASIC
            ),
            Card(
                id = "305",
                deckId = "3",
                front = "Why use 'collectAsStateWithLifecycle' instead of 'collectAsState'?",
                back = "It pauses stream collection when the app goes into the background, saving CPU and battery.",
                cardType = CardType.BASIC
            ),

            // Deck 4: Android Architecture (4 Cards)
            Card(
                id = "401",
                deckId = "4",
                front = "What are the core layers of Google's Clean Architecture?",
                back = "UI Layer (Composables + ViewModel) -> Domain Layer (Use Cases) -> Data Layer (Repositories + DAOs).",
                cardType = CardType.BASIC
            ),
            Card(
                id = "402",
                deckId = "4",
                front = "Why should Composables be Stateless?",
                back = "Stateless composables receive state and callbacks, making them easy to test, preview, and reuse.",
                cardType = CardType.BASIC
            ),
            Card(
                id = "403",
                deckId = "4",
                front = "What is Unidirectional Data Flow (UDF)?",
                back = "State flows DOWN from ViewModel to UI, and events flow UP from UI to ViewModel.",
                cardType = CardType.BASIC
            ),
            Card(
                id = "404",
                deckId = "4",
                front = "Why use Repositories instead of calling DAOs directly from ViewModels?",
                back = "Repositories abstract data sources (local Room, in-memory, network) so ViewModels stay independent of storage.",
                cardType = CardType.BASIC
            ),

            // Deck 5: Android Jetpack Libraries (4 Cards)
            Card(
                id = "501",
                deckId = "5",
                front = "What is Room in Android Jetpack?",
                back = "Room is Google's official object mapping library providing SQLite database access with compile-time SQL verification.",
                cardType = CardType.BASIC
            ),
            Card(
                id = "502",
                deckId = "5",
                front = "What is Hilt?",
                back = "Hilt is Google's recommended dependency injection library built on top of Dagger for Android.",
                cardType = CardType.BASIC
            ),
            Card(
                id = "503",
                deckId = "5",
                front = "What is NavHost in Jetpack Navigation Compose?",
                back = "NavHost is the container that maps route string URLs (like 'cards/{deckId}') to screen composables.",
                cardType = CardType.BASIC
            ),
            Card(
                id = "504",
                deckId = "5",
                front = "What is WorkManager used for?",
                back = "WorkManager schedules deferrable, guaranteed background work even if the app exits or device restarts.",
                cardType = CardType.BASIC
            ),

            // Deck 6: Material Design 3 (4 Cards)
            Card(
                id = "601",
                deckId = "6",
                front = "What is Dynamic Color in Material 3?",
                back = "Dynamic Color extracts color palettes from the user's personal device wallpaper on Android 12+.",
                cardType = CardType.BASIC
            ),
            Card(
                id = "602",
                deckId = "6",
                front = "What is Scaffold in Material 3 Compose?",
                back = "Scaffold is a top-level layout container that manages TopAppBar, BottomBar, and FloatingActionButton slots.",
                cardType = CardType.BASIC
            ),
            Card(
                id = "603",
                deckId = "6",
                front = "Why is Touch Target Size important in Accessibility?",
                back = "Material 3 recommends minimum 48dp x 48dp touch targets so buttons are easy to tap for everyone.",
                cardType = CardType.BASIC
            ),
            Card(
                id = "604",
                deckId = "6",
                front = "What is NavigationBar in Material 3?",
                back = "NavigationBar renders the standard Material 3 bottom navigation bar containing top-level screen tabs.",
                cardType = CardType.BASIC
            )
        )
    )

    override fun getAllCards(): Flow<List<Card>> {
        return _cards.asStateFlow()
    }

    override fun getCardsForDeck(deckId: String): Flow<List<Card>> {
        return _cards.map { cards -> cards.filter { it.deckId == deckId } }
    }

    override fun getCardById(id: String): Flow<Card?> {
        return _cards.map { cards -> cards.find { it.id == id } }
    }

    override suspend fun insertCard(card: Card) {
        _cards.update { current -> current + card }
    }

    override suspend fun updateCard(card: Card) {
        _cards.update { current ->
            current.map { if (it.id == card.id) card else it }
        }
    }

    override suspend fun deleteCard(id: String) {
        _cards.update { current -> current.filterNot { it.id == id } }
    }

    override suspend fun deleteCardsForDeck(deckId: String) {
        _cards.update { current -> current.filterNot { it.deckId == deckId } }
    }
}
