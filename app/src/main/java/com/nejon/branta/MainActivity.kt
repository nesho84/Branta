package com.nejon.branta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nejon.branta.feature.cards.CardsScreenRoute
import com.nejon.branta.feature.decks.DecksScreenRoute
import com.nejon.branta.ui.theme.BrantaTheme

// ---------------------------------------------------------------------------
// MAIN ACTIVITY
// - The single Activity entry point of Branta.
// - Uses official Jetpack Navigation Compose (NavHost & NavController) for routing.
// ---------------------------------------------------------------------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enables transparent edge-to-edge system bars
        setContent {
            BrantaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // 1. Create and remember the central NavController router instance
                    val navController = rememberNavController()

                    // 2. NavHost defines the route graph and starting screen destination
                    NavHost(
                        navController = navController,
                        startDestination = "decks" // Starting route name
                    ) {
                        // Destination 1: Decks Screen ("decks")
                        composable("decks") {
                            DecksScreenRoute(
                                onDeckClick = { deckId ->
                                    // Push cards route with dynamic deckId path parameter
                                    navController.navigate("cards/$deckId")
                                }
                            )
                        }

                        // Destination 2: Cards Screen ("cards/{deckId}")
                        composable("cards/{deckId}") { backStackEntry ->
                            // Extract deckId path parameter from route arguments
                            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""

                            CardsScreenRoute(
                                deckId = deckId,
                                onBackClick = {
                                    // Pop screen off backstack to return to Decks Screen
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}