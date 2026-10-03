package com.termrunway.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.termrunway.app.ui.TermRunwayApp
import com.termrunway.app.ui.TermRunwayViewModel
import com.termrunway.app.ui.ThemeMode
import com.termrunway.app.ui.theme.TermRunwayTheme

class MainActivity : ComponentActivity() {
    private val viewModel: TermRunwayViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            val dark = when (state.themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            TermRunwayTheme(darkTheme = dark) {
                TermRunwayApp(viewModel)
            }
        }
    }
}
