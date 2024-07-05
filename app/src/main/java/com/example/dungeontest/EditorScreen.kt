package com.example.dungeontest

import OpenAiRequestBuilder
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dungeontest.composables.MapVisualization
import com.example.dungeontest.composables.MiniFabItems
import com.example.dungeontest.composables.MultiFloatingActionButton
import com.example.dungeontest.graph.AiMapDescParserImpl
import com.example.dungeontest.graph.OpenAiRespRoomAdapter
import com.example.dungeontest.model.EditorViewModel
import com.example.dungeontest.model.MapViewModel
import com.example.dungeontest.model.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(drawerState: DrawerState, scope: CoroutineScope, navController: NavController, mapViewModel: MapViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val viewModel = viewModel<EditorViewModel>()
    val settingsViewModel: SettingsViewModel = viewModel<SettingsViewModel>()

    val statusMessage = remember { mutableStateOf("Loading") }
    val graphString = remember { mutableStateOf<String?>(null) }
    val loading = remember { mutableStateOf(false) }

    val transitoryMapRecord = mapViewModel.transitoryMapRecord
    if(transitoryMapRecord == null){
        navController.navigate("MainScreen")
    }

    if(transitoryMapRecord!!.dotString == null){
        LaunchedEffect(key1 = Unit) {
            loading.value = true;
            Log.v("EditorScreen", settingsViewModel.tokenValue)
            Log.v("EditorScreen", settingsViewModel.selectedModel.toString())

                val requestBuilder = OpenAiRequestBuilder()
                val stringApiVersion = if(settingsViewModel.selectedModel == 0) "gpt-4-turbo" else "gpt-4o"

                requestBuilder.sendOpenAIRequest(
                    settingsViewModel.tokenValue,
                    stringApiVersion,
                    transitoryMapRecord.pictureFileName
                ) { response ->
                    if (response != null) {
                        Log.v("APIResponse", response)
                        val graph =
                            AiMapDescParserImpl().parseAiMapDesc(response, OpenAiRespRoomAdapter())
                        /* Do what we must with the graph */

                        /* get DOT string from graph */
                        /* set dotString on MapRecord */
                        /* use mapViewModel to update MapRecord and save */
                        /* Set graphString.value */
                    } else {
                        Log.v("OpenAIResponse", "response was null")
                        scope.launch {
                            if (snackbarHostState.currentSnackbarData == null)
                                snackbarHostState.showSnackbar(
                                    "An error occured generating your map.",
                                    duration = SnackbarDuration.Short
                                )
                        }
                    }
                    loading.value = false;
                }
        }
    }

    val testDot = viewModel.testDot

    if (loading.value) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = statusMessage.value)
        }
    } else {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Edit Map") },
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
                // for Scott
                val items = listOf(
                    MiniFabItems(Icons.Filled.Check, "Save", onClick = {
                        //onClick logic goes here
                    }),
                    MiniFabItems(Icons.Filled.Edit, "Rename Node", onClick =
                    {
                        //onClick logic goes here
                    }),
                    MiniFabItems(Icons.Filled.Add, "Add Edge", onClick = {
                        //onClick logic goes here
                    }),
                    MiniFabItems(Icons.Filled.Clear, "Delete Edge", onClick = {
                        //onClick logic goes here
                    }),
                )
                MultiFloatingActionButton(items)
            },
            floatingActionButtonPosition = FabPosition.End
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
                    if(graphString.value != null){
                        MapVisualization(graphString.value!!)
                    }
                    else{
                        MapVisualization(testDot)
                    }
                }
            }
        }
    }
}
