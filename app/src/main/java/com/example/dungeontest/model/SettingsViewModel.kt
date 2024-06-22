package com.example.dungeontest.model

import android.app.Application
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.dungeontest.data.SettingsStorage

class SettingsViewModel(application: Application): AndroidViewModel(application){
    private val settingsStorage = SettingsStorage(application)
    private val _tokenValue = MutableStateFlow(TextFieldValue())
    val tokenValue: StateFlow<TextFieldValue> = _tokenValue.asStateFlow()

    private val _selectedModel = MutableStateFlow(-1)
    val selectedModel: StateFlow<Int> = _selectedModel.asStateFlow()

    private val _tokenValueInput = MutableStateFlow(TextFieldValue())
    private val _selectedModelInput = MutableStateFlow(-1)

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

        initInputValues()
    }

    private fun initInputValues(){
        _tokenValueInput.value = _tokenValue.value
        _selectedModelInput.value = _selectedModel.value
    }

    fun getTokenValueInput(): TextFieldValue {
        return _tokenValueInput.value
    }

    fun getSelectedModelInput(): Int {
        return _selectedModelInput.value
    }

    fun setTokenValueInput(value: TextFieldValue){
        _tokenValueInput.value = value
    }

    fun setSelectedModelInput(value: Int){
        _selectedModelInput.value = value
    }

}