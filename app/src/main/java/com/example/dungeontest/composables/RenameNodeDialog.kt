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

private const val TAG = "RenameNodeDialog"

@Composable
fun RenameNodeDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit, currGraphState: MutableState<Graph<MapRoom, DefaultEdge>?>,
    finalizeGraphEdit: (modifiedGraph: Graph<MapRoom, DefaultEdge>) -> Unit
) {
    if (currGraphState.value == null) {
        Log.e(TAG, "rename node dialog was opened while the Graph variable was null")
        onDismissRequest()
        return
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp)
        ) {
            val isNodesExpanded = remember { mutableStateOf(false) }
            val nodesDropdownIcon = if (isNodesExpanded.value)
                Icons.Filled.KeyboardArrowUp
            else
                Icons.Filled.KeyboardArrowDown

            val isReadyToConfirm = remember { mutableStateOf(false) }

            val selectedNode: MutableState<MapRoom?> =
                rememberSaveable { mutableStateOf(null) }
            val possibleNodes: MutableState<List<MapRoom>> =
                rememberSaveable { mutableStateOf(listOf()) }

            val newRoomName: MutableState<String?> = rememberSaveable { mutableStateOf(null) }

            LaunchedEffect(key1 = Unit) {
                possibleNodes.value = currGraphState.value!!.vertexSet().toList()
            }

            LaunchedEffect(key1 = selectedNode.value, key2 = newRoomName.value) {
                isReadyToConfirm.value = selectedNode.value != null &&
                        (newRoomName.value?.isNotBlank() ?: false)
            }

            val dialogWidth = remember { mutableFloatStateOf(0f) }

            Box {
                OutlinedTextField(value = selectedNode.value?.label ?: "?",
                    onValueChange = { selectedText ->
                        selectedNode.value =
                            possibleNodes.value.firstOrNull { it.label == selectedText }
                    }, label = { Text("Room to rename") }, trailingIcon = {
                        Icon(nodesDropdownIcon,
                            "button to ${if (isNodesExpanded.value) "collapse" else "expand"} list of nodes",
                            Modifier.clickable { isNodesExpanded.value = !isNodesExpanded.value })
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            dialogWidth.floatValue = coords.size.toSize().width
                        }
                )

                DropdownMenu(
                    expanded = isNodesExpanded.value,
                    onDismissRequest = { isNodesExpanded.value = false },
                    modifier = Modifier.width(with(LocalDensity.current) { dialogWidth.floatValue.toDp() })
                ) {
                    possibleNodes.value.forEach {
                        DropdownMenuItem(
                            text = { Text(it.label) },
                            onClick = {
                                selectedNode.value = it
                                newRoomName.value = it.label
                                isNodesExpanded.value = false
                            })
                    }
                }
            }

            TextInputField("", newRoomName, "New Room Name")

            Button(enabled = isReadyToConfirm.value,
                onClick = {
                    //the enabled check of isReadyToConfirm should prevent newRoomName.value from
                    // being null here
                    selectedNode.value!!.label = newRoomName.value ?: "!!!ERROR- INVALID ROOM NAME"

                    finalizeGraphEdit(currGraphState.value!!)
                    onDismissRequest()
                }) {
                Text("Rename Room")
            }

            Button(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    }


}