package com.example.dungeontest.composables

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
                            if (isAddingEdge) !doesEdgeExist else doesEdgeExist
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


            //!!todo fix dropdown
            Text(text = "First Room", modifier = Modifier)
            DropdownMenu(
                expanded = isFirstNodesExpanded.value,
                onDismissRequest = { isFirstNodesExpanded.value = false }) {
                possibleFirstNodes.value.forEach {
                    DropdownMenuItem(
                        text = { it.label }, onClick = { selectedFirstNode.value = it })
                }
            }


            //!!todo fix dropdown
            Text(text = "Second Room", modifier = Modifier)
            DropdownMenu(
                expanded = isSecondNodesExpanded.value,
                onDismissRequest = { isSecondNodesExpanded.value = false }) {
                possibleSecondNodes.value.forEach {
                    DropdownMenuItem(
                        text = { it.label }, onClick = { selectedSecondNode.value = it })
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