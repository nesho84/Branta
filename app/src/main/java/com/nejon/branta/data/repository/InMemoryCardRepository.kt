package com.nejon.branta.data.repository

import com.nejon.branta.data.model.Card
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

// ---------------------------------------------------------------------------
// IN-MEMORY CARD REPOSITORY
// - Temporary reactive in-memory storage seeded with Android Q&A cards and Kotlin notes.
// ---------------------------------------------------------------------------
class InMemoryCardRepository : CardRepository {

    // MutableStateFlow holds reactive list of sample cards in memory
    private val _cards = MutableStateFlow(
        listOf(
            // Deck 1: Kotlin Fundamentals (4 Cards)
            Card(
                id = "101",
                deckId = "1",
                text = "What is the difference between 'val' and 'var'?",
                answer = "'val' declares a read-only (immutable) variable, while 'var' declares a mutable variable."
            ),
            Card(
                id = "102",
                deckId = "1",
                text = "How does Kotlin enforce Null Safety?",
                answer = "Types are non-null by default (e.g. String). Nullable types require a question mark (e.g. String?)."
            ),
            Card(
                id = "104",
                deckId = "1",
                text = "What is Smart Casting in Kotlin?",
                answer = "The Kotlin compiler automatically casts an object to a type after checking 'is Type' without explicit casting."
            ),
            Card(
                id = "105",
                deckId = "1",
                text = "What is an Extension Function?",
                answer = "Extension functions allow adding new methods to existing classes without modifying source code or inheriting."
            ),

            // Deck 2: Jetpack Compose UI (4 Cards)
            Card(
                id = "201",
                deckId = "2",
                text = "What is Recomposition in Jetpack Compose?",
                answer = "Recomposition is the process of re-executing @Composable functions when state changes to update the UI."
            ),
            Card(
                id = "202",
                deckId = "2",
                text = "Why does Modifier order matter?",
                answer = "Modifiers are evaluated sequentially from top to bottom as a chain of wrapper operations."
            ),
            Card(
                id = "204",
                deckId = "2",
                text = "What is the difference between LazyColumn and Column?",
                answer = "LazyColumn virtualizes items and only renders what is currently visible on screen (like FlatList)."
            ),
            Card(
                id = "205",
                deckId = "2",
                text = "What is State Hoisting in Compose?",
                answer = "State hoisting is a pattern of moving state to a caller to make a component stateless and reusable."
            ),

            // Deck 3: Coroutines & Flow (5 Cards)
            Card(
                id = "301",
                deckId = "3",
                text = "What is a 'suspend' function?",
                answer = "A function that can pause execution without blocking the underlying thread, called only from a coroutine."
            ),
            Card(
                id = "302",
                deckId = "3",
                text = "What happens to coroutines in 'viewModelScope' on screen destroy?",
                answer = "All active coroutines launched in viewModelScope are automatically canceled to prevent memory leaks."
            ),
            Card(
                id = "303",
                deckId = "3",
                text = "What is the difference between Flow and StateFlow?",
                answer = "Flow is a cold stream without memory state. StateFlow is a hot stream that always holds the latest state value."
            ),
            Card(
                id = "304",
                deckId = "3",
                text = "What does the 'combine' operator do in Kotlin Coroutines?",
                answer = "'combine' listens to multiple flows simultaneously and emits a new merged value whenever any flow updates."
            ),
            Card(
                id = "305",
                deckId = "3",
                text = "Why use 'collectAsStateWithLifecycle' instead of 'collectAsState'?",
                answer = "It pauses stream collection when the app goes into the background, saving CPU and battery."
            ),

            // Deck 4: Android Architecture (4 Cards)
            Card(
                id = "401",
                deckId = "4",
                text = "What are the core layers of Google's Clean Architecture?",
                answer = "UI Layer (Composables + ViewModel) -> Domain Layer (Use Cases) -> Data Layer (Repositories + DAOs)."
            ),
            Card(
                id = "402",
                deckId = "4",
                text = "Why should Composables be Stateless?",
                answer = "Stateless composables receive state and callbacks, making them easy to test, preview, and reuse."
            ),
            Card(
                id = "403",
                deckId = "4",
                text = "What is Unidirectional Data Flow (UDF)?",
                answer = "State flows DOWN from ViewModel to UI, and events flow UP from UI to ViewModel."
            ),
            Card(
                id = "404",
                deckId = "4",
                text = "Why use Repositories instead of calling DAOs directly from ViewModels?",
                answer = "Repositories abstract data sources (local Room, in-memory, network) so ViewModels stay independent of storage."
            ),

            // Deck 5: Android Jetpack Libraries (4 Cards)
            Card(
                id = "501",
                deckId = "5",
                text = "What is Room in Android Jetpack?",
                answer = "Room is Google's official object mapping library providing SQLite database access with compile-time SQL verification."
            ),
            Card(
                id = "502",
                deckId = "5",
                text = "What is Hilt?",
                answer = "Hilt is Google's recommended dependency injection library built on top of Dagger for Android."
            ),
            Card(
                id = "503",
                deckId = "5",
                text = "What is NavHost in Jetpack Navigation Compose?",
                answer = "NavHost is the container that maps route string URLs (like 'cards/{deckId}') to screen composables."
            ),
            Card(
                id = "504",
                deckId = "5",
                text = "What is WorkManager used for?",
                answer = "WorkManager schedules deferrable, guaranteed background work even if the app exits or device restarts."
            ),

            // Deck 6: Material Design 3 (4 Cards)
            Card(
                id = "601",
                deckId = "6",
                text = "What is Dynamic Color in Material 3?",
                answer = "Dynamic Color extracts color palettes from the user's personal device wallpaper on Android 12+."
            ),
            Card(
                id = "602",
                deckId = "6",
                text = "What is Scaffold in Material 3 Compose?",
                answer = "Scaffold is a top-level layout container that manages TopAppBar, BottomBar, and FloatingActionButton slots."
            ),
            Card(
                id = "603",
                deckId = "6",
                text = "Why is Touch Target Size important in Accessibility?",
                answer = "Material 3 recommends minimum 48dp x 48dp touch targets so buttons are easy to tap for everyone."
            ),
            Card(
                id = "604",
                deckId = "6",
                text = "What is NavigationBar in Material 3?",
                answer = "NavigationBar renders the standard Material 3 bottom navigation bar containing top-level screen tabs."
            ),

            // Deck 7: Quick Kotlin Notes (5 Notes)
            Card(
                id = "701",
                deckId = "7",
                text = "A data class automatically gets equals(), hashCode(), toString(), and copy()."
            ),
            Card(
                id = "702",
                deckId = "7",
                text = "remember { } keeps a value when Compose redraws the screen (recomposition)."
            ),
            Card(
                id = "703",
                deckId = "7",
                text = "String? means the value can be a String or null."
            ),
            Card(
                id = "704",
                deckId = "7",
                text = "The Elvis operator ?: gives a fallback value when something is null."
            ),
            Card(
                id = "705",
                deckId = "7",
                text = "listOf() makes a read-only list; mutableListOf() makes one you can change."
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
