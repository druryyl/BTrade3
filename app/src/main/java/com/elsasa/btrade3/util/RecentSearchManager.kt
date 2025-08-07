package com.elsasa.btrade3.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

class RecentSearchManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("recent_searches", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val MAX_RECENT_SEARCHES = 10

    fun addRecentSearch(query: String) {
        if (query.isBlank()) return

        val recentSearches = getRecentSearches().toMutableList()

        // Remove if already exists
        recentSearches.remove(query)

        // Add to the beginning
        recentSearches.add(0, query)

        // Keep only the latest MAX_RECENT_SEARCHES
        if (recentSearches.size > MAX_RECENT_SEARCHES) {
            recentSearches.removeAt(recentSearches.size - 1)
        }

        val json = gson.toJson(recentSearches)
        prefs.edit { putString("recent_searches_list", json) }
    }

    fun getRecentSearches(): List<String> {
        val json = prefs.getString("recent_searches_list", null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun clearRecentSearches() {
        prefs.edit { remove("recent_searches_list") }
    }
}