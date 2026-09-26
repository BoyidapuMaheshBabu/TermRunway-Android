package com.termrunway.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SetupScreen(onComplete: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().imePadding().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "TermRunway",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "PLAN • CALCULATE • ACHIEVE",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 6.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Your private, offline money companion for everyday tracking and semester planning.",
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(32) },
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            label = { Text("Your name") },
            singleLine = true
        )
        Button(
            enabled = name.trim().isNotEmpty(),
            onClick = { onComplete(name.trim()) },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Enter TermRunway")
        }
    }
}
