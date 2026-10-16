package com.nejon.branta.feature.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nejon.branta.data.model.DeckType

// ---------------------------------------------------------------------------
// 1. ROUTE (Stateful Entry Point)
//    - Collects reactive StatsUiState from StatsViewModel.
// ---------------------------------------------------------------------------
@Composable
fun StatsScreenRoute(
    viewModel: StatsViewModel = viewModel()
) {
    // Collect reactive state from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatsScreen(uiState = uiState)
}

// ---------------------------------------------------------------------------
// 2. MAIN SCREEN UI (Stateless)
//    - Learned, Today/Still to learn, Collection, and Practice cards.
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    uiState: StatsUiState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Progress") }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                // State 1: Show loading spinner
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // State 2: No decks yet
                uiState.deckCount == 0 -> {
                    Text(
                        text = "Create a deck to see your progress!",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // State 3: Progress dashboard (scrolls on small screens)
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LearnedCard(uiState = uiState)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatTile(
                                label = "Learned today",
                                value = uiState.learnedToday,
                                modifier = Modifier.weight(1f)
                            )
                            StatTile(
                                label = "Still to learn",
                                value = uiState.stillToLearn,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        CollectionCard(uiState = uiState)

                        PracticeCard(uiState = uiState)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 3. HELPER COMPONENTS
// ---------------------------------------------------------------------------

// Big card: "12 of 28 cards" + percentage + progress bar
@Composable
private fun LearnedCard(
    uiState: StatsUiState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "📚 Learned",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${uiState.learnedCards} of ${plural(uiState.totalCards, "card", "cards")}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${uiState.learnedPercentage}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { uiState.learnedFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Small tile: a big number with a label underneath
@Composable
private fun StatTile(
    label: String,
    value: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$value",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

// Collection: how many decks and cards, and how many decks of each type
@Composable
private fun CollectionCard(
    uiState: StatsUiState,
    modifier: Modifier = Modifier
) {
    StatsSectionCard(title = "🗂 Collection", modifier = modifier) {
        Text(
            text = "${plural(uiState.deckCount, "deck", "decks")}  ·  ${plural(uiState.totalCards, "card", "cards")}",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${DeckType.NOTE.label}: ${plural(uiState.noteDeckCount, "deck", "decks")}  ·  " +
                "${DeckType.QUESTION_ANSWER.label}: ${plural(uiState.qaDeckCount, "deck", "decks")}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Practice: all "Got it" and "Skip" presses added together
@Composable
private fun PracticeCard(
    uiState: StatsUiState,
    modifier: Modifier = Modifier
) {
    StatsSectionCard(title = "🎯 Practice", modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "✅ ${uiState.gotItTotal} Got it",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "⏭ ${uiState.skipTotal} Skipped",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

// Shared look for the Collection and Practice cards: a title and some content
@Composable
private fun StatsSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

// "1 deck" / "3 decks"
private fun plural(count: Int, one: String, many: String): String =
    if (count == 1) "$count $one" else "$count $many"

// ---------------------------------------------------------------------------
// 4. PREVIEW
// ---------------------------------------------------------------------------
@Preview(showBackground = true)
@Composable
private fun StatsScreenPreview() {
    MaterialTheme {
        StatsScreen(
            uiState = StatsUiState(
                deckCount = 7,
                noteDeckCount = 1,
                qaDeckCount = 6,
                totalCards = 30,
                learnedCards = 12,
                learnedToday = 3,
                gotItTotal = 34,
                skipTotal = 12
            )
        )
    }
}