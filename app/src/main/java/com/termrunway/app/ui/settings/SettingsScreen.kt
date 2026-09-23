package com.termrunway.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.termrunway.app.ui.daily.formatRupees
import com.termrunway.app.ui.daily.parseAmountCents

enum class SettingsSection { MAIN, PROFILE, BACKUP }
enum class ThemeChoice { SYSTEM, LIGHT, DARK }

@Composable
fun SettingsScreen(
    section: SettingsSection,
    username: String,
    dailyLimitCents: Long,
    theme: ThemeChoice,
    lastBackupName: String,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenBackup: () -> Unit,
    onSaveUsername: (String) -> Unit,
    onSaveDailyLimit: (Long) -> Unit,
    onThemeChange: (ThemeChoice) -> Unit,
    onCreateBackup: () -> Unit,
    onRestoreBackup: () -> Unit,
    onDeleteAll: () -> Unit
) {
    when (section) {
        SettingsSection.MAIN -> MainSettings(
            username, dailyLimitCents, theme, onBack, onOpenProfile, onOpenBackup, onThemeChange, onSaveDailyLimit, onDeleteAll
        )
        SettingsSection.PROFILE -> ProfileSettings(username, onBack, onSaveUsername)
        SettingsSection.BACKUP -> BackupSettings(lastBackupName, onBack, onCreateBackup, onRestoreBackup)
    }
}

@Composable
private fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.material3.IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back") }
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MainSettings(
    username: String,
    dailyLimitCents: Long,
    theme: ThemeChoice,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenBackup: () -> Unit,
    onThemeChange: (ThemeChoice) -> Unit,
    onSaveDailyLimit: (Long) -> Unit,
    onDeleteAll: () -> Unit
) {
    var showTheme by rememberSaveable { mutableStateOf(false) }
    var showLimit by rememberSaveable { mutableStateOf(false) }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { ScreenHeader("Settings", onBack) }
        item { SettingsSectionLabel("PROFILE") }
        item { SettingsRow(Icons.Outlined.Person, username.ifBlank { "Set username" }, "Local profile", onOpenProfile) }
        item { SettingsSectionLabel("DAILY TRACKING") }
        item { SettingsRow(Icons.Outlined.Wallet, "Daily spending limit", if (dailyLimitCents > 0) formatRupees(dailyLimitCents) else "Not set", { showLimit = true }) }
        item { SettingsSectionLabel("APP") }
        item { SettingsRow(Icons.Outlined.Settings, "Theme", theme.name.lowercase().replaceFirstChar { it.uppercase() }, { showTheme = true }) }
        item { SettingsSectionLabel("DATA & STORAGE") }
        item { SettingsRow(Icons.Outlined.Backup, "Backup & Restore", "Protect and restore your local data", onOpenBackup) }
        item { SettingsRow(Icons.Outlined.Delete, "Delete all data", "Remove all local TermRunway data", onDeleteAll, true) }
        item { Spacer(Modifier.size(18.dp)); Text("TermRunway · Daily Tracker v1", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium) }
    }

    if (showTheme) {
        AlertDialog(
            onDismissRequest = { showTheme = false },
            title = { Text("Theme") },
            text = {
                Column {
                    ThemeChoice.entries.forEach { choice ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = theme == choice, onClick = { onThemeChange(choice); showTheme = false })
                            Text(choice.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showLimit) {
        var text by rememberSaveable { mutableStateOf(if (dailyLimitCents > 0) dailyLimitCents.div(100).toString() else "") }
        AlertDialog(
            onDismissRequest = { showLimit = false },
            title = { Text("Daily spending limit") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(text, { text = it }, label = { Text("Amount") }, leadingIcon = { Text("₹") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                    Text("This never blocks spending. It only provides an indication.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = { TextButton(onClick = { onSaveDailyLimit(parseAmountCents(text) ?: 0L); showLimit = false }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { showLimit = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 10.dp, bottom = 2.dp))
}

@Composable
private fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit, danger: Boolean = false) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Outlined.ArrowForward, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProfileSettings(username: String, onBack: () -> Unit, onSave: (String) -> Unit) {
    var value by rememberSaveable { mutableStateOf(username) }
    var error by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ScreenHeader("Profile", onBack)
        Text("Your local profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.length <= 30) { value = it; error = false } },
            label = { Text("Username") },
            singleLine = true,
            isError = error,
            supportingText = { if (error) Text("Username cannot be empty.") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { if (value.trim().isBlank()) error = true else { onSave(value.trim()); onBack() } }, modifier = Modifier.fillMaxWidth()) { Text("Save changes") }
    }
}

@Composable
private fun BackupSettings(lastBackupName: String, onBack: () -> Unit, onCreateBackup: () -> Unit, onRestoreBackup: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { ScreenHeader("Backup & Restore", onBack) }
        item {
            Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Your data stays on your device.", fontWeight = FontWeight.SemiBold)
                    Text("Create a portable JSON backup before a phone reset, reinstall, or device change.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Create Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(if (lastBackupName.isBlank()) "No backup created yet." else "Last backup: " + lastBackupName, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = onCreateBackup) { Icon(Icons.Outlined.Backup, null); Spacer(Modifier.width(6.dp)); Text("Create backup") }
                }
            }
        }
        item {
            Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Restore Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Select a TermRunway JSON backup. The restore flow will show a preview before replacing current data.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = onRestoreBackup) { Icon(Icons.Outlined.Restore, null); Spacer(Modifier.width(6.dp)); Text("Choose backup file") }
                }
            }
        }
        item {
            Text("Backup contains username, expenses, income and app preferences. Calculated summaries are rebuilt after restore.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
