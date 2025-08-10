package com.elsasa.btrade3.util

import android.content.Context
import java.security.SecureRandom

object UlidHelper {
    private const val ENCODING_CHARS = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"
    private const val ENCODING_LENGTH = 32
    private const val TIME_LENGTH = 10
    private const val RANDOM_LENGTH = 16
    private val secureRandom = SecureRandom()

    /**
     * Generates a new ULID string
     */
    fun generate(): String {
        val timePart = getTimePart()
        val randomPart = getRandomPart()
        return timePart + randomPart
    }

    private fun getTimePart(): String {
        val time = System.currentTimeMillis()
        val chars = CharArray(TIME_LENGTH)

        var remaining = time
        for (i in TIME_LENGTH - 1 downTo 0) {
            val mod = remaining % ENCODING_LENGTH
            chars[i] = ENCODING_CHARS[mod.toInt()]
            remaining = remaining / ENCODING_LENGTH  // FIX: Remove subtraction
        }

        return String(chars)
    }

    private fun getRandomPart(): String {
        val chars = CharArray(RANDOM_LENGTH)

        // Generate 10 random bytes (80 bits) for the random component
        val randomBytes = ByteArray(10)  // 80 bits / 8 = 10 bytes
        secureRandom.nextBytes(randomBytes)

        // Convert 80 bits to 16 base32 characters (5 bits each)
        var bitBuffer = 0L
        var bitsInBuffer = 0
        var byteIndex = 0

        for (i in 0 until RANDOM_LENGTH) {
            // Ensure we have at least 5 bits in the buffer
            while (bitsInBuffer < 5 && byteIndex < randomBytes.size) {
                bitBuffer = (bitBuffer shl 8) or (randomBytes[byteIndex].toLong() and 0xFF)
                bitsInBuffer += 8
                byteIndex++
            }

            // Extract 5 bits for this character
            val value = ((bitBuffer shr (bitsInBuffer - 5)) and 0x1F).toInt()
            chars[i] = ENCODING_CHARS[value]
            bitsInBuffer -= 5
        }

        return String(chars)
    }

    /**
     * Extracts the timestamp from a ULID
     * @param ulid The ULID string
     * @return The timestamp in milliseconds since epoch, or null if invalid ULID
     */
    fun getTimestamp(ulid: String): Long? {
        if (ulid.length != TIME_LENGTH + RANDOM_LENGTH) return null

        var time = 0L
        for (i in 0 until TIME_LENGTH) {
            val c = ulid[i]
            val value = ENCODING_CHARS.indexOf(c)
            if (value == -1) return null
            time = time * ENCODING_LENGTH + value
        }
        return time
    }

    /**
     * Checks if a string is a valid ULID
     */
    fun isValid(ulid: String): Boolean {
        if (ulid.length != TIME_LENGTH + RANDOM_LENGTH) return false

        for (c in ulid) {
            if (ENCODING_CHARS.indexOf(c) == -1) {
                return false
            }
        }

        // Additional validation: Check if timestamp part is reasonable
        val timestamp = getTimestamp(ulid)
        return (timestamp != null) && (timestamp > 0)
    }

    // BONUS: Generate ULID with specific timestamp (useful for testing)
    fun generate(timestamp: Long): String {
        val timePart = getTimePart(timestamp)
        val randomPart = getRandomPart()
        return timePart + randomPart
    }

    private fun getTimePart(timestamp: Long): String {
        val chars = CharArray(TIME_LENGTH)
        var remaining = timestamp

        for (i in TIME_LENGTH - 1 downTo 0) {
            val mod = remaining % ENCODING_LENGTH
            chars[i] = ENCODING_CHARS[mod.toInt()]
            remaining = remaining / ENCODING_LENGTH
        }

        return String(chars)
    }
}