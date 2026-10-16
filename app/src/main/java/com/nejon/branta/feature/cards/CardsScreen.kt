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
import com.nejon.branta.data.model.DeckType

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
        onCreateCard = { text, answer -> viewModel.createCard(text, answer) },
        onUpdateCard = { cardId, text, answer -> viewModel.updateCard(cardId, text, answer) },
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
    onUpdateCard: (String, String, String) -> Unit = { _, _, _ -> },
    onDeleteCard: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    // Every card in this deck follows the deck's type
    val deckType = uiState.deck?.type ?: DeckType.QUESTION_ANSWER

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
                                deckType = deckType,
                                onEdit = { cardToEdit = card },
                                onDelete = { cardToDelete = card }
                            )
                        }
                    }
                }
            }
        }
    }

    // 1. Create Card Dialog (empty form)
    if (showCreateDialog) {
        CardDialog(
            title = "New Card",
            confirmLabel = "Create",
            deckType = deckType,
            onDismiss = { showCreateDialog = false },
            onConfirm = { text, answer ->
                onCreateCard(text, answer)
                showCreateDialog = false
            }
        )
    }

    // 2. Edit Card Dialog (same form, pre-filled with the card's values)
    cardToEdit?.let { card ->
        CardDialog(
            title = "Edit Card",
            confirmLabel = "Save",
            deckType = deckType,
            initialText = card.text,
            initialAnswer = card.answer ?: "",
            onDismiss = { cardToEdit = null },
            onConfirm = { text, answer ->
                onUpdateCard(card.id, text, answer)
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
//    - Individual card item: text (and answer in Q&A decks), then a footer with progress counts + Edit & Delete.
// ---------------------------------------------------------------------------
@Composable
private fun CardItem(
    card: Card,
    deckType: DeckType,
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
            // Card content depends on the deck's type
            when (deckType) {
                DeckType.NOTE -> {
                    Text(
                        text = card.text,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                DeckType.QUESTION_ANSWER -> {
                    Text(
                        text = "QUESTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = card.text,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ANSWER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = card.answer.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // @TODO: content for the planned deck types (hidden from the picker for now)
                DeckType.TRUE_FALSE,
                DeckType.MULTIPLE_CHOICE,
                DeckType.TYPE_ANSWER,
                DeckType.PICTURE,
                DeckType.LISTEN -> {}
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer: learning progress on the left, Edit & Delete on the right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✅ Got it ${card.gotItCount}×  ·  ⏭ Skipped ${card.skipCount}×",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Card"
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Card",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 4. DIALOGS
// ---------------------------------------------------------------------------

// Create / Edit Card Dialog: the fields follow the deck's type
@Composable
private fun CardDialog(
    title: String,
    confirmLabel: String,
    deckType: DeckType,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    initialText: String = "",
    initialAnswer: String = ""
) {
    var text by remember { mutableStateOf(initialText) }
    var answer by remember { mutableStateOf(initialAnswer) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                when (deckType) {
                    DeckType.NOTE -> {
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            label = { Text("Text to learn") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    DeckType.QUESTION_ANSWER -> {
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            label = { Text("Question") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = answer,
                            onValueChange = { answer = it },
                            label = { Text("Answer") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // @TODO: fields for the planned deck types (hidden from the picker for now)
                    DeckType.TRUE_FALSE,
                    DeckType.MULTIPLE_CHOICE,
                    DeckType.TYPE_ANSWER,
                    DeckType.PICTURE,
                    DeckType.LISTEN -> {}
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text, answer) },
                enabled = isCardInputValid(deckType, text, answer)
            ) {
                Text(confirmLabel)
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
                deck = Deck(id = "1", name = "Kotlin Basics", description = "Core concepts", type = DeckType.QUESTION_ANSWER),
                cards = listOf(
                    Card(id = "101", deckId = "1", text = "What is 'val'?", answer = "Immutable variable", gotItCount = 3, skipCount = 1),
                    Card(id = "102", deckId = "1", text = "What is 'var'?", answer = "A variable you can change")
                )
            )
        )
    }
}
