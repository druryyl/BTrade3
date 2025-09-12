package com.elsasa.btrade3.util

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.ConfigurationCompat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Gets the device's current locale from system settings
 * @param context Android context
 * @return Current system locale
 */
fun getSystemLocale(context: Context): Locale {
    val configuration = context.resources.configuration
    return ConfigurationCompat.getLocales(configuration)[0] ?: Locale.getDefault()
}

/**
 * Composable function to get weekday from date string using device locale
 * @param dateString Date string in yyyy-MM-dd format
 * @param style Text style for weekday name (default: FULL)
 * @return Weekday name in device's locale
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun getWeekdayWithDeviceLocale(
    dateString: String,
    style: TextStyle = TextStyle.SHORT
): String {
    val context = LocalContext.current
    val deviceLocale = getSystemLocale(context)
    return getWeekdayFromDateString(dateString, deviceLocale, style)
}

/**
 * Gets the weekday name from a date string in yyyy-MM-dd format
 *
 * @param dateString Date string in yyyy-MM-dd format (e.g., "2024-03-15")
 * @param locale Locale for weekday name (default: system default)
 * @param style Text style for weekday name (default: FULL for complete name)
 * @return Weekday name (e.g., "Friday", "Fri", etc.)
 * @throws Exception if date string is invalid or null
 */
@RequiresApi(Build.VERSION_CODES.O)
fun getWeekdayFromDateString(
    dateString: String?,
    locale: Locale = Locale.getDefault(),
    style: TextStyle = TextStyle.SHORT
): String {
    if (dateString.isNullOrBlank()) {
        throw IllegalArgumentException("Date string cannot be null or empty")
    }

    return try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = LocalDate.parse(dateString, formatter)
        date.dayOfWeek.getDisplayName(style, locale)
    } catch (e: Exception) {
        throw IllegalArgumentException("Invalid date format. Expected yyyy-MM-dd, got: $dateString", e)
    }
}