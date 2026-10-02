package com.termrunway.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.AppData
import com.termrunway.app.data.ThemeMode
import com.termrunway.app.util.parseMoneyToCents

@Composable
fun SettingsScreen(
    data: AppData,
    onBack: () -> Unit,
    onSaveUsername: (String) -> Unit,
    onSaveDailyLimit: (Long) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onCreateBackup: () -> Unit,
    onPickBackup: () -> Unit,
    onDeleteAll: () -> Unit
) {
    var username by remember(data.username) { mutableStateOf(data.username) }
    var limitText by remember(data.dailyLimitCents) {
        mutableStateOf(if (data.dailyLimitCents == 0L) "" else data.dailyLimitCents.toDouble().div(100.0).toString())
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).imePadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                }
                Column(Modifier.weight(1f)) {
                    Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Your data stays on this phone.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Profile", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it.take(32) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Name") },
                        singleLine = true
                    )
                    Button(
                        enabled = username.trim().isNotEmpty() && username.trim() != data.username,
                        onClick = { onSaveUsername(username.trim()) }
                    ) { Text("Save name") }
                }
            }
        }

        item {
            Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Daily spending reference", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Optional only. It helps you compare today's spending against a target, but it never blocks an expense.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = limitText,
                        onValueChange = { limitText = it.filter { ch -> ch.isDigit() || ch == '.' }.take(12) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Limit per day (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Button(onClick = {
                        val cents = if (limitText.isBlank()) 0L else parseMoneyToCents(limitText) ?: -1L
                        if (cents >= 0L) onSaveDailyLimit(cents)
                    }) { Text("Save limit") }
                }
            }
        }

        item {
            Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Theme", fontWeight = FontWeight.SemiBold)
                    ThemeOption("System default", ThemeMode.SYSTEM, data.theme, onThemeChange)
                    ThemeOption("Light", ThemeMode.LIGHT, data.theme, onThemeChange)
                    ThemeOption("Dark", ThemeMode.DARK, data.theme, onThemeChange)
                }
            }
        }

        item {
            Card(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Backup & restore", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Backups include your transactions and settings. Keep the exported JSON file private.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onCreateBackup, modifier = Modifier.weight(1f)) { Text("Backup") }
                        Button(onClick = onPickBackup, modifier = Modifier.weight(1f)) { Text("Restore") }
                    }
                }
            }
        }

        item {
            Card(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Danger zone", fontWeight = FontWeight.SemiBold)
                    Text("Delete every local TermRunway record from this phone.", color = MaterialTheme.colorScheme.onErrorContainer)
                    TextButton(onClick = onDeleteAll) { Text("Delete all local data") }
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(label: String, mode: ThemeMode, selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onSelect(mode) }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected == mode, onClick = { onSelect(mode) })
        Text(label, modifier = Modifier.padding(start = 4.dp))
    }
}
