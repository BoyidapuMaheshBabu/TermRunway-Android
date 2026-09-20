package com.termrunway.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.termrunway.app.ui.home.HomeScreen
import com.termrunway.app.ui.theme.TermRunwayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TermRunwayTheme {
                HomeScreen()
            }
        }
    }
}
