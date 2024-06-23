package com.example.dungeontest

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import com.example.dungeontest.model.cardInfos
import com.example.dungeontest.data.SettingsStorage
import com.example.dungeontest.model.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    drawerState: DrawerState,
    scope: CoroutineScope,
) {
    val viewModel: SettingsViewModel = viewModel()
    // Remembering state
    val snackbarHostState = remember { SnackbarHostState() }

    // Checking device orientation
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Managing focus and keyboard
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Context and preferences datastore
    val context = LocalContext.current
    val preferences = SettingsStorage(context)

    // Value for the text field from state
    var tokenValue = viewModel.tokenValue.value.text
    var selectedModel = viewModel.selectedModel.value

    // Value for the text field from user input
    val tokenValueInput = rememberSaveable { mutableStateOf<String?>(null) }

    val selectedModelInput = rememberSaveable { mutableIntStateOf(-1) }


    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            drawerState.open()
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Open nav drawer")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = {

            if (tokenValueInput.value != null) {
                tokenValue = tokenValueInput.value!!
                viewModel.saveToken(tokenValue)
            }

            if (selectedModelInput.value != -1) {
                selectedModel = selectedModelInput.value
                viewModel.saveSelectedModel(selectedModel)
            }

            scope.launch {
                withContext(Dispatchers.Main) {
                    if (snackbarHostState.currentSnackbarData == null) {
                        snackbarHostState.showSnackbar("Data saved successfully")
                    }
                }
            }

            }) {

                Icon(Icons.Filled.Check, contentDescription = "Save")
                Text("Save")

            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)

        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.width(2.dp))
                TextInputField(tokenValue, tokenValueInput, "Your OpenAI Key")
                Spacer(modifier = Modifier.width(2.dp))
                if (isLandscape) {
                    LazyRow(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp)

                    ) {
                        items(cardInfos) { models ->
                            ModelCard(models.id, models.title, models.description, selectedModel, selectedModelInput)
                            Spacer(modifier = Modifier.width(20.dp))
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(cardInfos) { models ->
                            ModelCard(models.id, models.title, models.description, selectedModel, selectedModelInput)
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun TextInputField(tokenValueState: String, tokenValueInput: MutableState<String?>, labelText: String) {
    val maxLength = 40
    val aiEsqueColors = listOf(
        Color(0xFF607D8B),
        Color(0xFF3F51B5),
        Color(0xFF2196F3),
        Color(0xFF03A9F4),
        Color(0xFF00BCD4),
        Color(0xFF009688),
        Color(0xFF4CAF50),
        Color(0xFF8BC34A)
    )
    val brush = remember {
        Brush.linearGradient(
            colors = aiEsqueColors
        )
    }
    val textField = FocusRequester()
    val focusManager = LocalFocusManager.current
    val textFieldWidth = (maxLength * 8).dp
    OutlinedTextField(
        value = tokenValueInput.value?.let { it } ?: tokenValueState,
        onValueChange = {newValue ->
            tokenValueInput.value = newValue
        },

        modifier = Modifier
            .focusRequester(textField)
            .width(textFieldWidth),
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        textStyle = TextStyle(brush = brush),
        label = { Text(labelText) },
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
            }
        ),

    )
}


@Composable
fun ModelCard(id: Int, title: String, description: String, selectedModel: Int, selectedModelInput: MutableIntState) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val outlineColor =
        if (selectedModelInput.value != -1) //value is initialized but no user input
            if (selectedModelInput.value == id) MaterialTheme.colorScheme.primary
            else Color.Gray
        else
            if (selectedModel == id) MaterialTheme.colorScheme.primary
            else Color.Gray

    OutlinedCard(

        modifier = Modifier,
        border = BorderStroke(
            width = 2.dp,
            color = outlineColor
        ),

    ) {
        Column(
            modifier = Modifier
                .clickable {
                    selectedModelInput.value = id
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }


        ){
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .width(200.dp)
                    .height(120.dp)
            ) {
                Column(
                    modifier = Modifier
                        .width(160.dp)

                ) {

                    //Title
                    Text(
                        color = outlineColor,

                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .wrapContentHeight()
                            .fillMaxWidth()
                    )

                    //Description
                    Text(
                        color = outlineColor,
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                    )


                }
                Column(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    RadioButton(
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = Color.Gray
                        ),
                        selected =
                            if (selectedModelInput.value != -1) //value is initialized but no user input
                                selectedModelInput.value == id
                            else
                                selectedModel == id,
                        onClick = null
                    )
                }
            }

        }

    }
}



