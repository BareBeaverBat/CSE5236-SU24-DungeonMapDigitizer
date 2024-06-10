package com.example.dungeontest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.Manifest
import android.util.Log
import androidx.compose.material3.SnackbarHost

import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File


@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    dummyData: List<MapDetails>,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    val context = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let {
                Log.v("MainScreen", "Photo URI: $it")
                //todo go to map naming screen (pass along the photo uri somehow???, so it can be associated with the new map name that the user enters in the database)

            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            photoUri = createImageFileUri(context)
            cameraLauncher.launch(photoUri!!)
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
            /* TODO: Implement FAB action */
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
                items(dummyData) { mapDetails ->
                    MapDetails(mapDetails)
                }
            }
        }
    }
}

fun createImageFileUri(context: Context): Uri {
    val imagePath = File(context.getExternalFilesDir(null), "images")
    imagePath.mkdirs()
    //todo need to make uuid instead of hardcoded name 'photo'?
    val imageFile = File(imagePath as File, "photo.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.file provider", imageFile)
}
