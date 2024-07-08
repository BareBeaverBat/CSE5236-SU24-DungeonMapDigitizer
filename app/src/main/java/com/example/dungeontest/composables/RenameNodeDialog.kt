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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
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

            //todo rename this once I better understand what it's doing
            val textFieldSize = remember { mutableStateOf(Size.Zero)}

            Box {
                OutlinedTextField(value = selectedNode.value?.label ?: "?",
                    onValueChange = {
                        val selectedText = it
                        selectedNode.value = possibleNodes.value.firstOrNull { it.label == selectedText}
                    }, label={Text("Room to rename")}, trailingIcon = {
                        Icon(nodesDropdownIcon, "button to ${if(isNodesExpanded.value) "collapse" else "expand"} list of nodes",
                            Modifier.clickable { isNodesExpanded.value = !isNodesExpanded.value })
                    }, modifier = Modifier.fillMaxWidth().onGloballyPositioned { coords ->
                        textFieldSize.value = coords.size.toSize()
                    }
                )

                DropdownMenu(
                    expanded = isNodesExpanded.value,
                    onDismissRequest = { isNodesExpanded.value = false },
                    modifier = Modifier.width(with(LocalDensity.current){textFieldSize.value.width.toDp()})) {
                    possibleNodes.value.forEach {
                        DropdownMenuItem(text= {Text(it.label)}, onClick = { selectedNode.value = it })
                    }
                }
            }

            TextInputField("", newRoomName, "New Room Name")

            Button(enabled = isReadyToConfirm.value,
                onClick = {
                    val roomToRename = selectedNode.value!!

                    //todo per Nick, see if I can override hashcode/equals in MapRoom to only rely on id field, then make label a var??
                    // then this becomes easy/quick rather than hard/wasteful

                    //flagging next 10 lines as a possible area to check during performance optimization
                    val neighborsOfRenamedRoom = currGraphState.value!!.vertexSet().filter {
                        currGraphState.value!!.containsEdge(roomToRename, it)
                    }
                    currGraphState.value!!.removeVertex(roomToRename)

                    val renamedRoom = roomToRename.copy(label = newRoomName.value!!)
                    currGraphState.value!!.addVertex(renamedRoom)
                    neighborsOfRenamedRoom.forEach {
                        currGraphState.value!!.addEdge(renamedRoom, it)
                    }

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