package com.example.util

import java.text.DecimalFormat
import java.util.Locale

object CurrencyFormatter {

    /**
     * Formats a double amount with the chosen currency symbol and optionally Indian Numbering System
     * Examples: ₹500, ₹12,500, ₹1,25,000
     */
    fun format(amount: Double, symbol: String = "₹", useIndianGrouping: Boolean = true): String {
        val isNegative = amount < 0
        val absAmount = kotlin.math.abs(amount)
        val formattedNumber = if (useIndianGrouping) {
            formatIndian(absAmount)
        } else {
            val standardFormat = DecimalFormat("#,##0.##")
            standardFormat.format(absAmount)
        }
        val prefix = if (isNegative) "-" else ""
        return "$prefix$symbol$formattedNumber"
    }

    private fun formatIndian(amount: Double): String {
        val longVal = amount.toLong()
        val decimalPart = amount - longVal
        val hasDecimals = decimalPart >= 0.01

        val str = longVal.toString()
        val len = str.length

        val formattedInt = if (len <= 3) {
            str
        } else {
            val lastThree = str.substring(len - 3)
            val remaining = str.substring(0, len - 3)
            val sb = StringBuilder()
            var count = 0
            for (i in remaining.length - 1 downTo 0) {
                sb.append(remaining[i])
                count++
                if (count % 2 == 0 && i > 0) {
                    sb.append(',')
                }
            }
            sb.reverse().toString() + "," + lastThree
        }

        return if (hasDecimals) {
            val decStr = String.format(Locale.US, "%.2f", decimalPart).substring(1) // e.g. ".50"
            "$formattedInt$decStr"
        } else {
            formattedInt
        }
    }
}
