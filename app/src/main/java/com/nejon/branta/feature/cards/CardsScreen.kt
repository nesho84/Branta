package com.nejon.branta.feature.cards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nejon.branta.data.model.Card
import com.nejon.branta.data.model.Deck
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// ---------------------------------------------------------------------------
// 1. ROUTE (Stateful Entry Point)
//    - Receives deckId and back navigation callback.
//    - Collects reactive CardsUiState from CardsViewModel.
// ---------------------------------------------------------------------------
@Composable
fun CardsScreenRoute(
    deckId: String,
    onBackClick: () -> Unit,
    viewModel: CardsViewModel = viewModel(key = deckId) { CardsViewModel(deckId) }
) {
    // 1. Collect reactive state from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 2. Pass state and event handlers down to stateless CardsScreen
    CardsScreen(
        uiState = uiState,
        onCreateCard = { front, back -> viewModel.createCard(front, back) },
        onDeleteCard = { cardId -> viewModel.deleteCard(cardId) },
        onBackClick = onBackClick
    )
}

// ---------------------------------------------------------------------------
// 2. MAIN SCREEN UI (Stateless)
//    - Renders TopAppBar header with Deck title, Back button, FAB, & Cards list.
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    uiState: CardsUiState,
    onCreateCard: (String, String) -> Unit = { _, _ -> },
    onDeleteCard: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    // Local boolean state tracking whether the create card dialog is open
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = uiState.deck?.name ?: "Cards") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Floating Action Button (+) in bottom right corner
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Card"
                )
            }
        }
    ) { innerPadding ->
        // Box fills full screen with safe padding from Scaffold topBar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                // State 1: Show loading spinner while fetching cards
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // State 2: Show empty text if deck has no cards
                uiState.cards.isEmpty() -> {
                    Text(
                        text = "No cards in this deck yet. Tap + to add one!",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // State 3: Render virtualized list of CardItem components
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items = uiState.cards, key = { it.id }) { card ->
                            CardItem(
                                card = card,
                                onDelete = { onDeleteCard(card.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Show CreateCardDialog popup when showCreateDialog is true
    if (showCreateDialog) {
        CreateCardDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { front, back ->
                onCreateCard(front, back)
                showCreateDialog = false // Close dialog after creating card
            }
        )
    }
}

// ---------------------------------------------------------------------------
// 3. HELPER COMPONENTS
//    - Individual Card item displaying Front prompt, Back answer, & Delete icon.
// ---------------------------------------------------------------------------
@Composable
private fun CardItem(
    card: Card,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Shadow depth
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // weight(1f) expands Column to take all remaining width, pushing Delete icon to right
            Column(modifier = Modifier.weight(1f)) {
                // 1. Front Prompt / Term
                Text(
                    text = card.front,
                    style = MaterialTheme.typography.titleMedium
                )
                // 2. Optional Back Answer (rendered only if back text is not null or blank)
                if (!card.back.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = card.back,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // 3. Delete Trash Icon Button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Card"
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 4. DIALOGS
//    - Modal form with text fields for creating a new flashcard.
// ---------------------------------------------------------------------------
@Composable
private fun CreateCardDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    // Local form state for text fields
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss, // Fired when user taps outside dialog or back button
        title = { Text("Create New Card") },
        text = {
            Column {
                // Front Prompt Text Input
                OutlinedTextField(
                    value = front,
                    onValueChange = { front = it },
                    label = { Text("Front (Prompt / Question)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Back Answer Text Input (Optional)
                OutlinedTextField(
                    value = back,
                    onValueChange = { back = it },
                    label = { Text("Back (Answer / Explanation - Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(front, back) },
                enabled = front.isNotBlank() // Disabled until user types front prompt
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ---------------------------------------------------------------------------
// 5. PREVIEW
//    - Visual preview in Android Studio with mock deck and card data.
// ---------------------------------------------------------------------------
@Preview(showBackground = true)
@Composable
private fun CardsScreenPreview() {
    MaterialTheme {
        CardsScreen(
            uiState = CardsUiState(
                deck = Deck(id = "1", name = "Kotlin Basics", description = "Core concepts"),
                cards = listOf(
                    Card(id = "101", deckId = "1", front = "What is 'val'?", back = "Immutable variable")
                )
            )
        )
    }
}