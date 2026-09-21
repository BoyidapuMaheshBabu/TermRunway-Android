package com.termrunway.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onOpenPlanning: () -> Unit,
    onOpenGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Profile") }) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Local Profile", style = MaterialTheme.typography.titleLarge)
                    Text("No account is required for TermRunway V1.")
                    Text("Your financial data is stored on this device.")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("App", style = MaterialTheme.typography.titleLarge)
                    Text("TermRunway")
                    Text("Version 1.0")
                    Text("Offline-first")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenPlanning, modifier = Modifier.fillMaxWidth()) {
                    Text("Manage Term Plan")
                }
                Button(onClick = onOpenGoal, modifier = Modifier.fillMaxWidth()) {
                    Text("Manage Savings Goal")
                }
            }
        }
    }
}