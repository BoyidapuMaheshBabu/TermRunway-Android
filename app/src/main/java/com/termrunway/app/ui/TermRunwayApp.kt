package com.termrunway.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.termrunway.app.data.ExpenseStorage
import com.termrunway.app.data.IncomeStorage
import com.termrunway.app.ui.expense.AddExpenseScreen
import com.termrunway.app.ui.history.HistoryScreen
import com.termrunway.app.ui.home.HomeScreen
import com.termrunway.app.ui.income.AddIncomeScreen

private enum class AppScreen {
    HOME,
    ADD_INCOME,
    ADD_EXPENSE,
    HISTORY
}

@Composable
fun TermRunwayApp() {
    val context = LocalContext.current
    val expenseStorage = remember {
        ExpenseStorage(context.applicationContext)
    }
    val incomeStorage = remember {
        IncomeStorage(context.applicationContext)
    }

    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.HOME) }
    var expenses by remember { mutableStateOf(expenseStorage.loadExpenses()) }
    var incomes by remember { mutableStateOf(incomeStorage.loadIncomes()) }
    var saveError by remember { mutableStateOf<String?>(null) }

    when (currentScreen) {
        AppScreen.HOME -> {
            HomeScreen(
                expenses = expenses,
                incomes = incomes,
                onAddIncome = {
                    saveError = null
                    currentScreen = AppScreen.ADD_INCOME
                },
                onAddExpense = {
                    saveError = null
                    currentScreen = AppScreen.ADD_EXPENSE
                },
                onViewHistory = {
                    currentScreen = AppScreen.HISTORY
                }
            )
        }

        AppScreen.ADD_INCOME -> {
            AddIncomeScreen(
                saveError = saveError,
                onBack = {
                    saveError = null
                    currentScreen = AppScreen.HOME
                },
                onSave = { income ->
                    val updatedIncomes = listOf(income) + incomes
                    val saved = runCatching {
                        incomeStorage.saveIncomes(updatedIncomes)
                    }.isSuccess

                    if (saved) {
                        incomes = updatedIncomes
                        saveError = null
                        currentScreen = AppScreen.HOME
                    } else {
                        saveError = "Couldn't save the income. Please try again."
                    }

                    saved
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
                        expenseStorage.saveExpenses(updatedExpenses)
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
