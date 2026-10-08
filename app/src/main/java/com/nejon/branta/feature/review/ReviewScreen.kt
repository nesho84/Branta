package com.nejon.branta.feature.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nejon.branta.data.model.Card
import com.nejon.branta.data.model.CardRating
import com.nejon.branta.data.model.Deck

// ---------------------------------------------------------------------------
// 1. ROUTE (Stateful Entry Point)
//    - Receives deckId and back navigation callback.
//    - Collects reactive ReviewUiState from ReviewViewModel.
// ---------------------------------------------------------------------------
@Composable
fun ReviewScreenRoute(
    deckId: String,
    onBackClick: () -> Unit,
    viewModel: ReviewViewModel = viewModel(key = deckId) { ReviewViewModel(deckId) }
) {
    // Collect reactive state from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ReviewScreen(
        uiState = uiState,
        onRevealAnswer = { viewModel.revealAnswer() },
        onAnswerCard = { rating -> viewModel.answerCard(rating) },
        onRestartSession = { viewModel.restartSession() },
        onBackClick = onBackClick
    )
}

// ---------------------------------------------------------------------------
// 2. MAIN SCREEN UI (Stateless)
//    - Clear 2-Phase Review Flow: Question Phase -> Answer Phase.
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    uiState: ReviewUiState,
    onRevealAnswer: () -> Unit = {},
    onAnswerCard: (CardRating) -> Unit = {},
    onRestartSession: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = uiState.deck?.name?.let { "Review: $it" } ?: "Review") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Back"
                        )
                    }
                }
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

                // State 2: No cards available to review in this deck
                uiState.totalCardsInSession == 0 -> {
                    Text(
                        text = "No cards available to review in this deck!",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // State 3: Session Completed! Show celebratory summary screen
                uiState.isSessionCompleted -> {
                    SessionCompletedContent(
                        reviewedCount = uiState.reviewedCount,
                        onRestart = onRestartSession,
                        onBack = onBackClick,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // State 4: Active Reviewing - Question / Answer Phase
                uiState.currentCard != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Progress Counter (e.g. "Remaining: 3 cards")
                        Text(
                            text = "Remaining: ${uiState.activeCards.size} cards (Reviewed ${uiState.reviewedCount})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Flashcard UI Component (Swiping enabled once answer is revealed)
                        FlashcardReviewItem(
                            card = uiState.currentCard!!,
                            isRevealed = uiState.isBackRevealed,
                            onReveal = onRevealAnswer,
                            onSwipeRight = { onAnswerCard(CardRating.GOOD) },
                            onSwipeLeft = { onAnswerCard(CardRating.AGAIN) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Phase 1: Answer NOT revealed yet -> Show "Show Answer" button
                        if (!uiState.isBackRevealed) {
                            Button(
                                onClick = onRevealAnswer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Text("Tap to Show Answer", style = MaterialTheme.typography.titleMedium)
                            }
                        } else {
                            // Phase 2: Answer IS revealed -> Clear kid-friendly choice buttons!
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // ❌ Left Button: Try Again
                                Button(
                                    onClick = { onAnswerCard(CardRating.AGAIN) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                ) {
                                    Text("❌ Try Again", style = MaterialTheme.typography.titleMedium)
                                }

                                // ✅ Right Button: Got It!
                                Button(
                                    onClick = { onAnswerCard(CardRating.GOOD) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                ) {
                                    Text("✅ Got It!", style = MaterialTheme.typography.titleMedium)
                                }
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
//    - Flashcard Review Card & Celebratory Session Completed Summary.
// ---------------------------------------------------------------------------

// Flashcard Component - Swiping left/right activates once answer is revealed
@Composable
private fun FlashcardReviewItem(
    card: Card,
    isRevealed: Boolean,
    onReveal: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Tracks horizontal drag distance in pixels
    var offsetX by remember { mutableFloatStateOf(0f) }

    Card(
        modifier = modifier
            .graphicsLayer {
                // Move card horizontally with drag and tilt angle proportionately
                translationX = offsetX
                rotationZ = offsetX / 25f // Tinder tilt effect
            }
            .pointerInput(isRevealed) {
                // Swiping is active only when answer is revealed
                if (isRevealed) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                // Swiped Right (>250px) -> Got It!
                                offsetX > 250f -> {
                                    onSwipeRight()
                                    offsetX = 0f
                                }
                                // Swiped Left (<-250px) -> Try Again
                                offsetX < -250f -> {
                                    onSwipeLeft()
                                    offsetX = 0f
                                }
                                else -> offsetX = 0f
                            }
                        },
                        onDragCancel = { offsetX = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX += dragAmount
                        }
                    )
                }
            }
            .clickable(onClick = if (!isRevealed) onReveal else { {} }), // Tap card to reveal answer
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Front Prompt Section
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "QUESTION",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = card.front,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            // Phase 1 vs Phase 2 UI
            if (!isRevealed) {
                // Phase 1 Hint
                Text(
                    text = "💡 Tap anywhere on card to show answer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            } else {
                // Phase 2: Back Answer Section
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "ANSWER",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = card.back ?: "(Read & Repeat)",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                // Phase 2 Swipe Instruction Badge
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "👈 Swipe Left = Try Again  |  👉 Swipe Right = Got It!",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

// Celebratory Session Completed Summary Screen
@Composable
private fun SessionCompletedContent(
    reviewedCount: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🎉 Awesome Job!",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "You reviewed $reviewedCount cards in this study session!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Practice Again ⭐")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Decks")
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 5. PREVIEW
//    - Visual preview of active review card in Android Studio.
// ---------------------------------------------------------------------------
@Preview(showBackground = true)
@Composable
private fun ReviewScreenPreview() {
    MaterialTheme {
        ReviewScreen(
            uiState = ReviewUiState(
                deck = Deck(id = "1", name = "Kotlin Basics"),
                activeCards = listOf(
                    Card(id = "101", deckId = "1", front = "What is 'val'?", back = "Immutable variable")
                ),
                totalCardsInSession = 1,
                reviewedCount = 0,
                isBackRevealed = true
            )
        )
    }
}
