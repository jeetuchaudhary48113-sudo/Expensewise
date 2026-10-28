package com.example.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("expensewise_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
        private const val KEY_THEME_MODE = "theme_mode" // "System", "Light", "Dark"
        private const val KEY_INDIAN_FORMAT = "indian_format"
        private const val KEY_HAS_SEEDED_INITIAL = "has_seeded_initial"
    }

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "Jeetu") ?: "Jeetu"
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var currencySymbol: String
        get() = prefs.getString(KEY_CURRENCY_SYMBOL, "₹") ?: "₹"
        set(value) = prefs.edit().putString(KEY_CURRENCY_SYMBOL, value).apply()

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, "System") ?: "System"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    var useIndianFormat: Boolean
        get() = prefs.getBoolean(KEY_INDIAN_FORMAT, true)
        set(value) = prefs.edit().putBoolean(KEY_INDIAN_FORMAT, value).apply()

    var hasSeededInitial: Boolean
        get() = prefs.getBoolean(KEY_HAS_SEEDED_INITIAL, false)
        set(value) = prefs.edit().putBoolean(KEY_HAS_SEEDED_INITIAL, value).apply()
}
