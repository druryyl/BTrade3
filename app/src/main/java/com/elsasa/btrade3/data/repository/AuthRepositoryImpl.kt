package com.elsasa.btrade3.data.repository

import com.elsasa.btrade3.domain.model.ValidationResult
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class AuthRepositoryImpl(
    private val baseUrl: String = "http://dev.smart-ics.com:8089"
) : AuthRepository {

    override suspend fun isSalesPersonValid(email: String): ValidationResult {
        return try {
            val encoded = URLEncoder.encode(email, "UTF-8")
            val urlStr = "$baseUrl/belajar-api/api/SalesPerson/exists?email=$encoded"
            val url = URL(urlStr)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15_000
                readTimeout = 15_000
                doInput = true
            }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val reader = BufferedReader(InputStreamReader(stream))
            val responseText = reader.use { it.readText() }
            conn.disconnect()

            if (code in 200..299) {
                val json = JSONObject(responseText)
                val data = json.optJSONObject("data")
                val isValid = data?.optInt("isValid", 0) ?: 0
                ValidationResult.Success(isValid == 1)
            } else {
                ValidationResult.Failure("Server returned HTTP $code")
            }
        } catch (e: Exception) {
            ValidationResult.Failure("Network error: ${e.message ?: "unknown"}")
        }
    }
}

