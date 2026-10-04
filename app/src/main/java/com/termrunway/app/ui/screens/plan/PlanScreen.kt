package com.termrunway.app.ui.screens.plan

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.termrunway.app.data.CategoryType
import com.termrunway.app.data.FinancialPlan
import com.termrunway.app.data.PlannedExpense
import com.termrunway.app.data.PlannedIncome
import com.termrunway.app.data.Transaction
import com.termrunway.app.data.TransactionType
import com.termrunway.app.domain.FinancialCalculator
import com.termrunway.app.ui.components.AmountCard
import com.termrunway.app.ui.components.AppLogoMark
import com.termrunway.app.ui.components.CategorySelector
import com.termrunway.app.ui.components.EmptyState
import com.termrunway.app.ui.components.ModeToggle
import com.termrunway.app.ui.components.MoneyText
import com.termrunway.app.ui.components.ProgressAmountBar
import com.termrunway.app.ui.components.QuickAddCard
import com.termrunway.app.ui.components.SectionTitle
import com.termrunway.app.ui.components.TransactionIcon
import com.termrunway.app.ui.components.dateLabel
import com.termrunway.app.ui.components.moneyString
import com.termrunway.app.ui.components.todayLabel
import com.termrunway.app.ui.theme.RunwayBlue
import com.termrunway.app.ui.theme.RunwayMint
import com.termrunway.app.ui.theme.RunwayMuted
import com.termrunway.app.ui.theme.RunwayRed
import com.termrunway.app.ui.mode.TrackingMode
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import com.termrunway.app.ui.AppUiState
import com.termrunway.app.ui.components.*
import com.termrunway.app.ui.util.endOfDay

@Composable
fun PlanScreen(
    state: AppUiState,
    onSettings: () -> Unit,
    onCreateOrEdit: () -> Unit,
    onTransaction: (Transaction) -> Unit
) {
    val active = state.activePlan
    val metrics = active?.let {
        FinancialCalculator.planMetrics(it, state.plannedIncomes, state.plannedExpenses, state.transactions)
    }
    val completed = state.plans.filter { active == null || it.id != active.id }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenHeader(
                title = "Plan Tracking",
                subtitle = "Plan what will happen to your money",
                onSettings = onSettings
            )
        }

        if (active == null) {
            item { PlanEmptyCard(onCreateOrEdit) }
        } else {
            item { PlanHeroCard(active, metrics, onCreateOrEdit) }
            item { SectionTitle("Expected inputs") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AmountCard("Expected income", metrics?.totalExpectedIncomePaise ?: 0L, RunwayMint, Modifier.weight(1f))
                    AmountCard("Planned expenses", metrics?.totalPlannedExpensePaise ?: 0L, RunwayRed, Modifier.weight(1f))
                }
            }
            item { SectionTitle("Recent plan activity") }
            val planTransactions = state.transactions.filter {
                it.dateMs in active.startMs..endOfDay(active.endMs)
            }.take(5)
            if (planTransactions.isEmpty()) {
                item {
                    EmptyState(
                        "No actual activity yet",
                        "Record transactions to compare reality with your plan."
                    )
                }
            } else {
                items(planTransactions, key = { it.id }) { tx ->
                    TransactionRow(tx, { onTransaction(tx) })
                }
            }
            if (completed.isNotEmpty()) {
                item { SectionTitle("Other plans") }
                items(completed, key = { it.id }) { plan ->
                    OutlinedCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(plan.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    dateLabel(plan.startMs) + " → " + dateLabel(plan.endMs),
                                    color = RunwayMuted,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Icon(Icons.Outlined.KeyboardArrowRight, null)
                        }
                    }
                }
            }
        }
    }
}
