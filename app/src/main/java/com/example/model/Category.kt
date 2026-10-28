package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconName: String = "Category",
    val colorHex: String = "#10B981",
    val isDefault: Boolean = false
)

val DEFAULT_CATEGORIES = listOf(
    Category(name = "Groceries", iconName = "ShoppingCart", colorHex = "#10B981", isDefault = true),
    Category(name = "Milk and Dairy", iconName = "LocalDrink", colorHex = "#3B82F6", isDefault = true),
    Category(name = "Vegetables and Fruits", iconName = "Eco", colorHex = "#22C55E", isDefault = true),
    Category(name = "Petrol and Diesel", iconName = "LocalGasStation", colorHex = "#F59E0B", isDefault = true),
    Category(name = "Electricity Bill", iconName = "Bolt", colorHex = "#EAB308", isDefault = true),
    Category(name = "Water Bill", iconName = "WaterDrop", colorHex = "#06B6D4", isDefault = true),
    Category(name = "Mobile and Phone Bills", iconName = "PhoneAndroid", colorHex = "#8B5CF6", isDefault = true),
    Category(name = "Internet and Wi-Fi", iconName = "Wifi", colorHex = "#6366F1", isDefault = true),
    Category(name = "House Rent", iconName = "Home", colorHex = "#EC4899", isDefault = true),
    Category(name = "Transportation", iconName = "DirectionsBus", colorHex = "#14B8A6", isDefault = true),
    Category(name = "Medical and Healthcare", iconName = "LocalHospital", colorHex = "#EF4444", isDefault = true),
    Category(name = "Education", iconName = "School", colorHex = "#84CC16", isDefault = true),
    Category(name = "Shopping", iconName = "ShoppingBag", colorHex = "#F97316", isDefault = true),
    Category(name = "Food and Restaurants", iconName = "Restaurant", colorHex = "#D97706", isDefault = true),
    Category(name = "Entertainment", iconName = "Movie", colorHex = "#A855F7", isDefault = true),
    Category(name = "Travel", iconName = "Flight", colorHex = "#0284C7", isDefault = true),
    Category(name = "Household Expenses", iconName = "Weekend", colorHex = "#64748B", isDefault = true),
    Category(name = "Insurance", iconName = "Security", colorHex = "#0D9488", isDefault = true),
    Category(name = "Subscriptions", iconName = "Subscriptions", colorHex = "#7C3AED", isDefault = true),
    Category(name = "Other Expenses", iconName = "MoreHoriz", colorHex = "#94A3B8", isDefault = true)
)
