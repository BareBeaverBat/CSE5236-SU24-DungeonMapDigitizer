package com.example.dungeontest

import android.util.Base64
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dungeontest.graph.saveImageToInternalStorage
import com.example.dungeontest.model.MapRecord
import com.example.dungeontest.model.MapViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamingScreen(drawerState: DrawerState, scope: CoroutineScope, base64EncodedPhotoUri: String, navController: NavController, viewModel: MapViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val photoUri = URLDecoder.decode(String(Base64.decode(base64EncodedPhotoUri, Base64.NO_WRAP), Charsets.UTF_8), StandardCharsets.UTF_8.toString())

    val context = LocalContext.current

    val mapInputNameVal = remember {
        mutableStateOf(TextFieldValue())
    }

    val showDialog = remember {
        mutableStateOf(false)
    }

    Log.v("NamingScreen", photoUri)
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Map") },
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
        }
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
                Spacer(modifier = Modifier.height(2.dp))
                TextInputField(mapInputNameVal, "Map Name")
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        Log.v("NextButton", "button clicked")
                        scope.launch {
                            val mapName = mapInputNameVal.value.text
                            val exists = viewModel.mapExists(mapName)
                            if (exists) {
                                showDialog.value = true
                            } else {
                                val imagePath = saveImageToInternalStorage(context = context, base64Image = base64EncodedPhotoUri, mapName = mapName)
                                Log.v("NamingScreen", imagePath.toString())
                                if (imagePath != null) {
                                    val newMap = MapRecord(
                                        mapName,
                                        imagePath.toString(),
                                    )
                                    val didSaveMap = viewModel.insertMap(newMap)
                                    Log.v("NamingScreen", "Setting transitory value")
                                    viewModel.transitoryMapRecord = newMap
                                    navController.navigate("EditorScreen")
                                } else {
                                    snackbarHostState.showSnackbar("Failed to save image.")
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Text("Continue")
                }
            }
        }
    }
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showDialog.value = false
            },
            title = {
                Text(text = "Map Already Exists!")
            },
            text = {
                Column {
                    Text("Confirming this will overwrite the old map.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Would you like to continue?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val mapName = mapInputNameVal.value.text
                            val imagePath = saveImageToInternalStorage(context = context, base64Image = base64EncodedPhotoUri, mapName = mapName)
                            Log.v("NamingScreen", imagePath.toString())
                            if (imagePath != null) {
                                val newMap = MapRecord(
                                    mapName,
                                    imagePath.toString(),
                                )
                                val didSaveMap = viewModel.updateMap(newMap)
                                viewModel.transitoryMapRecord = newMap
                            } else {
                                snackbarHostState.showSnackbar("Failed to save image.")
                            }
                        }
                        showDialog.value = false
                        navController.navigate("EditorScreen")
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
private fun TextInputField(tokenValue: MutableState<TextFieldValue>, labelText: String) {
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
        value = tokenValue.value,
        onValueChange = {
            tokenValue.value = it
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