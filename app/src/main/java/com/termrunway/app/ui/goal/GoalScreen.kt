package com.termrunway.app.ui.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.ExpenseAmount
import com.termrunway.app.data.SavingsGoal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(
    currentGoal: SavingsGoal?,
    onBack: () -> Unit,
    onSave: (SavingsGoal) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(currentGoal?.name ?: "") }
    var targetText by remember { mutableStateOf(currentGoal?.targetAmountCents?.let { ExpenseAmount.format(it) } ?: "") }
    var savedText by remember { mutableStateOf(currentGoal?.savedAmountCents?.let { ExpenseAmount.format(it) } ?: "") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Savings Goal") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (currentGoal != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(currentGoal.name, style = MaterialTheme.typography.titleLarge)
                        
                        val remaining = (currentGoal.targetAmountCents - currentGoal.savedAmountCents).coerceAtLeast(0L)
                        val progress = if (currentGoal.targetAmountCents > 0) {
                            (currentGoal.savedAmountCents.toFloat() / currentGoal.targetAmountCents.toFloat()).coerceIn(0f, 1f)
                        } else 0f
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("₹${ExpenseAmount.format(currentGoal.savedAmountCents)} / ₹${ExpenseAmount.format(currentGoal.targetAmountCents)}")
                            Text("${(progress * 100).toInt()}%")
                        }
                        
                        Box(modifier = Modifier.fillMaxWidth().height(12.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))) {
                            Box(modifier = Modifier.fillMaxWidth(progress).height(12.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp)))
                        }
                        
                        Text("Remaining: ₹${ExpenseAmount.format(remaining)}", style = MaterialTheme.typography.bodyMedium)
                        
                        if (currentGoal.savedAmountCents >= currentGoal.targetAmountCents && currentGoal.targetAmountCents > 0) {
                            Text("Goal Completed!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Text("Update Goal", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Goal Name") },
                singleLine = true
            )

            OutlinedTextField(
                value = targetText,
                onValueChange = { targetText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Target Amount") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = savedText,
                onValueChange = { savedText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Current Saved Amount") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            
            errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorText = "Please enter a goal name."
                        return@Button
                    }
                    val target = ExpenseAmount.parseToCents(targetText) ?: 0L
                    val saved = ExpenseAmount.parseToCents(savedText) ?: 0L
                    
                    if (target < 0 || saved < 0) {
                        errorText = "Amounts cannot be negative."
                        return@Button
                    }
                    
                    errorText = null
                    onSave(SavingsGoal(name, target, saved))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Goal")
            }
        }
    }
}
