package com.termrunway.app.ui

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.termrunway.app.data.BackupManager
import com.termrunway.app.data.Category
import com.termrunway.app.data.CategoryType
import com.termrunway.app.data.FinancialPlan
import com.termrunway.app.data.PlannedExpense
import com.termrunway.app.data.PlannedIncome
import com.termrunway.app.data.TermRunwayRepository
import com.termrunway.app.data.Transaction
import com.termrunway.app.data.TransactionType
import com.termrunway.app.domain.FinancialCalculator
import com.termrunway.app.ui.mode.TrackingMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val android.content.Context.termRunwayDataStore by preferencesDataStore(name = "termrunway_settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

private data class StoredPreferences(
    val name: String,
    val themeMode: ThemeMode,
    val trackingMode: TrackingMode,
    val onboardingCompleted: Boolean
)

data class AppUiState(
    val name: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val trackingMode: TrackingMode = TrackingMode.DAILY,
    val onboardingCompleted: Boolean = false,
    val preferencesLoaded: Boolean = false,
    val dataLoaded: Boolean = false,
    val transactions: List<Transaction> = emptyList(),
    val plans: List<FinancialPlan> = emptyList(),
    val activePlan: FinancialPlan? = null,
    val plannedIncomes: List<PlannedIncome> = emptyList(),
    val plannedExpenses: List<PlannedExpense> = emptyList(),
    val categories: List<Category> = emptyList(),
    val loading: Boolean = true,
    val busy: Boolean = false,
    val message: String? = null
) {
    val incomeCategories get() = categories.filter { it.type == CategoryType.INCOME }
    val expenseCategories get() = categories.filter { it.type == CategoryType.EXPENSE }
    val balancePaise get() = FinancialCalculator.balance(transactions)
    val totalIncomePaise get() = FinancialCalculator.totalByType(transactions, TransactionType.INCOME)
    val totalExpensePaise get() = FinancialCalculator.totalByType(transactions, TransactionType.EXPENSE)
    val todayIncomePaise get() = FinancialCalculator.dayIncome(transactions, System.currentTimeMillis())
    val todayExpensePaise get() = FinancialCalculator.dayExpense(transactions, System.currentTimeMillis())
}

class TermRunwayViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = TermRunwayRepository(app)
    private val preferences = app.termRunwayDataStore
    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.data.map { prefs ->
                val storedName = prefs[NAME_KEY].orEmpty()
                val onboardingDone = prefs[ONBOARDING_KEY] ?: storedName.isNotBlank()
                StoredPreferences(
                    name = storedName,
                    themeMode = when (prefs[THEME_KEY]) {
                        "light" -> ThemeMode.LIGHT
                        "dark" -> ThemeMode.DARK
                        else -> ThemeMode.SYSTEM
                    },
                    trackingMode = TrackingMode.fromStorageValue(prefs[TRACKING_MODE_KEY]),
                    onboardingCompleted = onboardingDone
                )
            }.collect { stored ->
                _state.update {
                    it.copy(
                        name = stored.name,
                        themeMode = stored.themeMode,
                        trackingMode = stored.trackingMode,
                        onboardingCompleted = stored.onboardingCompleted,
                        preferencesLoaded = true
                    )
                }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            runCatching {
                val tx = repository.listTransactions()
                val plans = repository.listPlans()
                val active = plans.firstOrNull { isActiveOrUpcoming(it) } ?: plans.firstOrNull()
                val incomes = active?.let { repository.planIncome(it.id) }.orEmpty()
                val expenses = active?.let { repository.planExpenses(it.id) }.orEmpty()
                val categories = repository.listCategories()
                _state.update {
                    it.copy(
                        transactions = tx,
                        plans = plans,
                        activePlan = active,
                        plannedIncomes = incomes,
                        plannedExpenses = expenses,
                        categories = categories,
                        loading = false,
                        dataLoaded = true
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        loading = false,
                        dataLoaded = true,
                        message = error.userMessage()
                    )
                }
            }
        }
    }

    fun completeOnboarding(value: String) {
        val clean = value.trim()
        if (clean.isEmpty()) return
        _state.update { it.copy(name = clean, onboardingCompleted = true) }
        viewModelScope.launch {
            preferences.edit {
                it[NAME_KEY] = clean.take(60)
                it[ONBOARDING_KEY] = true
            }
        }
    }

    fun setName(value: String) {
        val clean = value.trim()
        if (clean.isEmpty()) return
        _state.update { it.copy(name = clean) }
        viewModelScope.launch {
            preferences.edit { it[NAME_KEY] = clean.take(60) }
        }
    }

    fun setTheme(mode: ThemeMode) {
        _state.update { it.copy(themeMode = mode) }
        viewModelScope.launch {
            preferences.edit { it[THEME_KEY] = mode.name.lowercase() }
        }
    }

    fun setTrackingMode(mode: TrackingMode) {
        if (_state.value.trackingMode == mode) return

        _state.update { it.copy(trackingMode = mode) }
        viewModelScope.launch {
            preferences.edit { it[TRACKING_MODE_KEY] = mode.storageValue }
        }
    }

    fun addTransaction(transaction: Transaction, onDone: (() -> Unit)? = null) {
        if (transaction.amountPaise <= 0) {
            _state.update { it.copy(message = "Enter an amount greater than ₹0.") }
            return
        }
        if (transaction.category.isBlank()) {
            _state.update { it.copy(message = "Choose a category.") }
            return
        }
        if (transaction.dateMs > startOfToday() + 86_400_000L - 1L) {
            _state.update {
                it.copy(message = "Actual transactions can only be today or an earlier date. Use Plan mode for future money.")
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            runCatching { repository.insertTransaction(transaction) }
                .onSuccess {
                    refresh()
                    onDone?.invoke()
                    _state.update { it.copy(busy = false, message = "Transaction added.") }
                }
                .onFailure { error ->
                    _state.update { it.copy(busy = false, message = error.userMessage()) }
                }
        }
    }

    fun updateTransaction(transaction: Transaction, onDone: (() -> Unit)? = null) {
        if (transaction.amountPaise <= 0) {
            _state.update { it.copy(message = "Enter an amount greater than ₹0.") }
            return
        }
        if (transaction.dateMs > startOfToday() + 86_400_000L - 1L) {
            _state.update {
                it.copy(message = "Actual transactions can only be today or an earlier date. Use Plan mode for future money.")
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            runCatching { repository.updateTransaction(transaction) }
                .onSuccess {
                    refresh()
                    onDone?.invoke()
                    _state.update { it.copy(busy = false, message = "Transaction updated.") }
                }
                .onFailure { error ->
                    _state.update { it.copy(busy = false, message = error.userMessage()) }
                }
        }
    }

    fun deleteTransaction(id: Long, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            runCatching { repository.deleteTransaction(id) }
                .onSuccess {
                    refresh()
                    onDone?.invoke()
                    _state.update { it.copy(busy = false, message = "Transaction deleted.") }
                }
                .onFailure { error ->
                    _state.update { it.copy(busy = false, message = error.userMessage()) }
                }
        }
    }

    fun savePlan(
        existingId: Long?,
        name: String,
        startMs: Long,
        endMs: Long,
        startingMoneyPaise: Long,
        incomes: List<PlannedIncome>,
        expenses: List<PlannedExpense>,
        onDone: (() -> Unit)? = null
    ) {
        val cleanName = name.trim()
        when {
            cleanName.isEmpty() -> {
                _state.update { it.copy(message = "Give your plan a name.") }
                return
            }
            endMs < startMs -> {
                _state.update { it.copy(message = "End date must be on or after the start date.") }
                return
            }
            startingMoneyPaise < 0L || incomes.any { it.amountPaise < 0 } || expenses.any { it.amountPaise < 0 } -> {
                _state.update { it.copy(message = "Amounts cannot be negative.") }
                return
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            runCatching {
                val plan = FinancialPlan(
                    id = existingId ?: 0L,
                    name = cleanName.take(60),
                    startMs = startMs,
                    endMs = endMs,
                    startingMoneyPaise = startingMoneyPaise
                )
                if (existingId == null) {
                    repository.createPlan(plan, incomes, expenses)
                } else {
                    repository.replacePlan(plan, incomes, expenses)
                }
            }.onSuccess {
                refresh()
                onDone?.invoke()
                _state.update { it.copy(busy = false, message = "Plan saved.") }
            }.onFailure { error ->
                _state.update { it.copy(busy = false, message = error.userMessage()) }
            }
        }
    }

    fun deletePlan(id: Long, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            runCatching { repository.deletePlan(id) }
                .onSuccess {
                    refresh()
                    onDone?.invoke()
                    _state.update { it.copy(busy = false, message = "Plan deleted.") }
                }
                .onFailure { error -> _state.update { it.copy(busy = false, message = error.userMessage()) } }
        }
    }

    fun addCategory(name: String, type: CategoryType) {
        viewModelScope.launch {
            runCatching { repository.addCategory(name, type) }
                .onSuccess { refresh(); _state.update { it.copy(message = "Category added.") } }
                .onFailure { error -> _state.update { it.copy(message = error.userMessage()) } }
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            runCatching { repository.deleteCustomCategory(id) }
                .onSuccess { refresh(); _state.update { it.copy(message = "Custom category removed.") } }
                .onFailure { error -> _state.update { it.copy(message = error.userMessage()) } }
        }
    }

    fun exportBackup(
        resolver: android.content.ContentResolver,
        uri: android.net.Uri,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            runCatching {
                val snapshot = repository.snapshot(_state.value.name, _state.value.themeMode.name.lowercase())
                BackupManager.write(snapshot, resolver, uri)
            }.onSuccess {
                _state.update { it.copy(busy = false, message = "Backup exported.") }
                onDone()
            }.onFailure { error ->
                _state.update { it.copy(busy = false, message = error.userMessage()) }
            }
        }
    }

    fun restoreBackup(
        resolver: android.content.ContentResolver,
        uri: android.net.Uri,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            runCatching {
                val snapshot = BackupManager.read(resolver, uri)
                repository.restore(snapshot)
                preferences.edit {
                    it[NAME_KEY] = snapshot.name
                    it[THEME_KEY] = snapshot.themeMode
                }
            }.onSuccess {
                refresh()
                _state.update { it.copy(busy = false, message = "Backup restored.") }
                onDone()
            }.onFailure { error ->
                _state.update { it.copy(busy = false, message = "Restore failed: ${error.userMessage()}") }
            }
        }
    }

    fun clearAll(onDone: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            runCatching {
                repository.clearAll()
                preferences.edit { prefs ->
                    prefs.remove(NAME_KEY)
                    prefs[THEME_KEY] = "system"
                    prefs.remove(TRACKING_MODE_KEY)
                    prefs.remove(ONBOARDING_KEY)
                }
            }.onSuccess {
                refresh()
                _state.update {
                    it.copy(
                        busy = false,
                        name = "",
                        onboardingCompleted = false,
                        message = "All local data cleared."
                    )
                }
                onDone()
            }.onFailure { error ->
                _state.update { it.copy(busy = false, message = error.userMessage()) }
            }
        }
    }

    fun dismissMessage() {
        _state.update { it.copy(message = null) }
    }

    fun planMetrics(): com.termrunway.app.data.PlanMetrics? {
        val plan = _state.value.activePlan ?: return null
        return FinancialCalculator.planMetrics(
            plan = plan,
            plannedIncome = _state.value.plannedIncomes,
            plannedExpenses = _state.value.plannedExpenses,
            transactions = _state.value.transactions
        )
    }

    suspend fun loadPlanDetails(planId: Long): Pair<List<PlannedIncome>, List<PlannedExpense>> =
        repository.planIncome(planId) to repository.planExpenses(planId)

    private fun isActiveOrUpcoming(plan: FinancialPlan): Boolean =
        plan.endMs >= startOfToday() || plan.startMs >= startOfToday()

    private fun startOfToday(): Long = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun Throwable.userMessage(): String =
        message?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again."

    companion object {
        private val NAME_KEY = stringPreferencesKey("name")
        private val THEME_KEY = stringPreferencesKey("theme")
        private val TRACKING_MODE_KEY = stringPreferencesKey("tracking_mode")
        private val ONBOARDING_KEY = booleanPreferencesKey("onboarding_completed")
    }
}
