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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nejon.branta.data.model.Card
import com.nejon.branta.data.model.Deck
import com.nejon.branta.data.model.DeckType

// How far (in pixels) a card must be dragged to count as a swipe
private const val SWIPE_THRESHOLD = 250f

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
        onShowAnswer = { viewModel.showAnswer() },
        onGotIt = { viewModel.gotIt() },
        onSkip = { viewModel.skip() },
        onRestart = { viewModel.restartSession() },
        onBackClick = onBackClick
    )
}

// ---------------------------------------------------------------------------
// 2. MAIN SCREEN UI (Stateless)
//    - One card at a time: "Got it" or "Skip" (buttons or swipe).
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    uiState: ReviewUiState,
    onShowAnswer: () -> Unit = {},
    onGotIt: () -> Unit = {},
    onSkip: () -> Unit = {},
    onRestart: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = uiState.deck?.name ?: "Study") },
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
            val currentCard = uiState.currentCard
            // Every card follows the deck's type (Q&A cards can be flipped, Notes can't)
            val deckType = uiState.deck?.type ?: DeckType.QUESTION_ANSWER

            when {
                // State 1: Show loading spinner
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // State 2: Deck has no cards
                uiState.cards.isEmpty() -> {
                    Text(
                        text = "No cards in this deck yet!",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // State 3: Every card has been shown
                uiState.isFinished -> {
                    SessionFinishedContent(
                        gotIt = uiState.gotItInSession,
                        total = uiState.cards.size,
                        onRestart = onRestart,
                        onBack = onBackClick,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // State 4: Studying a card
                currentCard != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Progress: "Card 3 of 10"
                        Text(
                            text = "Card ${uiState.currentIndex + 1} of ${uiState.cards.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        StudyCard(
                            card = currentCard,
                            deckType = deckType,
                            isAnswerShown = uiState.isAnswerShown,
                            onShowAnswer = onShowAnswer,
                            onSwipeRight = onGotIt,
                            onSwipeLeft = onSkip,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Swipe hint: swiping does the same as the buttons below
                        Text(
                            text = "👈 Swipe left to skip  ·  Swipe right for “Got it” 👉",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Buttons do the same as swiping
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onSkip,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                            ) {
                                Text("⏭ Skip", style = MaterialTheme.typography.titleMedium)
                            }
                            Button(
                                onClick = onGotIt,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                            ) {
                                Text("✅ Got it", style = MaterialTheme.typography.titleMedium)
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

// The card on screen: drag left/right to swipe; in a Q&A deck, tap to see the answer
@Composable
private fun StudyCard(
    card: Card,
    deckType: DeckType,
    isAnswerShown: Boolean,
    onShowAnswer: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    modifier: Modifier = Modifier
) {
    // How far the card is dragged sideways (in pixels); starts at 0 for every new card
    var offsetX by remember(card.id) { mutableFloatStateOf(0f) }

    Card(
        modifier = modifier
            .graphicsLayer {
                // Move the card with your finger and tilt it a little
                translationX = offsetX
                rotationZ = offsetX / 25f
            }
            .pointerInput(card.id) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount -> offsetX += dragAmount },
                    onDragEnd = {
                        when {
                            offsetX > SWIPE_THRESHOLD -> onSwipeRight()  // far enough right: Got it
                            offsetX < -SWIPE_THRESHOLD -> onSwipeLeft()  // far enough left: Skip
                            else -> offsetX = 0f                         // not far enough: snap back
                        }
                    },
                    onDragCancel = { offsetX = 0f }
                )
            }
            // Only a Q&A card whose answer is still hidden reacts to a tap
            .clickable(
                enabled = deckType == DeckType.QUESTION_ANSWER && !isAnswerShown,
                onClick = onShowAnswer
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Type badge: "📝 Note" or "❓ Q&A"
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = deckType.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // The note, or the question
            Text(
                text = card.text,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            // Q&A cards: hint first, answer after a tap
            if (deckType == DeckType.QUESTION_ANSWER) {
                Spacer(modifier = Modifier.height(24.dp))
                if (isAnswerShown) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = card.answer.orEmpty(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Hint: tapping the card shows the answer
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "👆 Tap the card to see the answer",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

// End of session: "You got 7 of 10 cards!"
@Composable
private fun SessionFinishedContent(
    gotIt: Int,
    total: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pick a title and message that fit how the session went
    val (title, message) = when (gotIt) {
        total -> "🌟 Perfect!" to "You got every card!"
        0 -> "🌱 Keep going!" to "You skipped every card this time. Try them again, you've got this!"
        else -> "🎉 Well done!" to "You got $gotIt of $total cards!"
    }

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
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Study again")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to decks")
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 4. PREVIEW
// ---------------------------------------------------------------------------
@Preview(showBackground = true)
@Composable
private fun ReviewScreenPreview() {
    MaterialTheme {
        ReviewScreen(
            uiState = ReviewUiState(
                deck = Deck(id = "1", name = "Kotlin Basics"),
                cards = listOf(
                    Card(id = "101", deckId = "1", text = "What is 'val'?", answer = "A read-only variable"),
                    Card(id = "102", deckId = "1", text = "What is 'var'?", answer = "A variable you can change")
                ),
                currentIndex = 0,
                isAnswerShown = false   // shows the "Tap the card" hint in the preview
            )
        )
    }
}
