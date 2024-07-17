package com.example.dungeontest.composables

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
import com.example.dungeontest.graph.MapRoom
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge

private const val TAG = "AddRemoveEdgeDialog"

@Composable
fun AddRemoveEdgeDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit, currGraphState: MutableState<Graph<MapRoom, DefaultEdge>?>,
    finalizeGraphEdit: (modifiedGraph: Graph<MapRoom, DefaultEdge>) -> Unit,
    isAddingEdge: Boolean
) {
    if (currGraphState.value == null) {
        Log.e(
            TAG,
            "${if (isAddingEdge) "add" else "remove"} edge dialog was opened while the Graph variable was null"
        )
        onDismissRequest()
        return
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp)
        ) {
            val isFirstNodesExpanded = remember { mutableStateOf(false) }
            val isSecondNodesExpanded = remember { mutableStateOf(false) }

            val firstNodesDropdownIcon = if (isFirstNodesExpanded.value)
                Icons.Filled.KeyboardArrowUp
            else
                Icons.Filled.KeyboardArrowDown
            val secondNodesDropdownIcon = if (isSecondNodesExpanded.value)
                Icons.Filled.KeyboardArrowUp
            else
                Icons.Filled.KeyboardArrowDown


            val isReadyToConfirm = remember { mutableStateOf(false) }

            val selectedFirstNode: MutableState<MapRoom?> =
                rememberSaveable { mutableStateOf(null) }
            val selectedSecondNode: MutableState<MapRoom?> =
                rememberSaveable { mutableStateOf(null) }

            val possibleFirstNodes: MutableState<List<MapRoom>> =
                rememberSaveable { mutableStateOf(listOf()) }
            val possibleSecondNodes: MutableState<List<MapRoom>> =
                rememberSaveable { mutableStateOf(listOf()) }

            LaunchedEffect(key1 = Unit) {
                possibleFirstNodes.value = currGraphState.value!!.vertexSet().toList()
            }

            LaunchedEffect(key1 = selectedFirstNode.value) {
                val graphView = currGraphState.value!!

                if (selectedFirstNode.value == null) {
                    possibleSecondNodes.value = listOf()
                } else {
                    //flagging this as a possible area to check during performance optimization
                    possibleSecondNodes.value = graphView.vertexSet().filter {
                        val doesEdgeExist = graphView.containsEdge(selectedFirstNode.value, it)
                        val isOtherNodeCandidateForSecond =
                            selectedFirstNode.value != it &&
                            (if (isAddingEdge) !doesEdgeExist else doesEdgeExist)
                        isOtherNodeCandidateForSecond
                    }
                    if (selectedSecondNode.value != null
                        && !possibleSecondNodes.value.contains(selectedSecondNode.value)
                    ) {
                        selectedSecondNode.value = null
                    }
                }
            }
            LaunchedEffect(key1 = selectedFirstNode.value, key2 = selectedSecondNode.value) {
                isReadyToConfirm.value = selectedFirstNode.value != null && selectedSecondNode.value != null
            }


            val dialogWidth = remember { mutableFloatStateOf(0f) }

            //todo before checkpoint 6, explore making a composable to reduce code duplication
            // around dropdowns of MapRooms
            //start of dropdown for selecting first node (out of the two nodes that currently do
            // or don't have an edge between them)
            Box {
                OutlinedTextField(value = selectedFirstNode.value?.label ?: "?",
                    onValueChange = { selectedText ->
                        selectedFirstNode.value =
                            possibleFirstNodes.value.firstOrNull { it.label == selectedText }
                    }, label = { Text("First Room") }, trailingIcon = {
                        Icon(firstNodesDropdownIcon,
                            "button to ${if (isFirstNodesExpanded.value) "collapse" else "expand"} list of candidates for the first node",
                            Modifier.clickable { isFirstNodesExpanded.value = !isFirstNodesExpanded.value })
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            dialogWidth.floatValue = coords.size.toSize().width
                        }
                )

                DropdownMenu(
                    expanded = isFirstNodesExpanded.value,
                    onDismissRequest = { isFirstNodesExpanded.value = false },
                    modifier = Modifier.width(with(LocalDensity.current) { dialogWidth.floatValue.toDp() })
                ) {
                    possibleFirstNodes.value.forEach {
                        DropdownMenuItem(
                            text = { Text(it.label) },
                            onClick = {
                                selectedFirstNode.value = it
                                isFirstNodesExpanded.value = false
                            })
                    }
                }
            }


            //start of dropdown for selecting the second node
            Box {
                OutlinedTextField(value = selectedSecondNode.value?.label ?: "?",
                    onValueChange = { selectedText ->
                        selectedSecondNode.value =
                            possibleSecondNodes.value.firstOrNull { it.label == selectedText }
                    }, label = { Text("Second Room") }, trailingIcon = {
                        Icon(secondNodesDropdownIcon,
                            "button to ${if (isSecondNodesExpanded.value) "collapse" else "expand"} list of candidates for the second node",
                            Modifier.clickable { isSecondNodesExpanded.value = !isSecondNodesExpanded.value })
                    }, modifier = Modifier
                        .fillMaxWidth()
                        //relying on the OutlinedTextField from the first dropdown to populate dialogWidth
                )

                DropdownMenu(
                    expanded = isSecondNodesExpanded.value,
                    onDismissRequest = { isSecondNodesExpanded.value = false },
                    modifier = Modifier.width(with(LocalDensity.current) { dialogWidth.floatValue.toDp() })
                ) {
                    possibleSecondNodes.value.forEach {
                        DropdownMenuItem(
                            text = { Text(it.label) },
                            onClick = {
                                selectedSecondNode.value = it
                                isSecondNodesExpanded.value = false
                            })
                    }
                }
            }
            


            Button(enabled = isReadyToConfirm.value,
                onClick = {
                    val currFirstNode = selectedFirstNode.value!!
                    val currSecondNode = selectedSecondNode.value!!
                    if (isAddingEdge) {
                        currGraphState.value!!.addEdge(currFirstNode, currSecondNode)
                    } else {
                        currGraphState.value!!.removeEdge(currFirstNode, currSecondNode)
                    }
                    finalizeGraphEdit(currGraphState.value!!)
                    onDismissRequest()
            }) {
                Text("${if(isAddingEdge) "Add" else "Remove"} Hallway")
            }

            Button(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    }


}