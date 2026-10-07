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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
//    - Renders TopAppBar, Tinder-Swipeable Flashcard, & SM-2 Rating buttons.
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
                uiState.cardsToReview.isEmpty() -> {
                    Text(
                        text = "No cards available to review in this deck!",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // State 3: Session Completed! Show completion summary screen
                uiState.isSessionCompleted -> {
                    SessionCompletedContent(
                        reviewedCount = uiState.reviewedCount,
                        onRestart = onRestartSession,
                        onBack = onBackClick,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // State 4: Active Reviewing - Show top card on stack + Tinder Swipe + rating controls
                uiState.currentCard != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Progress Counter (e.g. "Card 1 of 3")
                        Text(
                            text = "Card ${uiState.currentCardIndex + 1} of ${uiState.cardsToReview.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tinder-Swipeable Flashcard (Swipe Right=Knew it, Swipe Left=Didn't know)
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

                        // Bottom Controls: "Show Answer" button OR Rating buttons (AGAIN, HARD, GOOD, EASY)
                        if (!uiState.isBackRevealed) {
                            Button(
                                onClick = onRevealAnswer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Text("Show Answer", style = MaterialTheme.typography.titleMedium)
                            }
                        } else {
                            // 4 SM-2 Rating Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RatingButton(
                                    label = "Again",
                                    color = MaterialTheme.colorScheme.error,
                                    onClick = { onAnswerCard(CardRating.AGAIN) },
                                    modifier = Modifier.weight(1f)
                                )
                                RatingButton(
                                    label = "Hard",
                                    color = MaterialTheme.colorScheme.tertiary,
                                    onClick = { onAnswerCard(CardRating.HARD) },
                                    modifier = Modifier.weight(1f)
                                )
                                RatingButton(
                                    label = "Good",
                                    color = MaterialTheme.colorScheme.primary,
                                    onClick = { onAnswerCard(CardRating.GOOD) },
                                    modifier = Modifier.weight(1f)
                                )
                                RatingButton(
                                    label = "Easy",
                                    color = MaterialTheme.colorScheme.secondary,
                                    onClick = { onAnswerCard(CardRating.EASY) },
                                    modifier = Modifier.weight(1f)
                                )
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
//    - Tinder-Swipeable Flashcard, Rating Button, & Session Completed Summary.
// ---------------------------------------------------------------------------

// Tinder-style swipeable Flashcard - Translates, rotates, and triggers swipe left/right
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
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            // Swiped Right (>300px) -> I knew it (GOOD)
                            offsetX > 300f -> {
                                onSwipeRight()
                                offsetX = 0f
                            }
                            // Swiped Left (<-300px) -> I didn't know it (AGAIN)
                            offsetX < -300f -> {
                                onSwipeLeft()
                                offsetX = 0f
                            }
                            // Small drag -> Snap back to center
                            else -> offsetX = 0f
                        }
                    },
                    onDragCancel = { offsetX = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX += dragAmount
                    }
                )
            }
            .clickable(onClick = onReveal), // Tap card to reveal answer
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Front Prompt / Question Section
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "FRONT",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = card.front,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            // Back Answer Section (Shown if revealed)
            if (isRevealed) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "BACK",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = card.back ?: "(Read & Repeat)",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            } else {
                Text(
                    text = "👈 Swipe Left = Didn't know  |  👉 Swipe Right = Knew it\n(Tap card to reveal answer)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

// Reusable colored Rating Button (Again, Hard, Good, Easy)
@Composable
private fun RatingButton(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = modifier.height(48.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
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
                text = "🎉 Session Completed!",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Great job! You reviewed $reviewedCount cards in this session.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Review Again")
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
                cardsToReview = listOf(
                    Card(id = "101", deckId = "1", front = "What is 'val'?", back = "Immutable variable")
                ),
                currentCardIndex = 0,
                isBackRevealed = true
            )
        )
    }
}