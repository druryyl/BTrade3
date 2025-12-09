package com.elsasa.btrade3.datastore


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.serverPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "server_preferences")

class ServerPreferencesDataSource(private val context: Context) {
    companion object {
        private val SERVER_TARGET_KEY = stringPreferencesKey("server_target")
        const val DEFAULT_SERVER = "JOG" // Default to Jogja
    }

    val serverTarget: Flow<String> = context.serverPreferencesDataStore.data
        .map { preferences ->
            preferences[SERVER_TARGET_KEY] ?: DEFAULT_SERVER
        }

    suspend fun setServerTarget(server: String) {
        context.serverPreferencesDataStore.edit { preferences ->
            preferences[SERVER_TARGET_KEY] = server
        }
    }

    suspend fun getServerTarget(): String {
        return context.serverPreferencesDataStore.data.first()[SERVER_TARGET_KEY] ?: DEFAULT_SERVER
    }
}