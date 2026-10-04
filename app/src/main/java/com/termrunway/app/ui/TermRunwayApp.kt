package com.termrunway.app.ui

import androidx.compose.runtime.Composable
import com.termrunway.app.ui.navigation.AppNavigation

@Composable
fun TermRunwayApp(viewModel: TermRunwayViewModel) {
    AppNavigation(viewModel)
}
