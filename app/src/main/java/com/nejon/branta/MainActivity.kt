package com.nejon.branta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.nejon.branta.feature.decks.DecksScreenRoute
import com.nejon.branta.ui.theme.BrantaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrantaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DecksScreenRoute(
                        onDeckClick = { deckId ->
                            // Navigation to Card Review/Details will be added here next!
                        }
                    )
                }
            }
        }
    }
}