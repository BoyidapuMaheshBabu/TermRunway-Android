package com.termrunway.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.ExpenseStorage
import com.termrunway.app.data.IncomeStorage
import com.termrunway.app.data.TermPlanStorage
import com.termrunway.app.ui.activity.ActivityScreen
import com.termrunway.app.ui.expense.AddExpenseScreen
import com.termrunway.app.ui.home.HomeScreen
import com.termrunway.app.ui.income.AddIncomeScreen
import com.termrunway.app.ui.insights.InsightsScreen
import com.termrunway.app.ui.navigation.MainBottomBar
import com.termrunway.app.ui.navigation.MainTab
import com.termrunway.app.ui.planning.PlanScreen

private enum class AddMode {
    EXPENSE,
    INCOME
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermRunwayApp() {
    val context = LocalContext.current.applicationContext
    val expenseStorage = remember { ExpenseStorage(context) }
    val incomeStorage = remember { IncomeStorage(context) }
    val planStorage = remember { TermPlanStorage(context) }

    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    var addMode by rememberSaveable { mutableStateOf<AddMode?>(null) }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }

    var expenses by remember { mutableStateOf(expenseStorage.loadExpenses()) }
    var incomes by remember { mutableStateOf(incomeStorage.loadIncomes()) }
    var plan by remember { mutableStateOf(planStorage.loadPlan()) }
    var saveError by remember { mutableStateOf<String?>(null) }

    if (addMode != null) {
        when (addMode) {
            AddMode.EXPENSE -> AddExpenseScreen(
                onBack = {
                    saveError = null
                    addMode = null
                },
                saveError = saveError,
                onSave = { expense ->
                    val updated = listOf(expense) + expenses
                    runCatching {
                        expenseStorage.saveExpenses(updated)
                        expenses = updated
                        saveError = null
                        addMode = null
                    }.onFailure {
                        saveError = "Could not save the expense."
                    }
                    saveError == null
                }
            )

            AddMode.INCOME -> AddIncomeScreen(
                onBack = {
                    saveError = null
                    addMode = null
                },
                saveError = saveError,
                onSave = { income ->
                    val updated = listOf(income) + incomes
                    runCatching {
                        incomeStorage.saveIncomes(updated)
                        incomes = updated
                        saveError = null
                        addMode = null
                    }.onFailure {
                        saveError = "Could not save the income."
                    }
                    saveError == null
                }
            )

            null -> Unit
        }
        return
    }

    Scaffold(
        bottomBar = {
            MainBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onAddTransaction = {
                    saveError = null
                    showAddSheet = true
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        when (selectedTab) {
            MainTab.HOME -> HomeScreen(
                expenses = expenses,
                incomes = incomes,
                plan = plan,
                onOpenPlan = { selectedTab = MainTab.PLAN },
                onOpenActivity = { selectedTab = MainTab.ACTIVITY },
                onOpenInsights = { selectedTab = MainTab.INSIGHTS },
                modifier = Modifier.padding(innerPadding)
            )

            MainTab.PLAN -> PlanScreen(
                currentPlan = plan,
                expenses = expenses,
                incomes = incomes,
                onSave = { newPlan ->
                    runCatching {
                        planStorage.savePlan(newPlan)
                        plan = newPlan
                        saveError = null
                    }.onFailure {
                        saveError = "Could not save the plan."
                    }
                },
                modifier = Modifier.padding(innerPadding)
            )

            MainTab.ACTIVITY -> ActivityScreen(
                expenses = expenses,
                incomes = incomes,
                modifier = Modifier.padding(innerPadding)
            )

            MainTab.INSIGHTS -> InsightsScreen(
                expenses = expenses,
                incomes = incomes,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false }
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Add Transaction",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    "Choose what you want to record.",
                    modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                )
                Button(
                    onClick = {
                        showAddSheet = false
                        addMode = AddMode.EXPENSE
                    }
                ) {
                    Text("Expense")
                }
                Button(
                    onClick = {
                        showAddSheet = false
                        addMode = AddMode.INCOME
                    },
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                ) {
                    Text("Income")
                }
            }
        }
    }
}
