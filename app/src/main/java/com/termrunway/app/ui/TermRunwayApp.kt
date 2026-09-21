package com.termrunway.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.termrunway.app.data.ExpenseStorage
import com.termrunway.app.data.IncomeStorage
import com.termrunway.app.data.SavingsGoalStorage
import com.termrunway.app.data.TermPlanStorage
import com.termrunway.app.logic.calculateRunway
import com.termrunway.app.ui.analysis.AnalysisScreen
import com.termrunway.app.ui.charts.ChartsScreen
import com.termrunway.app.ui.decision.DecisionScreen
import com.termrunway.app.ui.expense.AddExpenseScreen
import com.termrunway.app.ui.goal.GoalScreen
import com.termrunway.app.ui.history.HistoryScreen
import com.termrunway.app.ui.home.HomeScreen
import com.termrunway.app.ui.income.AddIncomeScreen
import com.termrunway.app.ui.navigation.MainBottomBar
import com.termrunway.app.ui.navigation.MainTab
import com.termrunway.app.ui.planning.PlanningScreen
import com.termrunway.app.ui.profile.ProfileScreen
import com.termrunway.app.ui.reports.ReportsScreen

private enum class AppScreen {
    HOME,
    CHARTS,
    REPORTS,
    PROFILE,
    ADD_INCOME,
    ADD_EXPENSE,
    HISTORY,
    PLANNING,
    ANALYSIS,
    DECISION,
    GOAL
}

@Composable
fun TermRunwayApp() {
    val context = LocalContext.current
    val expenseStorage = remember { ExpenseStorage(context.applicationContext) }
    val incomeStorage = remember { IncomeStorage(context.applicationContext) }
    val planStorage = remember { TermPlanStorage(context.applicationContext) }
    val goalStorage = remember { SavingsGoalStorage(context.applicationContext) }

    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.HOME) }
    var expenses by remember { mutableStateOf(expenseStorage.loadExpenses()) }
    var incomes by remember { mutableStateOf(incomeStorage.loadIncomes()) }
    var plan by remember { mutableStateOf(planStorage.loadPlan()) }
    var goal by remember { mutableStateOf(goalStorage.loadGoal()) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var showAddActions by rememberSaveable { mutableStateOf(false) }

    val forecast = remember(plan, expenses, incomes) {
        calculateRunway(plan, expenses, incomes)
    }

    val isMainScreen = when (currentScreen) {
        AppScreen.HOME,
        AppScreen.CHARTS,
        AppScreen.REPORTS,
        AppScreen.PROFILE -> true
        else -> false
    }

    if (isMainScreen) {
        val selectedTab = when (currentScreen) {
            AppScreen.HOME -> MainTab.HOME
            AppScreen.CHARTS -> MainTab.CHARTS
            AppScreen.REPORTS -> MainTab.REPORTS
            AppScreen.PROFILE -> MainTab.PROFILE
            else -> MainTab.HOME
        }

        Scaffold(
            bottomBar = {
                MainBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        currentScreen = when (tab) {
                            MainTab.HOME -> AppScreen.HOME
                            MainTab.CHARTS -> AppScreen.CHARTS
                            MainTab.REPORTS -> AppScreen.REPORTS
                            MainTab.PROFILE -> AppScreen.PROFILE
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddActions = true }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add transaction"
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.Center
        ) { innerPadding ->
            when (currentScreen) {
                AppScreen.HOME -> HomeScreen(
                    expenses = expenses,
                    incomes = incomes,
                    forecast = forecast,
                    onViewHistory = { currentScreen = AppScreen.HISTORY },
                    onPlanTerm = { currentScreen = AppScreen.PLANNING },
                    onViewAnalysis = { currentScreen = AppScreen.ANALYSIS },
                    onDecisionCheck = { currentScreen = AppScreen.DECISION },
                    onViewGoal = { currentScreen = AppScreen.GOAL },
                    modifier = Modifier.padding(innerPadding)
                )

                AppScreen.CHARTS -> ChartsScreen(
                    expenses = expenses,
                    incomes = incomes,
                    plan = plan,
                    forecast = forecast,
                    modifier = Modifier.padding(innerPadding)
                )

                AppScreen.REPORTS -> ReportsScreen(
                    expenses = expenses,
                    incomes = incomes,
                    plan = plan,
                    forecast = forecast,
                    goal = goal,
                    onOpenPlanning = { currentScreen = AppScreen.PLANNING },
                    onOpenHistory = { currentScreen = AppScreen.HISTORY },
                    onOpenAnalysis = { currentScreen = AppScreen.ANALYSIS },
                    onOpenGoal = { currentScreen = AppScreen.GOAL },
                    modifier = Modifier.padding(innerPadding)
                )

                AppScreen.PROFILE -> ProfileScreen(
                    onOpenPlanning = { currentScreen = AppScreen.PLANNING },
                    onOpenGoal = { currentScreen = AppScreen.GOAL },
                    modifier = Modifier.padding(innerPadding)
                )

                else -> Unit
            }
        }

        if (showAddActions) {
            ModalBottomSheet(onDismissRequest = { showAddActions = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Add Transaction",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "Choose what you want to record.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Button(
                        onClick = {
                            showAddActions = false
                            saveError = null
                            currentScreen = AppScreen.ADD_EXPENSE
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Expense")
                    }

                    Button(
                        onClick = {
                            showAddActions = false
                            saveError = null
                            currentScreen = AppScreen.ADD_INCOME
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Income")
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(
                        onClick = { showAddActions = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    } else {
        when (currentScreen) {
            AppScreen.ADD_INCOME -> AddIncomeScreen(
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

            AppScreen.ADD_EXPENSE -> AddExpenseScreen(
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

            AppScreen.HISTORY -> HistoryScreen(
                expenses = expenses,
                onBack = { currentScreen = AppScreen.HOME }
            )

            AppScreen.PLANNING -> PlanningScreen(
                currentPlan = plan,
                onBack = { currentScreen = AppScreen.HOME },
                onSave = { newPlan ->
                    runCatching { planStorage.savePlan(newPlan) }
                    plan = newPlan
                    currentScreen = AppScreen.HOME
                }
            )

            AppScreen.ANALYSIS -> AnalysisScreen(
                expenses = expenses,
                plan = plan,
                onBack = { currentScreen = AppScreen.HOME }
            )

            AppScreen.DECISION -> DecisionScreen(
                forecast = forecast,
                onBack = { currentScreen = AppScreen.HOME }
            )

            AppScreen.GOAL -> GoalScreen(
                currentGoal = goal,
                onBack = { currentScreen = AppScreen.HOME },
                onSave = { newGoal ->
                    runCatching { goalStorage.saveGoal(newGoal) }
                    goal = newGoal
                    currentScreen = AppScreen.HOME
                }
            )

            else -> Unit
        }
    }
}
