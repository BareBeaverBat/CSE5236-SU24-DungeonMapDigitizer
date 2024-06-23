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
    private val _tokenValue = mutableStateOf(TextFieldValue())
    val tokenValue: State<TextFieldValue> = _tokenValue

    private val _selectedModel = mutableIntStateOf(0)
    val selectedModel: State<Int> = _selectedModel

    init {
        viewModelScope.launch {
            settingsStorage.getAccessToken.collect { token ->
                _tokenValue.value = TextFieldValue(token)
            }
        }

        viewModelScope.launch {
            settingsStorage.getSelectedModel.collect { model ->
                _selectedModel.value = model
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