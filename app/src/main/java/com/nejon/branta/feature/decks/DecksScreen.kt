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
import androidx.compose.material3.AlertDialog
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
    onDeleteDeck: (String) -> Unit = {},
    onDeckClick: (String) -> Unit = {},
    onStudyClick: (String) -> Unit = {}
) {
    // remember: Preserves variable across UI recompositions (re-renders)
    // mutableStateOf: Creates reactive Compose state
    // by: Kotlin delegate allowing direct boolean assignment (showCreateDialog = true)
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Branta Decks") }
            )
        },
        floatingActionButton = {
            // Floating Action Button (+) anchored in bottom-right corner
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Deck"
                )
            }
        }
    ) { innerPadding ->
        // Box overlays content and uses innerPadding so topBar doesn't obscure list
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
                        contentPadding = PaddingValues(16.dp), // Padding around outer list
                        verticalArrangement = Arrangement.spacedBy(12.dp), // 12dp gap between cards
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // key = { it.id } helps Compose track list items when deleted or reordered
                        items(items = uiState.decks, key = { it.id }) { deck ->
                            DeckItem(
                                deck = deck,
                                onClick = { onDeckClick(deck.id) },
                                onStudy = { onStudyClick(deck.id) },
                                onDelete = { onDeleteDeck(deck.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Popup modal dialog rendered when showCreateDialog is true
    if (showCreateDialog) {
        CreateDeckDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, desc ->
                onCreateDeck(name, desc)
                showCreateDialog = false // Close dialog after creating deck
            }
        )
    }
}

// ---------------------------------------------------------------------------
// 3. HELPER COMPONENTS
//    - Individual Deck Card item displaying title, optional description, Study button, & delete icon.
// ---------------------------------------------------------------------------
@Composable
private fun DeckItem(
    deck: Deck,
    onClick: () -> Unit,
    onStudy: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // Makes entire card tapable with ripple
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Shadow depth
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // weight(1f) expands Column to take all remaining width, pushing buttons to right
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deck.name,
                    style = MaterialTheme.typography.titleMedium
                )
                // Optional description rendered ONCE only if text is not blank
                if (deck.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = deck.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // "Study" button opens Spaced Repetition Review Mode
            TextButton(onClick = onStudy) {
                Text("Study")
            }
            // Trash Icon Button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete ${deck.name}"
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 4. DIALOGS
//    - Modal form with text fields for creating a new deck.
// ---------------------------------------------------------------------------
@Composable
private fun CreateDeckDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    // Local form state for text fields
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss, // Fired when user taps outside dialog or back button
        title = { Text("Create New Deck") },
        text = {
            Column {
                // Deck Name Text Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Description Text Input (Optional)
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
                enabled = name.isNotBlank() // Disabled until user types a name
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
//    - Visual preview in Android Studio's design tab with mock data.
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
                )
            )
        )
    }
}