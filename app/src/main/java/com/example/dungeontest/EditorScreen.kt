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
import com.example.dungeontest.composables.LoadingOverlay
import com.example.dungeontest.composables.MapVisualization
import com.example.dungeontest.composables.MiniFabItems
import com.example.dungeontest.composables.MultiFloatingActionButton
import com.example.dungeontest.composables.RenameNodeDialog
import com.example.dungeontest.graph.AiMapDescParserImpl
import com.example.dungeontest.graph.GraphParser
import com.example.dungeontest.graph.MapRoom
import com.example.dungeontest.graph.OpenAiRespRoomAdapter
import com.example.dungeontest.model.MapViewModel
import com.example.dungeontest.model.SettingsViewModel
import com.example.dungeontest.model.cardInfos
import com.example.dungeontest.ui.EditorScreenRenderModes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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

    val mapGraph: MutableState<Graph<MapRoom, DefaultEdge>?> =
        rememberSaveable { mutableStateOf(null) }
    val mapDotString: MutableState<String?> = rememberSaveable { mutableStateOf(null) }

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
                        dLogWithoutTrunc(TAG, "non-null api response: $response")
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
    } else {
        Log.v(TAG, "DOT string is defined in viewmodel's transitory map record; on first composition after Activity creation/recreation (and not in other rounds of composition), will now try to populate map Graph and DOT string state varialbes")
        LaunchedEffect(key1 = Unit) {
            Log.v(TAG, "DOT string is defined in viewmodel's transitory map record, checking whether relevant state variables need to be populated")
            if (mapDotString.value == null) {
                Log.v(TAG, "updating DOT string state variable from viewmodel's transitory map record")
                /* build the graph object from the dotString parse */
                mapDotString.value = mapViewModel.transitoryMapRecord!!.dotString
            }

            if (mapGraph.value == null) {
                Log.v(TAG, "recreating Graph object from DOT string")
                /* build the graph object from the DOT string */
                mapGraph.value =
                    GraphParser().dotToGraph(mapViewModel.transitoryMapRecord!!.dotString!!)
            }
        }
    }

    when (renderMode.value) {
        EditorScreenRenderModes.LOADING_OVERLAY -> {
            LoadingOverlay(
                message = statusMessage.value
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

                            scope.launch {
                                val exists = mapViewModel.mapExists(mapViewModel.transitoryMapRecord!!.mapName)
                                if (exists) {
                                    mapViewModel.updateMap(mapViewModel.transitoryMapRecord!!)
                                } else {
                                    mapViewModel.insertMap(mapViewModel.transitoryMapRecord!!)
                                }
                                navController.navigate("MainScreen")
                            }
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
                            MapVisualization(dotAsString = it,
                                if(mapGraph.value == null)
                                    "Map can't be visualized"
                                else
                                    "Visualization of map with ${mapGraph.value!!.vertexSet().size} rooms"
                            )
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


/**
 * Logs very large strings more fully than Log.d() while avoiding splitting a line between
 * two log messages if possible
 * Based on https://stackoverflow.com/a/48284047/10808625 , but has various tweaks, including a
 *  quick if-else reversal so it can use tail recursion
 * @param currTag the current context's tag
 * @param longMsg potentially very long message to print out
 */
fun dLogWithoutTrunc(currTag: String, longMsg: String) {
    val MAX_INDEX = 4000
    val MIN_INDEX = 3000

    if (longMsg.length <= MAX_INDEX) {
        Log.d(currTag, longMsg)
    } else { // String to be logged is longer than the max...
        var currSegmentToLog = longMsg.substring(0, MAX_INDEX)

        // Try to find a substring break at a line end.
        var indexOfNextSegmentStart = currSegmentToLog.lastIndexOf('\n')
        if (indexOfNextSegmentStart >= MIN_INDEX) {
            currSegmentToLog = currSegmentToLog.substring(0, indexOfNextSegmentStart)
        } else {
            indexOfNextSegmentStart = MAX_INDEX
        }
        Log.d(currTag, currSegmentToLog)

        dLogWithoutTrunc(currTag, longMsg.substring(indexOfNextSegmentStart))
    }
}
