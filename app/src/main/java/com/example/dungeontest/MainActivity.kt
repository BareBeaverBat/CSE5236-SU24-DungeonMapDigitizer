package com.example.dungeontest


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dungeontest.graph.deletePhotoFromInternalStorage
import com.example.dungeontest.model.MapRecord
import com.example.dungeontest.model.MapViewModel
import com.example.dungeontest.ui.theme.DungeonTestTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate() called")
        enableEdgeToEdge()
        setContent {
            DungeonTestTheme {
                RootScreen()
            }
        }
        Log.v(TAG, "onCreate() finished")
    }

    override fun onStart() {
        super.onStart()
        Log.v(TAG, "onStart() called")
    }

    override fun onResume() {
        super.onResume()
        Log.v(TAG, "onResume() called")
    }

    override fun onPause() {
        super.onPause()
        Log.v(TAG, "onPause() called")
    }

    override fun onStop() {
        super.onStop()
        Log.v(TAG, "onStop() called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(TAG, "onDestroy() called")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val mapViewModel: MapViewModel = viewModel<MapViewModel>()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(230.dp)) {
                NavigationDrawerItem(
                    label = { Text("Main Page", style = MaterialTheme.typography.titleMedium) },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("MainScreen") {
                                popUpTo("MainScreen") { inclusive = true }
                            }

                        }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Settings", style = MaterialTheme.typography.titleMedium) },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("SettingsScreen") {
                                popUpTo("MainScreen") { inclusive = false }
                            }

                        }
                    }
                )

            }
        },
    ) {
        NavHost(navController = navController, startDestination = "MainScreen") {
            composable("MainScreen") {
                MainScreen(drawerState, scope, navController, mapViewModel)
            }
            composable("SettingsScreen") {
                SettingsScreen(drawerState, scope)
            }

            composable (
                "NamingScreen/{PhotoUri}",
                arguments = listOf(navArgument("PhotoUri") { type = NavType.StringType })
            ){ backStack ->
                val photoUri = backStack.arguments?.getString("PhotoUri") ?: ""
                NamingScreen(drawerState, scope, photoUri, navController, mapViewModel)
            }
            composable("EditorScreen") {
                EditorScreen(drawerState, scope, navController, mapViewModel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DungeonTestTheme {
        RootScreen()

    }
}

@Composable
fun MapDetailsCard(item: MapRecord, modifier: Modifier = Modifier, itemOnClick: (item: MapRecord) -> Unit) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = modifier
            .padding(8.dp)

    ) {
        Column(
            modifier = Modifier
                .height(60.dp)
                .clickable(
                ) {
                    //todo Handle item click. Will do later (to load a previously-saved map for visualizing, editing, and/or renaming)
                    itemOnClick(item)
                }
        ) {
            Row {
                Text(
                    modifier = Modifier
                        .padding(18.dp)
                        .weight(0.5f),
                    text = item.mapName
                )
                Text(
                    modifier = Modifier
                        .padding(2.dp, 18.dp)
                        .weight(0.5f),
                    text = item.pictureFileName.takeLast(20)
                )
            }
        }
    }
}

@Composable
fun SavedMapEntry(mapDetails: MapRecord, viewModel: MapViewModel, modifier: Modifier = Modifier, itemOnClick: (item: MapRecord) -> Unit) {
    val context = LocalContext.current
    Row(modifier = modifier) {
        MapDetailsCard(item = mapDetails, Modifier.weight(0.9f), itemOnClick)
        IconButton(onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                val deleteResult = viewModel.deleteMap(mapDetails)
                deletePhotoFromInternalStorage(context, mapDetails.mapName)
                Log.d("MapDetails", "deleteResult: $deleteResult for map $mapDetails")
            }
        }, modifier = Modifier
            .align(Alignment.CenterVertically)
            .border(2.dp, Color.Red, shape = CircleShape)
            .weight(0.1f)
            ) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete")
        }

    }
}


