package com.nejon.branta.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

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
//    - Overview + Card Maturity + Recent Activity + Needs Attention.
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    uiState: StatsUiState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Learning Progress") }
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

                // State 2: Active Dashboard
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Section A: Overall Mastery Summary Card
                        item { OverallMasteryCard(uiState = uiState) }

                        // Section B: Card Maturity Breakdown (New / Learning / Mature)
                        item { MaturityCard(uiState = uiState) }

                        // Section C: Recent Review Activity
                        item { ActivityCard(uiState = uiState) }

                        // Section D: Needs Attention title
                        item {
                            Text(
                                text = "Needs Attention",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Section E: Struggling cards, or a friendly empty message
                        if (uiState.strugglingCards.isEmpty()) {
                            item {
                                Text(
                                    text = "No struggling cards. Nice work! 🎉",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(items = uiState.strugglingCards, key = { it.cardId }) { card ->
                                StrugglingCardItem(card = card)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 3. HELPER COMPONENTS
// ---------------------------------------------------------------------------

// Overall Mastery Summary Card
@Composable
private fun OverallMasteryCard(
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
                text = "📊 Overall Mastery Rate",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${uiState.overallMasteryPercentage}%",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Overall Progress Bar (0.0f to 1.0f)
            LinearProgressIndicator(
                progress = { uiState.overallMasteryPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Metric Summary Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricPill(label = "Decks", value = "${uiState.totalDecks}")
                MetricPill(label = "Total Cards", value = "${uiState.totalCards}")
                MetricPill(label = "Mastered", value = "⭐ ${uiState.masteredCards}")
            }
        }
    }
}

// One colored slice of the maturity bar
private data class MaturitySegment(
    val label: String,
    val count: Int,
    val color: Color
)

// Card Maturity: segmented bar + legend
@Composable
private fun MaturityCard(
    uiState: StatsUiState,
    modifier: Modifier = Modifier
) {
    val segments = listOf(
        MaturitySegment("New", uiState.newCards, MaterialTheme.colorScheme.outlineVariant),
        MaturitySegment("Learning", uiState.learningCards, MaterialTheme.colorScheme.tertiary),
        MaturitySegment("Mature", uiState.matureCards, MaterialTheme.colorScheme.primary)
    )

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
                text = "🌱 Card Maturity",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Segmented bar: each slice's width is proportional to its card count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // weight() must be > 0, so empty buckets are skipped
                segments.filter { it.count > 0 }.forEach { segment ->
                    Box(
                        modifier = Modifier
                            .weight(segment.count.toFloat())
                            .fillMaxHeight()
                            .background(segment.color)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                segments.forEach { segment -> LegendItem(segment = segment) }
            }
        }
    }
}

// Legend entry: colored dot + label + count
@Composable
private fun LegendItem(segment: MaturitySegment) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(segment.color)
        )
        Text(
            text = "${segment.label} ${segment.count}",
            style = MaterialTheme.typography.labelMedium
        )
    }
}

// Recent Activity: two side-by-side tiles
@Composable
private fun ActivityCard(
    uiState: StatsUiState,
    modifier: Modifier = Modifier
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
                text = "🔥 Recent Activity",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActivityTile(label = "Reviews today", count = uiState.reviewsToday, modifier = Modifier.weight(1f))
                ActivityTile(label = "Last 7 days", count = uiState.reviewsThisWeek, modifier = Modifier.weight(1f))
            }
        }
    }
}

// Single activity number tile
@Composable
private fun ActivityTile(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

// Struggling card row: front text + deck name + ease badge
@Composable
private fun StrugglingCardItem(
    card: StrugglingCard,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.front,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.deckName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Ease badge: lower = harder for you
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    text = "Ease %.1f".format(card.easeFactor),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// Small Metric Chip Helper
@Composable
private fun MetricPill(
    label: String,
    value: String
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 4. PREVIEW
// ---------------------------------------------------------------------------
@Preview(showBackground = true)
@Composable
private fun StatsScreenPreview() {
    MaterialTheme {
        StatsScreen(
            uiState = StatsUiState(
                totalDecks = 6,
                totalCards = 28,
                masteredCards = 12,
                overallMasteryPercentage = 42,
                newCards = 14,
                learningCards = 10,
                matureCards = 4,
                reviewsToday = 7,
                reviewsThisWeek = 19,
                strugglingCards = listOf(
                    StrugglingCard("1", "What is Smart Casting in Kotlin?", "Kotlin Fundamentals", 1.7f),
                    StrugglingCard("2", "Explain remember vs rememberSaveable", "Jetpack Compose UI", 2.1f)
                )
            )
        )
    }
}
