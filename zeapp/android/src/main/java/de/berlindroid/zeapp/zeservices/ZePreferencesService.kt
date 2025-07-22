package de.berlindroid.zeapp.zeservices

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val PREFS_NAME = "defaults"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFS_NAME,
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, PREFS_NAME))
    },
)

class ZePreferencesService
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        companion object {
            private val OPEN_API_PREFERENCES_KEY = stringPreferencesKey("openapi")
            private val THEME_KEY = intPreferencesKey("theme")
        }

        val dataStore = context.dataStore

        suspend fun getOpenApiKey(): String =
            dataStore.data
                .map { preferences -> preferences[OPEN_API_PREFERENCES_KEY].orEmpty() }
                .first()

        suspend fun getThemeSettings(): Int =
            dataStore.data
                .map { preferences -> preferences[THEME_KEY] ?: 0 }
                .first()

        suspend fun setThemeSettings(themeSettings: Int) {
            dataStore.edit { preferences ->
                preferences[THEME_KEY] = themeSettings
            }
        }
    }
