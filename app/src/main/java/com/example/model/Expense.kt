package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val date: Long, // timestamp in millis
    val paymentMethod: String = "Cash", // Cash, UPI, Debit Card, Credit Card, Bank Transfer, Other
    val notes: String = "",
    val isRecurring: Boolean = false,
    val recurringPeriod: String = "None", // None, Daily, Weekly, Monthly
    val createdAt: Long = System.currentTimeMillis()
)

val PAYMENT_METHODS = listOf(
    "Cash",
    "UPI",
    "Debit Card",
    "Credit Card",
    "Bank Transfer",
    "Other"
)
