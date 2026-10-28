package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DateFilterType(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    PREVIOUS_MONTH("Previous Month"),
    THIS_QUARTER("This Quarter"),
    PREVIOUS_QUARTER("Previous Quarter"),
    THIS_YEAR("This Year"),
    PREVIOUS_YEAR("Previous Year"),
    CUSTOM("Custom Range")
}

data class DateRange(
    val startMillis: Long,
    val endMillis: Long,
    val label: String
)

object DateUtils {

    fun getStartOfDay(calendar: Calendar): Long {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfDay(calendar: Calendar): Long {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun getDateRange(
        filter: DateFilterType,
        customStart: Long = System.currentTimeMillis(),
        customEnd: Long = System.currentTimeMillis()
    ): DateRange {
        val now = Calendar.getInstance()

        return when (filter) {
            DateFilterType.TODAY -> {
                DateRange(getStartOfDay(now), getEndOfDay(now), "Today")
            }
            DateFilterType.YESTERDAY -> {
                val cal = now.clone() as Calendar
                cal.add(Calendar.DAY_OF_YEAR, -1)
                DateRange(getStartOfDay(cal), getEndOfDay(cal), "Yesterday")
            }
            DateFilterType.THIS_WEEK -> {
                val cal = now.clone() as Calendar
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                val start = getStartOfDay(cal)
                cal.add(Calendar.DAY_OF_WEEK, 6)
                val end = getEndOfDay(cal)
                DateRange(start, end, "This Week")
            }
            DateFilterType.THIS_MONTH -> {
                val cal = now.clone() as Calendar
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = getStartOfDay(cal)
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                val end = getEndOfDay(cal)
                DateRange(start, end, "This Month")
            }
            DateFilterType.PREVIOUS_MONTH -> {
                val cal = now.clone() as Calendar
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = getStartOfDay(cal)
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                val end = getEndOfDay(cal)
                DateRange(start, end, "Previous Month")
            }
            DateFilterType.THIS_QUARTER -> {
                val currentMonth = now.get(Calendar.MONTH) // 0-based
                val quarter = currentMonth / 3
                val startMonth = quarter * 3
                val endMonth = startMonth + 2

                val startCal = now.clone() as Calendar
                startCal.set(Calendar.MONTH, startMonth)
                startCal.set(Calendar.DAY_OF_MONTH, 1)

                val endCal = now.clone() as Calendar
                endCal.set(Calendar.MONTH, endMonth)
                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH))

                val qName = "Q${quarter + 1}"
                DateRange(getStartOfDay(startCal), getEndOfDay(endCal), "$qName (This Quarter)")
            }
            DateFilterType.PREVIOUS_QUARTER -> {
                val cal = now.clone() as Calendar
                var quarter = cal.get(Calendar.MONTH) / 3 - 1
                if (quarter < 0) {
                    quarter = 3
                    cal.add(Calendar.YEAR, -1)
                }
                val startMonth = quarter * 3
                val endMonth = startMonth + 2

                cal.set(Calendar.MONTH, startMonth)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = getStartOfDay(cal)

                cal.set(Calendar.MONTH, endMonth)
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                val end = getEndOfDay(cal)

                DateRange(start, end, "Q${quarter + 1} (Previous Quarter)")
            }
            DateFilterType.THIS_YEAR -> {
                val cal = now.clone() as Calendar
                cal.set(Calendar.DAY_OF_YEAR, 1)
                val start = getStartOfDay(cal)
                cal.set(Calendar.MONTH, Calendar.DECEMBER)
                cal.set(Calendar.DAY_OF_MONTH, 31)
                val end = getEndOfDay(cal)
                DateRange(start, end, "${now.get(Calendar.YEAR)} (This Year)")
            }
            DateFilterType.PREVIOUS_YEAR -> {
                val cal = now.clone() as Calendar
                cal.add(Calendar.YEAR, -1)
                val prevYear = cal.get(Calendar.YEAR)
                cal.set(Calendar.DAY_OF_YEAR, 1)
                val start = getStartOfDay(cal)
                cal.set(Calendar.MONTH, Calendar.DECEMBER)
                cal.set(Calendar.DAY_OF_MONTH, 31)
                val end = getEndOfDay(cal)
                DateRange(start, end, "$prevYear (Previous Year)")
            }
            DateFilterType.CUSTOM -> {
                val sCal = Calendar.getInstance().apply { timeInMillis = customStart }
                val eCal = Calendar.getInstance().apply { timeInMillis = customEnd }
                val start = getStartOfDay(sCal)
                val end = getEndOfDay(eCal)
                DateRange(start, end, "${formatShortDate(start)} - ${formatShortDate(end)}")
            }
        }
    }

    fun getQuarterRange(year: Int, quarter: Int): DateRange {
        val startMonth = (quarter - 1) * 3
        val endMonth = startMonth + 2

        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, startMonth)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val start = getStartOfDay(cal)

        cal.set(Calendar.MONTH, endMonth)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val end = getEndOfDay(cal)

        return DateRange(start, end, "Q$quarter $year")
    }

    fun getMonthRange(year: Int, monthIndex: Int): DateRange {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthIndex)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val start = getStartOfDay(cal)

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val end = getEndOfDay(cal)

        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(start))
        return DateRange(start, end, monthName)
    }

    fun formatDate(millis: Long): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
    }

    fun formatShortDate(millis: Long): String {
        return SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(millis))
    }

    fun formatFullDateTime(millis: Long): String {
        return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(millis))
    }

    fun formatMonthYear(millis: Long): String {
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(millis))
    }

    fun formatDayName(millis: Long): String {
        return SimpleDateFormat("EEE", Locale.getDefault()).format(Date(millis))
    }

    fun getCurrentMonthKey(): String {
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }
}
