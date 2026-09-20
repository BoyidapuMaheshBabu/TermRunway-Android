package com.termrunway.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.termrunway.app.data.ExpenseStorage
import com.termrunway.app.ui.expense.AddExpenseScreen
import com.termrunway.app.ui.history.HistoryScreen
import com.termrunway.app.ui.home.HomeScreen

private enum class AppScreen {
    HOME,
    ADD_EXPENSE,
    HISTORY
}

@Composable
fun TermRunwayApp() {
    val context = LocalContext.current
    val storage = remember {
        ExpenseStorage(context.applicationContext)
    }

    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.HOME) }
    var expenses by remember { mutableStateOf(storage.loadExpenses()) }
    var saveError by remember { mutableStateOf<String?>(null) }

    when (currentScreen) {
        AppScreen.HOME -> {
            HomeScreen(
                expenses = expenses,
                onAddExpense = {
                    saveError = null
                    currentScreen = AppScreen.ADD_EXPENSE
                },
                onViewHistory = {
                    currentScreen = AppScreen.HISTORY
                }
            )
        }

        AppScreen.ADD_EXPENSE -> {
            AddExpenseScreen(
                saveError = saveError,
                onBack = {
                    saveError = null
                    currentScreen = AppScreen.HOME
                },
                onSave = { expense ->
                    val updatedExpenses = listOf(expense) + expenses
                    val saved = runCatching {
                        storage.saveExpenses(updatedExpenses)
                    }.isSuccess

                    if (saved) {
                        expenses = updatedExpenses
                        saveError = null
                        currentScreen = AppScreen.HOME
                    } else {
                        saveError = "Couldn't save the expense. Please try again."
                    }

                    saved
                }
            )
        }

        AppScreen.HISTORY -> {
            HistoryScreen(
                expenses = expenses,
                onBack = {
                    currentScreen = AppScreen.HOME
                }
            )
        }
    }
}
