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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dungeontest.model.MapListViewModel
import com.example.dungeontest.ui.theme.DungeonTestTheme
import kotlinx.coroutines.launch

import com.example.dungeontest.model.MapRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate() called")
        enableEdgeToEdge()
        setContent {
            DungeonTestTheme {
                MyScreen()
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
fun MyScreen() {//todo discuss renaming this to something more informative like RootScreen or BaseScreen
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

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
                MainScreen(drawerState, scope)
            }
            composable("SettingsScreen") {
                SettingsScreen(drawerState, scope)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DungeonTestTheme {
        MyScreen()

    }
}

@Composable
fun ItemCard(item: MapRecord, modifier: Modifier = Modifier) {
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
                }
        ) {
            Row {
                Text(
                    modifier = Modifier.padding(18.dp).weight(0.5f),
                    text = item.mapName
                )
                Text(
                    modifier = Modifier.padding(18.dp).weight(0.5f),
                    text = item.pictureFileName.takeLast(20)
                )
            }
        }
    }
}

@Composable
fun MapDetails(mapDetails: MapRecord, viewModel: MapListViewModel, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        ItemCard(item = mapDetails, Modifier.weight(0.9f))
        IconButton(onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                val deleteResult = viewModel.deleteMap(mapDetails)
                Log.d("MapDetails", "deleteResult: $deleteResult for map $mapDetails")
            }
        }, modifier = Modifier.align(Alignment.CenterVertically)
            .border(2.dp, Color.Red, shape = CircleShape)
            .weight(0.1f)
            ) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete")
        }

    }
}


