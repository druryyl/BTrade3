package com.elsasa.btrade3.util

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class FriendlyIdGenerator {


    fun generateCompactDateSequenceId(context: Context): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR) % 100 // Last 2 digits of year
        val month = calendar.get(Calendar.MONTH) + 1 // 1-based month
        val monthHex = Integer.toHexString(month).uppercase()
        val dateKey = "$year$monthHex"

        val sharedPref = context.getSharedPreferences("faktur_sequence", Context.MODE_PRIVATE)
        val lastDateKey = sharedPref.getString("last_date_key", "")
        val lastSequence = sharedPref.getInt("last_sequence", 0)

        val newSequence = if (lastDateKey == dateKey) lastSequence + 1 else 1

        sharedPref.edit().apply {
            putString("last_date_key", dateKey)
            putInt("last_sequence", newSequence)
            apply()
        }

        return "$dateKey-${String.format(Locale.ROOT, "%03d", newSequence)}"
    }
    // Option 1: Date-based with sequence number
    // Format: YYYYMMDD-001, YYYYMMDD-002, etc.
    fun generateDateSequenceId(context: Context): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        // Get last sequence number from SharedPreferences or database
        val sharedPref = context.getSharedPreferences("faktur_sequence", Context.MODE_PRIVATE)
        val lastDate = sharedPref.getString("last_date", "")
        val lastSequence = sharedPref.getInt("last_sequence", 0)

        val newSequence = if (lastDate == currentDate) {
            lastSequence + 1
        } else {
            1 // Reset sequence for new date
        }

        // Save new values
        with(sharedPref.edit()) {
            putString("last_date", currentDate)
            putInt("last_sequence", newSequence)
            apply()
        }

        return "$currentDate-${String.format(Locale.ROOT, "%03d", newSequence)}"
    }

    // Option 2: Prefix + Timestamp + Random suffix
    // Format: FAK-240810-A7B2
    fun generatePrefixTimestampId(): String {
        val dateFormat = SimpleDateFormat("yyMMdd", Locale.getDefault())
        val datePart = dateFormat.format(Date())
        val randomSuffix = generateRandomAlphanumeric(4)
        return "FAK-$datePart-$randomSuffix"
    }

    // Option 3: Human-readable words + numbers
    // Format: TIGER-2024-125, ZEBRA-2024-126
    fun generateWordBasedId(): String {
        val animals = listOf(
            "TIGER", "EAGLE", "SHARK", "WOLF", "BEAR", "LION", "HAWK", "FALCON",
            "PANTHER", "COBRA", "RHINO", "ZEBRA", "CHEETAH", "JAGUAR", "LYNX"
        )
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        val randomNumber = Random.nextInt(100, 999)
        val randomAnimal = animals.random()

        return "$randomAnimal-$year-$randomNumber"
    }

    // Option 4: Short alphanumeric (Base36)
    // Format: 8-character readable code like "K7M9N2P4"
    fun generateShortAlphanumeric(): String {
        val timestamp = System.currentTimeMillis()
        val random = Random.nextInt(1000, 9999)
        val combined = timestamp + random

        // Convert to base36 for shorter, readable string
        return combined.toString(36).uppercase().takeLast(8)
    }

    // Option 5: Department/Location + Sequential
    // Format: JKT-001-240810 (Jakarta office, sequence 001, date)
    fun generateLocationSequenceId(locationCode: String, context: Context): String {
        val dateFormat = SimpleDateFormat("yyMMdd", Locale.getDefault())
        val datePart = dateFormat.format(Date())

        val sharedPref = context.getSharedPreferences("location_sequence_$locationCode", Context.MODE_PRIVATE)
        val sequence = sharedPref.getInt("sequence", 0) + 1

        with(sharedPref.edit()) {
            putInt("sequence", sequence)
            apply()
        }

        return "$locationCode-${String.format("%03d", sequence)}-$datePart"
    }

    // Option 6: Human-friendly with check digit
    // Format: FK240810C7 (FK prefix, date, check digit)
    fun generateChecksumId(): String {
        val dateFormat = SimpleDateFormat("yyMMdd", Locale.getDefault())
        val datePart = dateFormat.format(Date())
        val randomPart = Random.nextInt(10, 99)

        val baseId = "FK$datePart$randomPart"
        val checkDigit = calculateCheckDigit(baseId)

        return "$baseId$checkDigit"
    }

    // Helper function for random alphanumeric
    private fun generateRandomAlphanumeric(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    // Simple check digit calculation (Luhn-like)
    private fun calculateCheckDigit(input: String): String {
        val sum = input.sumOf { char ->
            when {
                char.isDigit() -> char.digitToInt()
                char.isLetter() -> char.code - 'A'.code + 10
                else -> 0
            }
        }
        return (sum % 36).toString(36).uppercase()
    }
}