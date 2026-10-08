package com.nejon.branta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nejon.branta.feature.cards.CardsScreenRoute
import com.nejon.branta.feature.decks.DecksScreenRoute
import com.nejon.branta.feature.review.ReviewScreenRoute
import com.nejon.branta.feature.stats.StatsScreenRoute
import com.nejon.branta.ui.theme.BrantaTheme

// ---------------------------------------------------------------------------
// MAIN ACTIVITY
// - App router handling "decks", "stats", "cards/{deckId}", and "review/{deckId}".
// - Includes standard Material 3 Bottom Navigation Bar for top-level tabs.
// ---------------------------------------------------------------------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enables transparent edge-to-edge system bars
        setContent {
            BrantaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // 1. Central NavController router instance
                    val navController = rememberNavController()

                    // 2. Observe current active route to highlight active tab and control bottom bar visibility
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    Scaffold(
                        contentWindowInsets = WindowInsets(0, 0, 0, 0), // Outer Scaffold does NOT consume top status bar insets!
                        bottomBar = {
                            // Display Bottom Navigation Bar ONLY on top-level tabs ("decks" and "stats")
                            if (currentRoute == "decks" || currentRoute == "stats") {
                                NavigationBar {
                                    // Tab 1: Decks
                                    NavigationBarItem(
                                        selected = currentRoute == "decks",
                                        onClick = {
                                            if (currentRoute != "decks") {
                                                navController.navigate("decks") {
                                                    popUpTo("decks") { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        icon = { Icon(Icons.Default.Style, contentDescription = "Decks") },
                                        label = { Text("Decks") }
                                    )

                                    // Tab 2: Stats
                                    NavigationBarItem(
                                        selected = currentRoute == "stats",
                                        onClick = {
                                            if (currentRoute != "stats") {
                                                navController.navigate("stats") {
                                                    popUpTo("decks") { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        icon = { Icon(Icons.Default.BarChart, contentDescription = "Stats") },
                                        label = { Text("Stats") }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        // 3. NavHost container padded at bottom and consumeWindowInsets to inform child Scaffolds!
                        NavHost(
                            navController = navController,
                            startDestination = "decks",
                            modifier = Modifier
                                .padding(bottom = innerPadding.calculateBottomPadding())
                                .consumeWindowInsets(innerPadding)
                        ) {
                            // Route 1: Decks Screen ("decks")
                            composable("decks") {
                                DecksScreenRoute(
                                    onDeckClick = { deckId ->
                                        navController.navigate("cards/$deckId")
                                    },
                                    onStudyClick = { deckId ->
                                        navController.navigate("review/$deckId")
                                    }
                                )
                            }

                            // Route 2: Learning Statistics Dashboard Screen ("stats")
                            composable("stats") {
                                StatsScreenRoute()
                            }

                            // Route 3: Cards Screen ("cards/{deckId}")
                            composable("cards/{deckId}") { backStackEntry ->
                                val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
                                CardsScreenRoute(
                                    deckId = deckId,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }

                            // Route 4: Review Session Screen ("review/{deckId}")
                            composable("review/{deckId}") { backStackEntry ->
                                val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
                                ReviewScreenRoute(
                                    deckId = deckId,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}