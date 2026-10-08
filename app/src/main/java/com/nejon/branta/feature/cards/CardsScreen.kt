package com.nejon.branta.feature.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nejon.branta.data.model.Card
import com.nejon.branta.data.model.Deck

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
    // Collect reactive state from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Pass state and callbacks down to stateless CardsScreen
    CardsScreen(
        uiState = uiState,
        onCreateCard = { front, back -> viewModel.createCard(front, back) },
        onUpdateCard = { cardId, front, back -> viewModel.updateCard(cardId, front, back) },
        onDeleteCard = { cardId -> viewModel.deleteCard(cardId) },
        onBackClick = onBackClick
    )
}

// ---------------------------------------------------------------------------
// 2. MAIN SCREEN UI (Stateless)
//    - Renders TopAppBar header with Deck title, Back button, Top Right + Add button, & Cards list.
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    uiState: CardsUiState,
    onCreateCard: (String, String) -> Unit = { _, _ -> },
    onUpdateCard: (String, String, String?) -> Unit = { _, _, _ -> },
    onDeleteCard: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    // Local dialog state controls
    var showCreateDialog by remember { mutableStateOf(false) }
    var cardToEdit by remember { mutableStateOf<Card?>(null) }
    var cardToDelete by remember { mutableStateOf<Card?>(null) }

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
                },
                actions = {
                    // + Add Card Action Button in Top Right Header
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Card"
                        )
                    }
                }
            )
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
                        text = "No cards in this deck yet. Tap + at the top right to add one!",
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
                                onEdit = { cardToEdit = card },
                                onDelete = { cardToDelete = card }
                            )
                        }
                    }
                }
            }
        }
    }

    // 1. Create Card Dialog
    if (showCreateDialog) {
        CreateCardDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { front, back ->
                onCreateCard(front, back)
                showCreateDialog = false
            }
        )
    }

    // 2. Edit Card Dialog
    cardToEdit?.let { card ->
        EditCardDialog(
            card = card,
            onDismiss = { cardToEdit = null },
            onConfirm = { front, back ->
                onUpdateCard(card.id, front, back)
                cardToEdit = null
            }
        )
    }

    // 3. Delete Confirmation Dialog (Prevents accidental card deletions!)
    cardToDelete?.let { card ->
        AlertDialog(
            onDismissRequest = { cardToDelete = null },
            title = { Text("Delete Card?") },
            text = { Text("Are you sure you want to delete this card? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCard(card.id)
                        cardToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { cardToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ---------------------------------------------------------------------------
// 3. HELPER COMPONENTS
//    - Individual Flashcard item displaying Card Type Badge, Front, Back, Edit & Delete icons.
// ---------------------------------------------------------------------------
@Composable
private fun CardItem(
    card: Card,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Top Header: Card Type Pill Badge + Edit & Delete Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Card Type Pill Badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = card.cardType.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                // Action Buttons: Edit Pencil + Delete Trash
                Row {
                    // Edit Pencil Icon
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Card"
                        )
                    }
                    // Delete Trash Icon
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Card",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 1. Front Prompt / Question
            Text(
                text = "FRONT",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = card.front,
                style = MaterialTheme.typography.titleMedium
            )

            // 2. Optional Back Answer
            if (!card.back.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "BACK",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = card.back,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 4. DIALOGS
// ---------------------------------------------------------------------------

// Create Card Dialog
@Composable
private fun CreateCardDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Card") },
        text = {
            Column {
                OutlinedTextField(
                    value = front,
                    onValueChange = { front = it },
                    label = { Text("Front (Prompt / Question)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                enabled = front.isNotBlank()
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

// Edit Card Dialog
@Composable
private fun EditCardDialog(
    card: Card,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var front by remember { mutableStateOf(card.front) }
    var back by remember { mutableStateOf(card.back ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Card") },
        text = {
            Column {
                OutlinedTextField(
                    value = front,
                    onValueChange = { front = it },
                    label = { Text("Front (Prompt / Question)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                enabled = front.isNotBlank()
            ) {
                Text("Save")
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
