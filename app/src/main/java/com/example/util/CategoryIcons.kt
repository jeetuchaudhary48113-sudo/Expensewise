package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIcons {
    val AVAILABLE_ICONS: Map<String, ImageVector> = mapOf(
        "ShoppingCart" to Icons.Default.ShoppingCart,
        "LocalDrink" to Icons.Default.LocalDrink,
        "Eco" to Icons.Default.Eco,
        "LocalGasStation" to Icons.Default.LocalGasStation,
        "Bolt" to Icons.Default.Bolt,
        "WaterDrop" to Icons.Default.WaterDrop,
        "PhoneAndroid" to Icons.Default.PhoneAndroid,
        "Wifi" to Icons.Default.Wifi,
        "Home" to Icons.Default.Home,
        "DirectionsBus" to Icons.Default.DirectionsBus,
        "LocalHospital" to Icons.Default.LocalHospital,
        "School" to Icons.Default.School,
        "ShoppingBag" to Icons.Default.ShoppingBag,
        "Restaurant" to Icons.Default.Restaurant,
        "Movie" to Icons.Default.Movie,
        "Flight" to Icons.Default.Flight,
        "Weekend" to Icons.Default.Weekend,
        "Security" to Icons.Default.Security,
        "Subscriptions" to Icons.Default.Subscriptions,
        "Category" to Icons.Default.Category,
        "MoreHoriz" to Icons.Default.MoreHoriz
    )

    val PRESET_COLORS = listOf(
        "#10B981", // Emerald
        "#3B82F6", // Blue
        "#22C55E", // Green
        "#F59E0B", // Amber
        "#EAB308", // Yellow
        "#06B6D4", // Cyan
        "#8B5CF6", // Purple
        "#6366F1", // Indigo
        "#EC4899", // Pink
        "#14B8A6", // Teal
        "#EF4444", // Red
        "#84CC16", // Lime
        "#F97316", // Orange
        "#D97706", // Dark Amber
        "#A855F7", // Violet
        "#0284C7", // Sky Blue
        "#64748B", // Slate
        "#0D9488", // Deep Teal
        "#7C3AED", // Deep Purple
        "#94A3B8"  // Cool Grey
    )

    fun getIcon(iconName: String): ImageVector {
        return AVAILABLE_ICONS[iconName] ?: Icons.Default.Category
    }

    fun parseColor(hex: String, fallback: Color = Color(0xFF10B981)): Color {
        return try {
            val cleanHex = if (hex.startsWith("#")) hex.substring(1) else hex
            val colorInt = cleanHex.toLong(16)
            if (cleanHex.length == 6) {
                Color(0xFF000000 or colorInt)
            } else if (cleanHex.length == 8) {
                Color(colorInt)
            } else {
                fallback
            }
        } catch (_: Exception) {
            fallback
        }
    }
}
