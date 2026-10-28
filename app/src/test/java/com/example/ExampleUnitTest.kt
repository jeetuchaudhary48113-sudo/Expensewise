package com.example

import com.example.model.Expense
import com.example.util.CurrencyFormatter
import com.example.util.DateFilterType
import com.example.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {

    @Test
    fun test1_expenseCalculationTotal() {
        val expenses = listOf(
            Expense(title = "Groceries", amount = 500.0, category = "Groceries", date = System.currentTimeMillis()),
            Expense(title = "Milk", amount = 50.0, category = "Milk and Dairy", date = System.currentTimeMillis()),
            Expense(title = "Petrol", amount = 1000.0, category = "Petrol and Diesel", date = System.currentTimeMillis())
        )
        val total = expenses.sumOf { it.amount }
        assertEquals(1550.0, total, 0.001)
    }

    @Test
    fun test2_categoryTotals() {
        val expenses = listOf(
            Expense(title = "Groceries", amount = 500.0, category = "Groceries", date = System.currentTimeMillis()),
            Expense(title = "Milk", amount = 50.0, category = "Milk and Dairy", date = System.currentTimeMillis()),
            Expense(title = "Petrol", amount = 1000.0, category = "Petrol and Diesel", date = System.currentTimeMillis())
        )
        val groceriesTotal = expenses.filter { it.category == "Groceries" }.sumOf { it.amount }
        val milkTotal = expenses.filter { it.category == "Milk and Dairy" }.sumOf { it.amount }
        val petrolTotal = expenses.filter { it.category == "Petrol and Diesel" }.sumOf { it.amount }

        assertEquals(500.0, groceriesTotal, 0.001)
        assertEquals(50.0, milkTotal, 0.001)
        assertEquals(1000.0, petrolTotal, 0.001)
    }

    @Test
    fun test3_budgetCalculation() {
        val monthlyBudget = 10000.0
        val actualExpenses = 6000.0
        val remainingBudget = monthlyBudget - actualExpenses
        val utilization = (actualExpenses / monthlyBudget) * 100.0

        assertEquals(4000.0, remainingBudget, 0.001)
        assertEquals(60.0, utilization, 0.001)
    }

    @Test
    fun test4_currencyFormattingIndianSystem() {
        assertEquals("₹500", CurrencyFormatter.format(500.0, "₹"))
        assertEquals("₹12,500", CurrencyFormatter.format(12500.0, "₹"))
        assertEquals("₹1,25,000", CurrencyFormatter.format(125000.0, "₹"))
        assertEquals("₹12,50,000", CurrencyFormatter.format(1250000.0, "₹"))
    }

    @Test
    fun test5_dateRangeInclusiveFiltering() {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.JANUARY, 10, 10, 0, 0)
        val timeInRange = cal.timeInMillis

        cal.set(2026, Calendar.FEBRUARY, 10, 10, 0, 0)
        val timeOutOfRange = cal.timeInMillis

        val sCal = Calendar.getInstance().apply { set(2026, Calendar.JANUARY, 1, 0, 0, 0) }
        val eCal = Calendar.getInstance().apply { set(2026, Calendar.JANUARY, 31, 23, 59, 59) }

        val range = DateUtils.getDateRange(DateFilterType.CUSTOM, sCal.timeInMillis, eCal.timeInMillis)

        assertTrue(timeInRange in range.startMillis..range.endMillis)
        assertTrue(timeOutOfRange !in range.startMillis..range.endMillis)
    }

    @Test
    fun test6_quarterRanges() {
        val q1 = DateUtils.getQuarterRange(2026, 1)
        val q2 = DateUtils.getQuarterRange(2026, 2)
        val q3 = DateUtils.getQuarterRange(2026, 3)
        val q4 = DateUtils.getQuarterRange(2026, 4)

        assertEquals("Q1 2026", q1.label)
        assertEquals("Q2 2026", q2.label)
        assertEquals("Q3 2026", q3.label)
        assertEquals("Q4 2026", q4.label)
    }
}
