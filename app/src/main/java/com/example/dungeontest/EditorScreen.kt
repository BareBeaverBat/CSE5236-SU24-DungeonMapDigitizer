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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dungeontest.composables.AddRemoveEdgeDialog
import com.example.dungeontest.composables.MapVisualization
import com.example.dungeontest.composables.MiniFabItems
import com.example.dungeontest.composables.MultiFloatingActionButton
import com.example.dungeontest.graph.AiMapDescParserImpl
import com.example.dungeontest.graph.OpenAiRespRoomAdapter
import com.example.dungeontest.model.MapViewModel
import com.example.dungeontest.model.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.example.dungeontest.model.cardInfos
import com.example.dungeontest.graph.GraphParser
import com.example.dungeontest.composables.LoadingOverlay
import com.example.dungeontest.composables.RenameNodeDialog
import com.example.dungeontest.graph.MapRoom
import com.example.dungeontest.ui.EditorScreenRenderModes
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge

private const val TAG = "EditorScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    drawerState: DrawerState,
    scope: CoroutineScope,
    navController: NavController,
    mapViewModel: MapViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // settingsViewModel for retrieval of token and user selected model
    val settingsViewModel: SettingsViewModel = viewModel<SettingsViewModel>()

    // loading overlay related values
    val statusMessage = remember { mutableStateOf("Loading") }
    val renderMode = rememberSaveable { mutableStateOf(EditorScreenRenderModes.VIEWER) }

    // to prevent multiple API calls when screen is recomposed on configuration change
    var launched by rememberSaveable { mutableStateOf(false) }

    var mapGraph: MutableState<Graph<MapRoom, DefaultEdge>?> =
        rememberSaveable { mutableStateOf(null) }
    var mapDotString: MutableState<String?> = rememberSaveable { mutableStateOf(null) }

    val updateGraphVis = { updatedGraph: Graph<MapRoom, DefaultEdge> ->
        val dotString = GraphParser().graphToDot(updatedGraph)
        Log.v(TAG, "dot string was: $dotString")
        mapViewModel.transitoryMapRecord =  mapViewModel.transitoryMapRecord!!.copy(dotString = dotString)
        mapDotString.value = dotString
    }
    val editorDialogDismisser = { renderMode.value = EditorScreenRenderModes.VIEWER }


    if (mapViewModel.transitoryMapRecord == null) {
        navController.navigate("MainScreen")
    }

    if (mapViewModel.transitoryMapRecord!!.dotString == null) {
        LaunchedEffect(key1 = Unit) {
            if (!launched) {
                launched = true

                renderMode.value = EditorScreenRenderModes.LOADING_OVERLAY;
                Log.v(TAG, "tokenValue: ${settingsViewModel.getToken()}")
                Log.v(TAG, "selected model: ${settingsViewModel.selectedModel}")

                val requestBuilder = OpenAiRequestBuilder()
                val stringApiVersion =
                    cardInfos.find { it.id == settingsViewModel.getSelectedModel() }?.codeName ?: ""

                requestBuilder.sendOpenAIRequest(
                    settingsViewModel.getToken(),
                    stringApiVersion,
                    mapViewModel.transitoryMapRecord!!.pictureFileName
                ) { response ->
                    if (response != null) {
                        Log.v(TAG, "non-null api response: $response")
                        val graph =
                            AiMapDescParserImpl().parseAiMapDesc(response, OpenAiRespRoomAdapter())


                        mapGraph.value = graph
                        updateGraphVis(graph)
                    } else {
                        Log.v(TAG, "AI API response was null")
                        scope.launch {
                            if (snackbarHostState.currentSnackbarData == null)
                                snackbarHostState.showSnackbar(
                                    "An error occured generating your map.",
                                    duration = SnackbarDuration.Short
                                )
                        }
                    }
                    renderMode.value = EditorScreenRenderModes.VIEWER;
                }
            }
        }
    }

    when (renderMode.value) {
        EditorScreenRenderModes.LOADING_OVERLAY -> {
            LoadingOverlay(
                message = statusMessage.value,
            )
        }
        EditorScreenRenderModes.VIEWER -> {
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


                            //todo!
                            //onClick logic goes here
                        }),
                        MiniFabItems(Icons.Filled.Edit, "Rename Node", onClick = {
                            renderMode.value = EditorScreenRenderModes.RENAME_NODE
                        }),
                        MiniFabItems(Icons.Filled.Add, "Add Edge", onClick = {
                            renderMode.value = EditorScreenRenderModes.ADD_EDGE
                        }),
                        MiniFabItems(Icons.Filled.Clear, "Delete Edge", onClick = {
                            renderMode.value = EditorScreenRenderModes.REMOVE_EDGE
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
                        mapDotString.value?.let {
                            MapVisualization(dotAsString = it)
                        }
                    }
                }
            }
        }

        EditorScreenRenderModes.RENAME_NODE -> {
            RenameNodeDialog(
                onDismissRequest = editorDialogDismisser, currGraphState = mapGraph,
                finalizeGraphEdit = updateGraphVis
            )
        }

        EditorScreenRenderModes.ADD_EDGE -> {
            AddRemoveEdgeDialog(
                onDismissRequest = editorDialogDismisser, currGraphState = mapGraph,
                finalizeGraphEdit = updateGraphVis, isAddingEdge = true
            )
        }

        EditorScreenRenderModes.REMOVE_EDGE -> {
            AddRemoveEdgeDialog(
                onDismissRequest = editorDialogDismisser, currGraphState = mapGraph,
                finalizeGraphEdit = updateGraphVis, isAddingEdge = false
            )
        }
    }
}
