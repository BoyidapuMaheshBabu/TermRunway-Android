package com.termrunway.app.ui.onboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.BackupManager
import com.termrunway.app.data.BackupSnapshot
import com.termrunway.app.ui.components.AppLogoMark
import com.termrunway.app.ui.theme.RunwayMuted
import com.termrunway.app.ui.theme.RunwayRed
import kotlinx.coroutines.launch

@Composable
fun StartupScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {}
}

@Composable
fun WelcomeScreen(
    onSave: (String) -> Unit,
    onRestoreBackup: (Uri, (Boolean) -> Unit) -> Unit = { _, _ -> },
    hasLocalData: Boolean = false
) {
    var name by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var pendingSnapshot by remember { mutableStateOf<BackupSnapshot?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                BackupManager.read(context.contentResolver, uri)
            }.onSuccess { snapshot ->
                pendingUri = uri
                pendingSnapshot = snapshot
            }.onFailure {
                errorMessage = "This backup file couldn't be restored. Please choose a valid TermRunway backup."
            }
        }
    }

    // Error Dialog
    errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Invalid Backup File", fontWeight = FontWeight.Bold) },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) { Text("OK") }
            }
        )
    }

    // Success Dialog
    if (showSuccessDialog && pendingSnapshot != null) {
        val snapshotName = pendingSnapshot?.name.orEmpty()
        AlertDialog(
            onDismissRequest = {},
            title = { Text("✓ Restore Complete", fontWeight = FontWeight.Bold) },
            text = {
                Text("Your TermRunway data has been successfully restored on this device.")
            },
            confirmButton = {
                Button(onClick = {
                    showSuccessDialog = false
                    onSave(snapshotName.ifBlank { "User" })
                }) {
                    Text("Continue", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Preview / Confirm Dialog
    pendingSnapshot?.let { snapshot ->
        if (!showSuccessDialog) {
            AlertDialog(
                onDismissRequest = {
                    pendingSnapshot = null
                    pendingUri = null
                },
                title = { Text("Restore from Backup", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Backup details for ${snapshot.name.ifBlank { "User" }}:", fontWeight = FontWeight.SemiBold)
                        Column(Modifier.padding(start = 4.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("• Transactions: ${snapshot.transactions.size}", style = MaterialTheme.typography.bodyMedium)
                            Text("• Plans: ${snapshot.plans.size}", style = MaterialTheme.typography.bodyMedium)
                            Text("• Planned Incomes: ${snapshot.plannedIncomes.size}", style = MaterialTheme.typography.bodyMedium)
                            Text("• Planned Expenses: ${snapshot.plannedExpenses.size}", style = MaterialTheme.typography.bodyMedium)
                            Text("• Categories: ${snapshot.categories.size}", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (hasLocalData) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "This will replace the current TermRunway data on this device with the selected backup.",
                                color = RunwayRed,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val uri = pendingUri ?: return@Button
                            onRestoreBackup(uri) { success ->
                                if (success) {
                                    showSuccessDialog = true
                                } else {
                                    pendingSnapshot = null
                                    pendingUri = null
                                    errorMessage = "This backup file couldn't be restored. Please choose a valid TermRunway backup."
                                }
                            }
                        }
                    ) {
                        Text("Restore", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        pendingSnapshot = null
                        pendingUri = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppLogoMark(Modifier.fillMaxWidth().height(140.dp))
            Spacer(Modifier.height(20.dp))
            Text(
                "Welcome to TermRunway",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Understand what happened to your money today, and plan what happens next.",
                color = RunwayMuted,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(40) },
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            coroutineScope.launch {
                                bringIntoViewRequester.bringIntoView()
                            }
                        }
                    },
                label = { Text("Your name") },
                supportingText = { Text("TermRunway keeps your financial data on this device.") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    focusManager.clearFocus()
                    onSave(name.trim())
                },
                enabled = name.trim().isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) { Text("Start using TermRunway", fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(20.dp))
            Text(
                "Already have a backup file?",
                style = MaterialTheme.typography.bodySmall,
                color = RunwayMuted,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    focusManager.clearFocus()
                    filePickerLauncher.launch(arrayOf("application/json", "*/*"))
                },
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Icon(
                    Icons.Outlined.Restore,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Restore from Backup", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
