package com.example.dungeontest.model

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dungeontest.data.SettingsStorage
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

}