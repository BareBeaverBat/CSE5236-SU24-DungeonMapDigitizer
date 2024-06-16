package com.example.dungeontest.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.dungeontest.model.cardInfos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class Preferences(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("userToken")
        private val USER_TOKEN_KEY = stringPreferencesKey("user_token")
        private val USER_SELECTED_MODEL = intPreferencesKey("user_selected_model")
    }

    val getAccessToken: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_TOKEN_KEY] ?: ""
    }

    val getSelectedModel: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[USER_SELECTED_MODEL] ?: cardInfos.firstOrNull { it.isDefault }?.id ?: 0
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_TOKEN_KEY] = token
        }
    }
    suspend fun saveSelectedModel(model: Int) {
        context.dataStore.edit { preferences ->
            preferences[USER_SELECTED_MODEL] = model
        }
    }

    suspend fun initSelectedModel() {
        val defaultModel = cardInfos.firstOrNull { it.isDefault }?.id ?: 0
        saveSelectedModel(defaultModel)
    }
}


