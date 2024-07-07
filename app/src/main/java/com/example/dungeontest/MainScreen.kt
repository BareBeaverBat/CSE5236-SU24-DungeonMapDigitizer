package com.example.dungeontest

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dungeontest.composables.MapDetailsCard
import com.example.dungeontest.model.MapViewModel
import com.example.dungeontest.model.SettingsViewModel
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    drawerState: DrawerState,
    scope: CoroutineScope,
    navController: NavController,
    viewModel: MapViewModel
) {
    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = viewModel<SettingsViewModel>()

    val options = remember {
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(1)
            .setResultFormats(RESULT_FORMAT_JPEG)
            .setScannerMode(SCANNER_MODE_FULL)
            .build()
    }

    val scanner = remember { GmsDocumentScanning.getClient(options) }

    var photoUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = {
            if (it.resultCode == RESULT_OK) {
                val result = GmsDocumentScanningResult.fromActivityResultIntent(it.data)
                photoUri = result?.pages?.get(0)?.imageUri
                if (photoUri != null) {
                    val base64EncodedPath = Base64.encodeToString(photoUri!!.path.toString().toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                    navController.navigate("NamingScreen/$base64EncodedPath") {
                        popUpTo("NamingScreen") { inclusive = true }
                    }
                } else {
                    Log.d("MainScreen", "photoUri is null. Result: ${result?.toString()}")
                }
            } else {
                Log.d("MainScreen", "Scanner resultCode not OK. Result code: ${it.resultCode}")
            }
        }
    )


    val maps = viewModel.allMaps.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }


    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch {
                    if (settingsViewModel.getToken().isEmpty()) {
                        if (snackbarHostState.currentSnackbarData == null)
                                snackbarHostState.showSnackbar(
                                    "API Key is required to proceed.",
                                    duration = SnackbarDuration.Short
                                )
                    } else {
                        scanner.getStartScanIntent(context as Activity)
                            .addOnSuccessListener {
                                scannerLauncher.launch(
                                    IntentSenderRequest.Builder(it).build()
                                )
                            }
                            .addOnFailureListener{
                                Log.v("MainScreen", it.toString())
                                scope.launch {
                                    if (snackbarHostState.currentSnackbarData == null)
                                        snackbarHostState.showSnackbar(
                                            "An error occurred while starting the scanner.",
                                            duration = SnackbarDuration.Short
                                        )
                                }
                                Log.d("MainScreen", "Error triggered at scanner.getStartScanIntent()")
                            }
                    }

            }

        } else {
            scope.launch {
                if (snackbarHostState.currentSnackbarData == null)
                    snackbarHostState.showSnackbar(
                        "Camera permission is required to take photos",
                        duration = SnackbarDuration.Short
                    )
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
                    MapDetailsCard(mapDetails,viewModel, mapDetails){
                        viewModel.transitoryMapRecord = it
                        navController.navigate("EditorScreen")
                    }
                }
            }
        }
    }
}

