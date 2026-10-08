package com.nejon.branta.feature.decks

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import com.nejon.branta.data.model.Deck
import androidx.compose.material3.LinearProgressIndicator

// ---------------------------------------------------------------------------
// 1. ROUTE (Stateful Entry Point)
//    - Connects to ViewModel and collects reactive UI state.
//    - Keeps the child DecksScreen 100% stateless and easy to preview/test.
// ---------------------------------------------------------------------------
@Composable
fun DecksScreenRoute(
    onDeckClick: (String) -> Unit,
    onStudyClick: (String) -> Unit,
    viewModel: DecksViewModel = viewModel()
) {
    // collectAsStateWithLifecycle() pauses stream collection when app goes to background
    // The Kotlin 'by' delegate extracts raw DecksUiState object directly out of State
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Pass data values down and event callbacks up to the ViewModel
    DecksScreen(
        uiState = uiState,
        onCreateDeck = { name, desc -> viewModel.createDeck(name, desc) },
        onUpdateDeck = { deckId, name, desc -> viewModel.updateDeck(deckId, name, desc) },
        onDeleteDeck = { deckId -> viewModel.deleteDeck(deckId) },
        onDeckClick = onDeckClick,
        onStudyClick = onStudyClick
    )
}

// ---------------------------------------------------------------------------
// 2. MAIN SCREEN UI (Stateless)
//    - Receives pure data (uiState) and callbacks.
//    - Uses Material 3 Scaffold to structure topBar, content, and FAB.
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecksScreen(
    uiState: DecksUiState,
    onCreateDeck: (String, String) -> Unit = { _, _ -> },
    onUpdateDeck: (String, String, String) -> Unit = { _, _, _ -> },
    onDeleteDeck: (String) -> Unit = {},
    onDeckClick: (String) -> Unit = {},
    onStudyClick: (String) -> Unit = {}
) {
    // Local dialog state controls
    var showCreateDialog by remember { mutableStateOf(false) }
    var deckToEdit by remember { mutableStateOf<Deck?>(null) }
    var deckToDelete by remember { mutableStateOf<Deck?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Branta Decks") },
            )
        },
        floatingActionButton = {
            // Floating Action Button (+) anchored in bottom-right corner
            FloatingActionButton(
                onClick = { showCreateDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Deck"
                )
            }
        }
    ) { innerPadding ->
        // Box overlays content and uses innerPadding from Scaffold
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Kotlin 'when' expression evaluates conditional UI rendering
            when {
                // State 1: Show loading spinner dead-center
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // State 2: Show empty text message dead-center if no decks exist
                uiState.decks.isEmpty() -> {
                    Text(
                        text = "No decks yet. Tap + to create one!",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // State 3: Render virtualized scrolling list of decks
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // key = { it.id } helps Compose track list items when deleted or reordered
                        items(items = uiState.decks, key = { it.id }) { deck ->
                            DeckItem(
                                deck = deck,
                                progress = uiState.deckProgress[deck.id] ?: DeckProgress(),
                                onClick = { onDeckClick(deck.id) },
                                onStudy = { onStudyClick(deck.id) },
                                onEdit = { deckToEdit = deck },
                                onDelete = { deckToDelete = deck }
                            )
                        }
                    }
                }
            }
        }
    }

    // 1. Create Deck Dialog
    if (showCreateDialog) {
        CreateDeckDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, desc ->
                onCreateDeck(name, desc)
                showCreateDialog = false
            }
        )
    }

    // 2. Edit Deck Dialog
    deckToEdit?.let { deck ->
        EditDeckDialog(
            deck = deck,
            onDismiss = { deckToEdit = null },
            onConfirm = { name, desc ->
                onUpdateDeck(deck.id, name, desc)
                deckToEdit = null
            }
        )
    }

    // 3. Delete Confirmation Dialog (Prevents accidental deck deletions!)
    deckToDelete?.let { deck ->
        AlertDialog(
            onDismissRequest = { deckToDelete = null },
            title = { Text("Delete '${deck.name}'?") },
            text = { Text("Are you sure you want to delete this deck? All flashcards inside it will be permanently deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteDeck(deck.id)
                        deckToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deckToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ---------------------------------------------------------------------------
// 3. HELPER COMPONENTS
//    - Individual Deck Card item displaying Deck Icon, Card Count Badge, title, description, & action buttons.
// ---------------------------------------------------------------------------
@Composable
private fun DeckItem(
    deck: Deck,
    progress: DeckProgress,
    onClick: () -> Unit,
    onStudy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // Tap card body to manage cards inside deck
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Top Header: Deck Icon + Card Count Pill Badge on Left | Edit & Delete Icons on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Deck Icon
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Default.Style,
                            contentDescription = "Deck Icon",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                    // Card Count Pill Badge (e.g. 5 Cards)
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "${progress.totalCards} Cards",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Edit Pencil + Delete Trash Icons on Top Right
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Deck"
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Deck",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Deck Title Name
            Text(
                text = deck.name,
                style = MaterialTheme.typography.titleMedium
            )

            // Optional Deck Description
            if (deck.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = deck.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Mastery progress (hidden for empty decks — a 0% bar there is just noise)
            if (progress.totalCards > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress.fraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${progress.masteredCards} of ${progress.totalCards} mastered",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${progress.percentage}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Action Row: "Study Deck" Button on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onStudy) {
                    Text("Study Deck")
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 4. DIALOGS
// ---------------------------------------------------------------------------

// Create Deck Dialog
@Composable
private fun CreateDeckDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Deck") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, description) },
                enabled = name.isNotBlank()
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

// Edit Deck Dialog
@Composable
private fun EditDeckDialog(
    deck: Deck,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(deck.name) }
    var description by remember { mutableStateOf(deck.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Deck") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, description) },
                enabled = name.isNotBlank()
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
private fun DecksScreenPreview() {
    MaterialTheme {
        DecksScreen(
            uiState = DecksUiState(
                decks = listOf(
                    Deck(
                        id = "1",
                        name = "Kotlin Basics",
                        description = "Core concepts of Kotlin language"
                    )
                ),
                deckProgress = mapOf("1" to DeckProgress(totalCards = 5, masteredCards = 3))
            )
        )
    }
}
