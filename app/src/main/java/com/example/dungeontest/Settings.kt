package com.example.dungeontest

import android.Manifest
import android.content.res.Configuration
import android.graphics.fonts.FontFamily
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions

import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.dungeontest.ui.theme.DungeonTestTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.CoroutineScope
import com.example.dungeontest.model.AvailableModels
import com.example.dungeontest.model.cardInfos
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(drawerState: DrawerState, scope: CoroutineScope) {
    val snackbarHostState = remember { SnackbarHostState() }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    var selectedCard by rememberSaveable { mutableIntStateOf(cardInfos.firstOrNull { it.isDefault }?.id ?: 0 ) }
    Log.d("SettingsScreen", "selectedCard: $selectedCard")
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current


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
                /* TODO: Implement FAB action */

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
                SimpleOutlinedTextFieldSample()
                Spacer(modifier = Modifier.width(2.dp))
                if (isLandscape) {
                    LazyRow(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp)

                    ) {
                        items(cardInfos) { models ->
                            OutlinedCardExample(models.title, models.description, selectedCard == models.id) {
                                selectedCard = models.id
                            }
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
                            OutlinedCardExample(models.title, models.description, selectedCard == models.id) {
                                selectedCard = models.id
                            }
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun SimpleOutlinedTextFieldSample() {
    var text by rememberSaveable { mutableStateOf("") }
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

        value = text,
        onValueChange = {
            if (it.length <= maxLength) text = it
            if (it.length == 2) {
                textField.freeFocus()

            }
        },

        modifier = Modifier.focusRequester(textField).width(textFieldWidth),
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        textStyle = TextStyle(brush = brush),
        label = { Text("Your OpenAI Key") },
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
            }
        ),

    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OutlinedCardExample(title: String, description: String, selectedCard: Boolean, onSelected: () -> Unit) {
    var selected by rememberSaveable { mutableStateOf(selectedCard) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    OutlinedCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, Color.Black),
        modifier = Modifier


    ) {
        Column(modifier = Modifier
            .clickable {
                onSelected()
                keyboardController?.hide()
                focusManager.clearFocus()
            }
            .defaultMinSize(200.dp, 120.dp)
        ){
            Row() {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .width(160.dp)
                ) {

                    //Title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .wrapContentHeight()
                            .fillMaxWidth()
                    )

                    //Description
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
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
                        selected = selectedCard,
                        onClick = null
                    )
                }
            }

        }


    }
}

