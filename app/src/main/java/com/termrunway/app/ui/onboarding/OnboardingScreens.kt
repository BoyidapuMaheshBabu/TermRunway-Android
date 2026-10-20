package com.termrunway.app.ui.onboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import kotlinx.coroutines.launch

@Composable
fun StartupScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {}
}

@Composable
fun RestoreBackupDialogs(
    pendingSnapshot: BackupSnapshot?,
    hasLocalData: Boolean,
    showConfirmDialog: Boolean,
    isRestoring: Boolean,
    showSuccessDialog: Boolean,
    errorMessage: String?,
    onDismissError: () -> Unit,
    onDismissRestoreFlow: () -> Unit,
    onBackupFirst: () -> Unit,
    onContinueToConfirm: () -> Unit,
    onBackToFirstDialog: () -> Unit,
    onConfirmRestore: () -> Unit,
    onDismissSuccess: () -> Unit
) {
    // Error Dialog
    errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text("Invalid Backup File", fontWeight = FontWeight.Bold) },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = onDismissError) { Text("OK") }
            }
        )
    }

    // Progress Dialog
    if (isRestoring) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    Text("Restoring your data…", fontWeight = FontWeight.Bold)
                }
            },
            text = { Text("Please wait while your data is being restored.") },
            confirmButton = {}
        )
    }

    // Success Dialog
    if (showSuccessDialog && pendingSnapshot != null) {
        AlertDialog(
            onDismissRequest = onDismissSuccess,
            title = { Text("✓ Restore Complete", fontWeight = FontWeight.Bold) },
            text = {
                Text("Your TermRunway data has been successfully restored on this device.")
            },
            confirmButton = {
                Button(onClick = onDismissSuccess) {
                    Text("Continue", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Preview / Confirm Dialogs
    pendingSnapshot?.let { snapshot ->
        if (!showSuccessDialog && !isRestoring) {
            if (showConfirmDialog) {
                // Second Dialog: Confirm Restore
                AlertDialog(
                    onDismissRequest = onDismissRestoreFlow,
                    title = {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Confirm Restore", fontWeight = FontWeight.Bold)
                            IconButton(onClick = onDismissRestoreFlow) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    },
                    text = {
                        Text("Your current data will be replaced with the selected backup. Do you want to continue?")
                    },
                    confirmButton = {
                        Button(onClick = onConfirmRestore) {
                            Text("Restore", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = onBackToFirstDialog) {
                            Text("Go Back")
                        }
                    }
                )
            } else {
                // First Dialog: Replace Current Data?
                AlertDialog(
                    onDismissRequest = onDismissRestoreFlow,
                    title = {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Replace Current Data?", fontWeight = FontWeight.Bold)
                            IconButton(onClick = onDismissRestoreFlow) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Restoring this backup will replace the data currently saved on this device. Back up your data first to avoid losing it.")
                            Text("Backup summary (${snapshot.name.ifBlank { "User" }}):", fontWeight = FontWeight.SemiBold)
                            Column(Modifier.padding(start = 4.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("• Transactions: ${snapshot.transactions.size}", style = MaterialTheme.typography.bodyMedium)
                                Text("• Plans: ${snapshot.plans.size}", style = MaterialTheme.typography.bodyMedium)
                                Text("• Planned Incomes: ${snapshot.plannedIncomes.size}", style = MaterialTheme.typography.bodyMedium)
                                Text("• Planned Expenses: ${snapshot.plannedExpenses.size}", style = MaterialTheme.typography.bodyMedium)
                                Text("• Categories: ${snapshot.categories.size}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = onContinueToConfirm) {
                            Text("Continue", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = onBackupFirst) {
                            Text("Backup First")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun WelcomeScreen(
    onSave: (String) -> Unit,
    onRestoreBackup: (Uri, (Boolean, String?) -> Unit) -> Unit = { _, _ -> },
    hasLocalData: Boolean = false
) {
    var name by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var pendingSnapshot by remember { mutableStateOf<BackupSnapshot?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                BackupManager.read(context.contentResolver, uri)
            }.onSuccess { snapshot ->
                pendingUri = uri
                pendingSnapshot = snapshot
                showConfirmDialog = false
                isRestoring = false
                showSuccessDialog = false
                errorMessage = null
            }.onFailure { err ->
                errorMessage = err.message ?: "This backup file couldn't be restored. Please choose a valid TermRunway backup."
            }
        }
    }

    RestoreBackupDialogs(
        pendingSnapshot = pendingSnapshot,
        hasLocalData = hasLocalData,
        showConfirmDialog = showConfirmDialog,
        isRestoring = isRestoring,
        showSuccessDialog = showSuccessDialog,
        errorMessage = errorMessage,
        onDismissError = { errorMessage = null },
        onDismissRestoreFlow = {
            pendingSnapshot = null
            pendingUri = null
            showConfirmDialog = false
            isRestoring = false
        },
        onBackupFirst = {
            errorMessage = "No local data to back up yet."
        },
        onContinueToConfirm = {
            showConfirmDialog = true
        },
        onBackToFirstDialog = {
            showConfirmDialog = false
        },
        onConfirmRestore = {
            val uri = pendingUri ?: return@RestoreBackupDialogs
            isRestoring = true
            onRestoreBackup(uri) { success, err ->
                isRestoring = false
                if (success) {
                    showSuccessDialog = true
                } else {
                    pendingSnapshot = null
                    pendingUri = null
                    errorMessage = err ?: "This backup file couldn't be restored. Please choose a valid TermRunway backup."
                }
            }
        },
        onDismissSuccess = {
            val snapshotName = pendingSnapshot?.name.orEmpty()
            showSuccessDialog = false
            pendingSnapshot = null
            pendingUri = null
            onSave(snapshotName.ifBlank { "User" })
        }
    )

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
