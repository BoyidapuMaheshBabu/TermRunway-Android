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
import com.termrunway.app.data.SavingsGoalStorage
import com.termrunway.app.data.TermPlanStorage
import com.termrunway.app.logic.calculateRunway
import com.termrunway.app.ui.analysis.AnalysisScreen
import com.termrunway.app.ui.decision.DecisionScreen
import com.termrunway.app.ui.expense.AddExpenseScreen
import com.termrunway.app.ui.goal.GoalScreen
import com.termrunway.app.ui.history.HistoryScreen
import com.termrunway.app.ui.home.HomeScreen
import com.termrunway.app.ui.income.AddIncomeScreen
import com.termrunway.app.ui.planning.PlanningScreen

private enum class AppScreen {
    HOME,
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

    val forecast = remember(plan, expenses, incomes) {
        calculateRunway(plan, expenses, incomes)
    }

    when (currentScreen) {
        AppScreen.HOME -> {
            HomeScreen(
                expenses = expenses,
                incomes = incomes,
                forecast = forecast,
                onAddIncome = {
                    saveError = null
                    currentScreen = AppScreen.ADD_INCOME
                },
                onAddExpense = {
                    saveError = null
                    currentScreen = AppScreen.ADD_EXPENSE
                },
                onViewHistory = { currentScreen = AppScreen.HISTORY },
                onPlanTerm = { currentScreen = AppScreen.PLANNING },
                onViewAnalysis = { currentScreen = AppScreen.ANALYSIS },
                onDecisionCheck = { currentScreen = AppScreen.DECISION },
                onViewGoal = { currentScreen = AppScreen.GOAL }
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
                    val saved = runCatching { incomeStorage.saveIncomes(updatedIncomes) }.isSuccess
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
                    val saved = runCatching { expenseStorage.saveExpenses(updatedExpenses) }.isSuccess
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
                onBack = { currentScreen = AppScreen.HOME }
            )
        }

        AppScreen.PLANNING -> {
            PlanningScreen(
                currentPlan = plan,
                onBack = { currentScreen = AppScreen.HOME },
                onSave = { newPlan ->
                    runCatching { planStorage.savePlan(newPlan) }
                    plan = newPlan
                    currentScreen = AppScreen.HOME
                }
            )
        }

        AppScreen.ANALYSIS -> {
            AnalysisScreen(
                expenses = expenses,
                plan = plan,
                onBack = { currentScreen = AppScreen.HOME }
            )
        }

        AppScreen.DECISION -> {
            DecisionScreen(
                forecast = forecast,
                onBack = { currentScreen = AppScreen.HOME }
            )
        }

        AppScreen.GOAL -> {
            GoalScreen(
                currentGoal = goal,
                onBack = { currentScreen = AppScreen.HOME },
                onSave = { newGoal ->
                    runCatching { goalStorage.saveGoal(newGoal) }
                    goal = newGoal
                    currentScreen = AppScreen.HOME
                }
            )
        }
    }
}
