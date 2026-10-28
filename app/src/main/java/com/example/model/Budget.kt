package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryName: String?, // null = overall monthly budget
    val monthlyLimit: Double,
    val monthYear: String = "ALL" // "ALL" applies to any month by default or "YYYY-MM"
)
