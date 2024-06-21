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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dungeontest.model.MapListViewModel
import com.example.dungeontest.model.MapRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamingScreen(drawerState: DrawerState, scope: CoroutineScope, base64EncodedPhotoUri: String, navController: NavController) {
    val snackbarHostState = remember { SnackbarHostState() }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val viewModel = viewModel<MapListViewModel>()

    val photoUri = String(Base64.decode(base64EncodedPhotoUri, Base64.NO_WRAP), Charsets.UTF_8)

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
                TextInputField(mapInputNameVal,"Map Name")
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        Log.v("SaveButton", "button clicked")
                        scope.launch {
                            val mapName = mapInputNameVal.value.text
                            val exists = viewModel.mapExists(mapName)
                            if (exists) {
                                showDialog.value = true
                            } else {
                                // Store this value in case we want to check if it actually saved or not
                                val didSaveMap = viewModel.insertMap(MapRecord(
                                    mapName,
                                    photoUri
                                ))
                                navController.navigate("MainScreen")
                            }
                        }
                      },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Text("Save")
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
                            viewModel.updateMap(MapRecord(
                                mapName,
                                photoUri
                            ))
                        }
                        showDialog.value = false
                        // Handle the save action here
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