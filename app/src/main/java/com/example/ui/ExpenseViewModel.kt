package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import com.example.data.AppDatabase
import com.example.data.ExpenseRepository
import com.example.data.PreferencesManager
import com.example.model.Budget
import com.example.model.Category
import com.example.model.Expense
import com.example.ui.components.BarChartItem
import com.example.ui.components.CategorySpendItem
import com.example.ui.components.TrendPoint
import com.example.util.CategoryIcons
import com.example.util.DateFilterType
import com.example.util.DateRange
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CategoryReportItem(
    val category: Category,
    val totalAmount: Double,
    val count: Int,
    val percentage: Float, // 0 to 100
    val averageAmount: Double
)

data class PeriodSummary(
    val totalAmount: Double,
    val transactionCount: Int,
    val averageDailySpending: Double,
    val highestSpendingDay: Pair<String, Double>?,
    val lowestSpendingDay: Pair<String, Double>?
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = ExpenseRepository(database)
    private val prefs = PreferencesManager(application)

    val allExpenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBudgets: StateFlow<List<Budget>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Preferences
    private val _userName = MutableStateFlow(prefs.userName)
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _currencySymbol = MutableStateFlow(prefs.currencySymbol)
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.themeMode)
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _snackBarMessage = MutableSharedFlow<String>()
    val snackBarMessage: SharedFlow<String> = _snackBarMessage.asSharedFlow()

    // Filters for "All Expenses" Screen
    val allExpensesPeriodFilter = MutableStateFlow(DateFilterType.THIS_MONTH)
    val allExpensesCustomStart = MutableStateFlow(DateUtils.getDateRange(DateFilterType.THIS_MONTH).startMillis)
    val allExpensesCustomEnd = MutableStateFlow(DateUtils.getDateRange(DateFilterType.THIS_MONTH).endMillis)
    val allExpensesCategoryFilter = MutableStateFlow<String?>(null)
    val allExpensesPaymentFilter = MutableStateFlow<String?>(null)
    val allExpensesSearchQuery = MutableStateFlow("")

    // Dashboard specific period filter
    val dashboardPeriodFilter = MutableStateFlow(DateFilterType.THIS_MONTH)

    // Reports screen filters
    val reportPeriodFilter = MutableStateFlow(DateFilterType.THIS_MONTH)
    val reportCustomStart = MutableStateFlow(DateUtils.getDateRange(DateFilterType.THIS_MONTH).startMillis)
    val reportCustomEnd = MutableStateFlow(DateUtils.getDateRange(DateFilterType.THIS_MONTH).endMillis)
    val reportSelectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val reportSelectedQuarter = MutableStateFlow(1 + Calendar.getInstance().get(Calendar.MONTH) / 3)
    val reportDrilldownCategory = MutableStateFlow<String?>(null)

    init {
        // Seed initial sample data only on first app launch
        if (!prefs.hasSeededInitial) {
            viewModelScope.launch {
                repository.seedSampleData()
                prefs.hasSeededInitial = true
            }
        }
    }

    // Filtered Expenses for "All Expenses" screen
    val filteredExpenses: StateFlow<List<Expense>> = combine(
        allExpenses,
        allExpensesPeriodFilter,
        allExpensesCustomStart,
        allExpensesCustomEnd,
        allExpensesCategoryFilter,
        allExpensesPaymentFilter,
        allExpensesSearchQuery
    ) { args ->
        val list = args[0] as List<Expense>
        val period = args[1] as DateFilterType
        val cStart = args[2] as Long
        val cEnd = args[3] as Long
        val catFilter = args[4] as String?
        val payFilter = args[5] as String?
        val query = (args[6] as String).trim().lowercase()

        val range = DateUtils.getDateRange(period, cStart, cEnd)

        list.filter { expense ->
            val matchesDate = expense.date in range.startMillis..range.endMillis
            val matchesCat = catFilter == null || expense.category == catFilter
            val matchesPay = payFilter == null || expense.paymentMethod == payFilter
            val matchesQuery = query.isEmpty() ||
                expense.title.lowercase().contains(query) ||
                expense.notes.lowercase().contains(query) ||
                expense.category.lowercase().contains(query)

            matchesDate && matchesCat && matchesPay && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calculations for Dashboard Summary Cards
    val dashboardTodayTotal: StateFlow<Double> = allExpenses.combine(dashboardPeriodFilter) { list, _ ->
        val range = DateUtils.getDateRange(DateFilterType.TODAY)
        list.filter { it.date in range.startMillis..range.endMillis }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val dashboardWeekTotal: StateFlow<Double> = allExpenses.combine(dashboardPeriodFilter) { list, _ ->
        val range = DateUtils.getDateRange(DateFilterType.THIS_WEEK)
        list.filter { it.date in range.startMillis..range.endMillis }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val dashboardMonthTotal: StateFlow<Double> = allExpenses.combine(dashboardPeriodFilter) { list, _ ->
        val range = DateUtils.getDateRange(DateFilterType.THIS_MONTH)
        list.filter { it.date in range.startMillis..range.endMillis }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val dashboardYearTotal: StateFlow<Double> = allExpenses.combine(dashboardPeriodFilter) { list, _ ->
        val range = DateUtils.getDateRange(DateFilterType.THIS_YEAR)
        list.filter { it.date in range.startMillis..range.endMillis }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val dashboardSelectedPeriodTotal: StateFlow<Double> = combine(
        allExpenses,
        dashboardPeriodFilter
    ) { list, filter ->
        val range = DateUtils.getDateRange(filter)
        list.filter { it.date in range.startMillis..range.endMillis }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Overall Monthly Budget calculation
    val overallMonthlyBudget: StateFlow<Double> = allBudgets.combine(allExpenses) { budgets, _ ->
        budgets.find { it.categoryName == null }?.monthlyLimit ?: 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Remaining budget = budget - month total
    val remainingMonthlyBudget: StateFlow<Double> = combine(
        overallMonthlyBudget,
        dashboardMonthTotal
    ) { budget, spent ->
        budget - spent
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Budget utilization = (spent / budget) * 100
    val budgetUtilizationPercentage: StateFlow<Float> = combine(
        overallMonthlyBudget,
        dashboardMonthTotal
    ) { budget, spent ->
        if (budget > 0.0) {
            ((spent / budget) * 100.0).toFloat()
        } else 0f
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    // Category Reports for Reports Screen
    fun getExpensesForReportRange(range: DateRange): List<Expense> {
        val list = allExpenses.value
        return list.filter { it.date in range.startMillis..range.endMillis }
    }

    fun computeCategoryReports(expenses: List<Expense>, categories: List<Category>): List<CategoryReportItem> {
        val total = expenses.sumOf { it.amount }
        val categoryMap = categories.associateBy { it.name }

        val grouped = expenses.groupBy { it.category }
        val result = mutableListOf<CategoryReportItem>()

        for ((catName, expList) in grouped) {
            val catTotal = expList.sumOf { it.amount }
            val count = expList.size
            val percentage = if (total > 0.0) ((catTotal / total) * 100.0).toFloat() else 0f
            val avg = if (count > 0) catTotal / count else 0.0
            val catEntity = categoryMap[catName] ?: Category(name = catName, isDefault = false)

            result.add(
                CategoryReportItem(
                    category = catEntity,
                    totalAmount = catTotal,
                    count = count,
                    percentage = percentage,
                    averageAmount = avg
                )
            )
        }

        return result.sortedByDescending { it.totalAmount }
    }

    fun computePeriodSummary(expenses: List<Expense>, range: DateRange): PeriodSummary {
        val total = expenses.sumOf { it.amount }
        val count = expenses.size

        // Daily average
        val dayCount = kotlin.math.max(
            1,
            ((range.endMillis - range.startMillis) / (24 * 60 * 60 * 1000L)).toInt() + 1
        )
        val avgDaily = total / dayCount

        // Group by day for highest and lowest
        val dayFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        val dayGrouped = expenses.groupBy { dayFormat.format(Date(it.date)) }
        val dailyTotals = dayGrouped.mapValues { entry -> entry.value.sumOf { it.amount } }

        val highest = dailyTotals.maxByOrNull { it.value }?.toPair()
        val lowest = if (dailyTotals.isNotEmpty()) dailyTotals.minByOrNull { it.value }?.toPair() else null

        return PeriodSummary(
            totalAmount = total,
            transactionCount = count,
            averageDailySpending = avgDaily,
            highestSpendingDay = highest,
            lowestSpendingDay = lowest
        )
    }

    // Chart Data Generators
    fun getCategoryDonutData(categoryReports: List<CategoryReportItem>): List<CategorySpendItem> {
        return categoryReports.map { item ->
            CategorySpendItem(
                categoryName = item.category.name,
                amount = item.totalAmount,
                percentage = item.percentage,
                color = CategoryIcons.parseColor(item.category.colorHex)
            )
        }
    }

    fun getMonthlyBarData(expenses: List<Expense>): List<BarChartItem> {
        val cal = Calendar.getInstance()
        val items = mutableListOf<BarChartItem>()
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        // Past 6 months
        for (i in 5 downTo 0) {
            val c = cal.clone() as Calendar
            c.add(Calendar.MONTH, -i)
            c.set(Calendar.DAY_OF_MONTH, 1)
            val start = DateUtils.getStartOfDay(c)
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
            val end = DateUtils.getEndOfDay(c)

            val monthExpenses = expenses.filter { it.date in start..end }
            val amount = monthExpenses.sumOf { it.amount }
            items.add(BarChartItem(label = monthFormat.format(Date(start)), amount = amount))
        }

        return items
    }

    fun getDailyTrendData(expenses: List<Expense>, daysCount: Int = 7): List<TrendPoint> {
        val cal = Calendar.getInstance()
        val points = mutableListOf<TrendPoint>()
        val dayFormat = SimpleDateFormat("dd", Locale.getDefault())

        for (i in (daysCount - 1) downTo 0) {
            val c = cal.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, -i)
            val start = DateUtils.getStartOfDay(c)
            val end = DateUtils.getEndOfDay(c)

            val dayTotal = expenses.filter { it.date in start..end }.sumOf { it.amount }
            points.add(TrendPoint(label = dayFormat.format(Date(start)), amount = dayTotal))
        }

        return points
    }

    // Actions
    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            repository.insertExpense(expense)
            _snackBarMessage.emit("Expense '${expense.title}' added successfully!")
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            repository.updateExpense(expense)
            _snackBarMessage.emit("Expense updated successfully!")
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _snackBarMessage.emit("Expense '${expense.title}' deleted")
        }
    }

    fun addCategory(name: String, iconName: String, colorHex: String) {
        viewModelScope.launch {
            val existing = allCategories.value.find { it.name.equals(name.trim(), ignoreCase = true) }
            if (existing != null) {
                _snackBarMessage.emit("Category '$name' already exists")
                return@launch
            }
            repository.insertCategory(
                Category(
                    name = name.trim(),
                    iconName = iconName,
                    colorHex = colorHex,
                    isDefault = false
                )
            )
            _snackBarMessage.emit("Category '$name' added!")
        }
    }

    fun renameCategory(category: Category, newName: String) {
        viewModelScope.launch {
            if (newName.isBlank()) return@launch
            repository.updateCategory(category.copy(name = newName.trim()))
            _snackBarMessage.emit("Category renamed to '$newName'")
        }
    }

    fun deleteCategory(category: Category, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val deleted = repository.deleteCategorySafe(category)
            if (deleted) {
                _snackBarMessage.emit("Category '${category.name}' deleted")
                onResult(true, "Category deleted")
            } else {
                val error = "Cannot delete '${category.name}' because existing expenses are linked to it."
                _snackBarMessage.emit(error)
                onResult(false, error)
            }
        }
    }

    fun setOverallMonthlyBudget(amount: Double) {
        viewModelScope.launch {
            repository.insertOrUpdateBudget(
                Budget(categoryName = null, monthlyLimit = amount)
            )
            _snackBarMessage.emit("Monthly budget set to $amount")
        }
    }

    fun setCategoryBudget(categoryName: String, amount: Double) {
        viewModelScope.launch {
            repository.insertOrUpdateBudget(
                Budget(categoryName = categoryName, monthlyLimit = amount)
            )
            _snackBarMessage.emit("Budget for '$categoryName' updated")
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
            _snackBarMessage.emit("Budget removed")
        }
    }

    fun updateSettings(name: String, currency: String, theme: String) {
        prefs.userName = name
        prefs.currencySymbol = currency
        prefs.themeMode = theme
        _userName.value = name
        _currencySymbol.value = currency
        _themeMode.value = theme
    }

    fun exportCsv(context: Context, expensesToExport: List<Expense>) {
        viewModelScope.launch {
            val csv = repository.generateCsv(expensesToExport)
            repository.shareText(context, csv, "ExpenseWise_Report.csv")
        }
    }

    fun exportJsonBackup(context: Context) {
        viewModelScope.launch {
            val json = repository.exportJsonBackup()
            repository.shareText(context, json, "ExpenseWise_Backup.json")
        }
    }

    fun restoreJsonBackup(jsonString: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.restoreJsonBackup(jsonString)
            if (res.isSuccess) {
                val count = res.getOrNull() ?: 0
                _snackBarMessage.emit("Successfully restored $count expenses!")
                onComplete(true, "Restored $count expenses")
            } else {
                val msg = res.exceptionOrNull()?.message ?: "Restore failed"
                _snackBarMessage.emit("Error: $msg")
                onComplete(false, msg)
            }
        }
    }

    fun populateSampleData() {
        viewModelScope.launch {
            repository.seedSampleData()
            _snackBarMessage.emit("Sample expenses loaded successfully!")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _snackBarMessage.emit("All expenses and budgets reset")
        }
    }

    fun getApkSize(context: Context): String {
        return try {
            val srcFile = File(context.applicationInfo.sourceDir)
            if (srcFile.exists()) {
                val mb = srcFile.length() / (1024.0 * 1024.0)
                String.format(Locale.US, "%.1f MB", mb)
            } else {
                "~28 MB"
            }
        } catch (_: Exception) {
            "~28 MB"
        }
    }

    fun shareApk(context: Context) {
        viewModelScope.launch {
            try {
                val srcFile = File(context.applicationInfo.sourceDir)
                if (!srcFile.exists()) {
                    Toast.makeText(context, "APK binary not accessible", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val cacheApk = File(context.cacheDir, "ExpenseWise.apk")
                srcFile.copyTo(cacheApk, overwrite = true)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    cacheApk
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.android.package-archive"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "ExpenseWise Android App APK")
                    putExtra(Intent.EXTRA_TEXT, "Here is the ExpenseWise Android App APK file.")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                val chooser = Intent.createChooser(intent, "Download / Share ExpenseWise APK").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(chooser)
                _snackBarMessage.emit("Opening APK export & share options...")
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to share APK: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
