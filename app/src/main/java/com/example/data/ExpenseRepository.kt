package com.example.data

import android.content.Context
import android.content.Intent
import com.example.model.Budget
import com.example.model.Category
import com.example.model.DEFAULT_CATEGORIES
import com.example.model.Expense
import com.example.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class ExpenseRepository(
    private val database: AppDatabase
) {
    private val dao = database.expenseDao()

    val allExpenses: Flow<List<Expense>> = dao.getAllExpenses()
    val allCategories: Flow<List<Category>> = dao.getAllCategories()
    val allBudgets: Flow<List<Budget>> = dao.getAllBudgets()

    fun getExpensesBetween(start: Long, end: Long): Flow<List<Expense>> {
        return dao.getExpensesBetween(start, end)
    }

    suspend fun insertExpense(expense: Expense): Long = withContext(Dispatchers.IO) {
        dao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: Expense) = withContext(Dispatchers.IO) {
        dao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) = withContext(Dispatchers.IO) {
        dao.deleteExpense(expense)
    }

    suspend fun deleteExpenseById(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteExpenseById(id)
    }

    // Categories
    suspend fun insertCategory(category: Category): Long = withContext(Dispatchers.IO) {
        dao.insertCategory(category)
    }

    suspend fun updateCategory(category: Category) = withContext(Dispatchers.IO) {
        dao.updateCategory(category)
    }

    /**
     * Safely delete category. If expenses exist, returns false and does NOT delete.
     */
    suspend fun deleteCategorySafe(category: Category): Boolean = withContext(Dispatchers.IO) {
        val count = dao.countExpensesForCategory(category.name)
        if (count > 0) {
            return@withContext false
        }
        dao.deleteCategory(category)
        return@withContext true
    }

    suspend fun countExpensesForCategory(categoryName: String): Int = withContext(Dispatchers.IO) {
        dao.countExpensesForCategory(categoryName)
    }

    // Budgets
    suspend fun insertOrUpdateBudget(budget: Budget): Long = withContext(Dispatchers.IO) {
        dao.deleteBudgetForCategory(budget.categoryName)
        dao.insertBudget(budget)
    }

    suspend fun deleteBudget(budget: Budget) = withContext(Dispatchers.IO) {
        dao.deleteBudget(budget)
    }

    suspend fun deleteBudgetForCategory(categoryName: String?) = withContext(Dispatchers.IO) {
        dao.deleteBudgetForCategory(categoryName)
    }

    // Seed realistic sample data
    suspend fun seedSampleData() = withContext(Dispatchers.IO) {
        // Ensure default categories
        dao.insertCategories(DEFAULT_CATEGORIES)

        val cal = Calendar.getInstance()
        val today = cal.timeInMillis

        cal.add(Calendar.HOUR_OF_DAY, -2)
        val todayEarlier = cal.timeInMillis

        cal.timeInMillis = today
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = cal.timeInMillis

        cal.timeInMillis = today
        cal.add(Calendar.DAY_OF_YEAR, -3)
        val threeDaysAgo = cal.timeInMillis

        cal.timeInMillis = today
        cal.add(Calendar.DAY_OF_YEAR, -6)
        val thisWeekEarlier = cal.timeInMillis

        cal.timeInMillis = today
        cal.add(Calendar.DAY_OF_YEAR, -15)
        val midMonth = cal.timeInMillis

        cal.timeInMillis = today
        cal.add(Calendar.MONTH, -1)
        val lastMonth = cal.timeInMillis

        val samples = listOf(
            Expense(title = "Milk and Bread", amount = 40.0, category = "Milk and Dairy", date = today, paymentMethod = "UPI", notes = "Morning dairy purchase"),
            Expense(title = "Petrol Refill", amount = 500.0, category = "Petrol and Diesel", date = todayEarlier, paymentMethod = "UPI", notes = "Bike petrol 5L"),
            Expense(title = "Vegetables", amount = 150.0, category = "Vegetables and Fruits", date = yesterday, paymentMethod = "Cash", notes = "Tomatoes, onions, spinach"),
            Expense(title = "Electricity Bill", amount = 1200.0, category = "Electricity Bill", date = thisWeekEarlier, paymentMethod = "Debit Card", notes = "Monthly BESCOM bill"),
            Expense(title = "Mobile Recharge", amount = 299.0, category = "Mobile and Phone Bills", date = threeDaysAgo, paymentMethod = "UPI", notes = "28 days plan 1.5GB/day"),
            Expense(title = "Supermarket Groceries", amount = 850.0, category = "Groceries", date = midMonth, paymentMethod = "Credit Card", notes = "Atta, pulses, cooking oil"),
            Expense(title = "Fiber Internet", amount = 799.0, category = "Internet and Wi-Fi", date = midMonth, paymentMethod = "Bank Transfer", notes = "Airtel Xstream broadband"),
            Expense(title = "Dinner with Family", amount = 950.0, category = "Food and Restaurants", date = midMonth, paymentMethod = "Debit Card", notes = "Weekend dining"),
            Expense(title = "Movie Tickets", amount = 420.0, category = "Entertainment", date = lastMonth, paymentMethod = "UPI", notes = "2 IMAX tickets"),
            Expense(title = "Pharmacy & Medicines", amount = 380.0, category = "Medical and Healthcare", date = lastMonth, paymentMethod = "Cash", notes = "Vitamins and first aid")
        )
        dao.insertExpenses(samples)

        // Set starter overall monthly budget of ₹15,000
        dao.insertBudget(Budget(categoryName = null, monthlyLimit = 15000.0))
        // Set sample category budgets
        dao.insertBudget(Budget(categoryName = "Groceries", monthlyLimit = 4000.0))
        dao.insertBudget(Budget(categoryName = "Petrol and Diesel", monthlyLimit = 2500.0))
        dao.insertBudget(Budget(categoryName = "Food and Restaurants", monthlyLimit = 3000.0))
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        dao.clearAllExpenses()
        dao.clearAllBudgets()
    }

    // Export CSV
    fun generateCsv(expenses: List<Expense>): String {
        val sb = StringBuilder()
        sb.append("Date,Title,Category,Amount,Payment Method,Notes,Recurring\n")
        for (e in expenses) {
            val dateStr = DateUtils.formatDate(e.date)
            val cleanTitle = escapeCsv(e.title)
            val cleanCategory = escapeCsv(e.category)
            val cleanMethod = escapeCsv(e.paymentMethod)
            val cleanNotes = escapeCsv(e.notes)
            val recurring = if (e.isRecurring) e.recurringPeriod else "No"
            sb.append("$dateStr,$cleanTitle,$cleanCategory,${e.amount},$cleanMethod,$cleanNotes,$recurring\n")
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    fun shareText(context: Context, text: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(intent, title).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(chooser)
    }

    // Export JSON Backup
    suspend fun exportJsonBackup(): String = withContext(Dispatchers.IO) {
        val expenses = dao.getAllExpensesSync()
        val categories = dao.getAllCategoriesSync()
        val budgets = dao.getAllBudgetsSync()

        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val expArray = JSONArray()
        for (e in expenses) {
            val obj = JSONObject()
            obj.put("title", e.title)
            obj.put("amount", e.amount)
            obj.put("category", e.category)
            obj.put("date", e.date)
            obj.put("paymentMethod", e.paymentMethod)
            obj.put("notes", e.notes)
            obj.put("isRecurring", e.isRecurring)
            obj.put("recurringPeriod", e.recurringPeriod)
            expArray.put(obj)
        }
        root.put("expenses", expArray)

        val catArray = JSONArray()
        for (c in categories) {
            val obj = JSONObject()
            obj.put("name", c.name)
            obj.put("iconName", c.iconName)
            obj.put("colorHex", c.colorHex)
            obj.put("isDefault", c.isDefault)
            catArray.put(obj)
        }
        root.put("categories", catArray)

        val budArray = JSONArray()
        for (b in budgets) {
            val obj = JSONObject()
            obj.put("categoryName", b.categoryName ?: "")
            obj.put("monthlyLimit", b.monthlyLimit)
            budArray.put(obj)
        }
        root.put("budgets", budArray)

        return@withContext root.toString(2)
    }

    // Restore JSON Backup
    suspend fun restoreJsonBackup(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        return@withContext try {
            val root = JSONObject(jsonString)
            if (!root.has("expenses")) {
                return@withContext Result.failure(Exception("Invalid backup file: missing 'expenses' field"))
            }

            val expArray = root.getJSONArray("expenses")
            val newExpenses = mutableListOf<Expense>()
            for (i in 0 until expArray.length()) {
                val obj = expArray.getJSONObject(i)
                newExpenses.add(
                    Expense(
                        title = obj.getString("title"),
                        amount = obj.getDouble("amount"),
                        category = obj.getString("category"),
                        date = obj.getLong("date"),
                        paymentMethod = obj.optString("paymentMethod", "Cash"),
                        notes = obj.optString("notes", ""),
                        isRecurring = obj.optBoolean("isRecurring", false),
                        recurringPeriod = obj.optString("recurringPeriod", "None")
                    )
                )
            }

            if (root.has("categories")) {
                val catArray = root.getJSONArray("categories")
                val newCategories = mutableListOf<Category>()
                for (i in 0 until catArray.length()) {
                    val obj = catArray.getJSONObject(i)
                    newCategories.add(
                        Category(
                            name = obj.getString("name"),
                            iconName = obj.optString("iconName", "Category"),
                            colorHex = obj.optString("colorHex", "#10B981"),
                            isDefault = obj.optBoolean("isDefault", false)
                        )
                    )
                }
                dao.insertCategories(newCategories)
            }

            if (root.has("budgets")) {
                val budArray = root.getJSONArray("budgets")
                for (i in 0 until budArray.length()) {
                    val obj = budArray.getJSONObject(i)
                    val cat = obj.optString("categoryName", "")
                    val limit = obj.getDouble("monthlyLimit")
                    dao.insertBudget(
                        Budget(
                            categoryName = if (cat.isEmpty()) null else cat,
                            monthlyLimit = limit
                        )
                    )
                }
            }

            dao.insertExpenses(newExpenses)
            Result.success(newExpenses.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
