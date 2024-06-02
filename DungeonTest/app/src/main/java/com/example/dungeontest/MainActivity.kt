package com.example.dungeontest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.dungeontest.ui.theme.DungeonTestTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.FabPosition
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dungeontest.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DungeonTestTheme {
                MyScreen()
            }
        }
    }
}

data class MapDetails(
    val title: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val dummyData = loadDummyData()
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
                            navController.navigate("MainScreen") {
                                popUpTo("MainScreen") { inclusive = true }
                            }
                            drawerState.close()
                        }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Settings", style = MaterialTheme.typography.titleMedium) },
                    selected = false,
                    onClick = {
                        scope.launch {
                            navController.navigate("SettingsScreen") {
                                popUpTo("MainScreen") { inclusive = true }
                            }
                            drawerState.close()
                        }
                    }
                )
            }
        },
    ) {
        NavHost(navController = navController, startDestination = "MainScreen") {
            composable("MainScreen") {
                MainScreen(dummyData, drawerState, scope)
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
fun ItemCard(item: MapDetails) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier
            .padding(8.dp)
            .clickable {
                // Handle item click
            }
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = item.title)
        }
    }
}

@Composable
fun MapDetails(mapDetails: MapDetails) {
    ItemCard(item = mapDetails)
}

fun loadDummyData(): List<MapDetails> {
    return listOf(
        MapDetails("Map Saul"),
        MapDetails("Map Kim"),
        MapDetails("Map Nacho"),
        MapDetails("Map Tuco"),
        MapDetails("Map Mike"),

    )
}

