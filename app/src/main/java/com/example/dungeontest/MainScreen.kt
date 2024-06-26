package com.example.dungeontest

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dungeontest.data.SettingsStorage
import com.example.dungeontest.model.MapViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File


@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    drawerState: DrawerState,
    scope: CoroutineScope,
    navController: NavController
) {
    val context = LocalContext.current
    val preferences = SettingsStorage(context)
    val viewModel = viewModel<MapViewModel>()
    val maps = viewModel.allMaps.observeAsState()
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let {
                Log.v("MainScreen", "Photo URI: $it")
                /* Encode the URI to base64 because it's a file path. Navigation uses / */

                 val base64EncodedUri = Base64.encodeToString(it.toString().toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                /* navcontroller is passed from the NavGraph setup in MainActivity */
                navController.navigate("NamingScreen/$base64EncodedUri") {
                    popUpTo("NamingScreen") { inclusive = true }
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch {
                preferences.getAccessToken.collect { token ->
                    if (token.isEmpty()) {
                        if (snackbarHostState.currentSnackbarData == null)
                                snackbarHostState.showSnackbar(
                                    "API Key is required to proceed.",
                                    duration = SnackbarDuration.Short
                                )
                    } else {
                        photoUri = createImageFileUri(context)
                        cameraLauncher.launch(photoUri!!)
                    }
                }
            }


        } else {
            scope.launch {
                // Show a snackbar message. Ideally shouldn't overlap
                // TODO: Handle limited invokations of a snackbar
                snackbarHostState.showSnackbar("Camera permission is required to take photos")
            }
        }
    }


    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Home Page") },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Open navigation drawer")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Floating action button.")

            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { innerPadding ->

        Column(modifier = Modifier.padding(innerPadding)) {
            Spacer(modifier = Modifier.height(2.dp))
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 18.dp),
                modifier = Modifier.fillMaxSize()
            ) {

                items(maps.value ?: listOf()) { mapDetails ->
                    SavedMapEntry(mapDetails, viewModel)
                }
            }
        }
    }
}

fun createImageFileUri(context: Context): Uri {
    val imagePath = File(context.getExternalFilesDir(null), "images")
    imagePath.mkdirs()
    //todo need to make uuid instead of hardcoded name 'photo'?
    val imageFile = File(imagePath, "photo.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.file provider", imageFile)
}
