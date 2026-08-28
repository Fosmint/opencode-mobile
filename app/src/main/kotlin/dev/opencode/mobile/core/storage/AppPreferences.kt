package dev.opencode.mobile.core.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "opencode_settings")

class AppPreferences(private val context: Context) {

    private val activeServerIdKey = stringPreferencesKey("active_server_id")
    private val themeKey = stringPreferencesKey("theme") // "dark" | "system"

    val activeServerId: Flow<String?> = context.dataStore.data.map { it[activeServerIdKey] }
    val theme: Flow<String> = context.dataStore.data.map { it[themeKey] ?: "dark" }

    suspend fun setActiveServerId(id: String?) {
        context.dataStore.edit {
            if (id == null) it.remove(activeServerIdKey) else it[activeServerIdKey] = id
        }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[themeKey] = theme }
    }
}
