package com.example.dungeontest.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dungeontest.data.SettingsStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel( application: Application): AndroidViewModel(application) {
    private val settingsStorage = SettingsStorage(application)

    private var _tokenValue = ""
    val tokenValue: String
        get() = _tokenValue

    private var _selectedModel = 0
    val selectedModel: Int
        get() = _selectedModel

    init {
        viewModelScope.launch {
            settingsStorage.getAccessToken.collect { token ->
                _tokenValue = token
            }
        }

        viewModelScope.launch {
            settingsStorage.getSelectedModel.collect { model ->
                _selectedModel = model
            }
        }
    }

    fun saveToken(token: String) {
        viewModelScope.launch {
            settingsStorage.saveToken(token)
        }
    }

    fun saveSelectedModel(modelId: Int) {
        viewModelScope.launch {
            settingsStorage.saveSelectedModel(modelId)
        }
    }

    // functions below to be called from anywhere else in the app that isn't the settings screen.
    suspend fun getToken(): String {
        return settingsStorage.getAccessToken.first()
    }

    suspend fun getSelectedModel(): Int {
        return settingsStorage.getSelectedModel.first()
    }

}