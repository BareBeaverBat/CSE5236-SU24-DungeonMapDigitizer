package com.example.dungeontest.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.dungeontest.model.cardInfos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsStorage(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("userConfig")
        private val API_TOKEN = stringPreferencesKey("apiToken")
        private val SELECTED_MODEL_ID = intPreferencesKey("selectedModelId")
    }

    val getAccessToken: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[API_TOKEN] ?: ""
    }

    val getSelectedModel: Flow<Int> = context.dataStore.data.map { preferences ->
        Log.d("SettingsStorage", "getSelectedModel: ${preferences[SELECTED_MODEL_ID]}")
        preferences[SELECTED_MODEL_ID] ?: cardInfos.firstOrNull { it.isDefault }?.id ?: -1
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[API_TOKEN] = token
        }
    }
    suspend fun saveSelectedModel(modelId: Int) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_MODEL_ID] = modelId
        }
    }


    
}


