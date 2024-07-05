package com.example.dungeontest.composables

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dungeontest.graph.deletePhotoFromInternalStorage
import com.example.dungeontest.model.MapRecord
import com.example.dungeontest.model.MapViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@Composable
fun MapDetailsCard(item: MapRecord, viewModel: MapViewModel, mapDetails: MapRecord, itemCallback: (map: MapRecord) -> Unit) {
    val context = LocalContext.current

    // Delete action
    val delete = SwipeAction(
        icon = rememberVectorPainter(Icons.TwoTone.Delete),
        background = MaterialTheme.colorScheme.errorContainer,
        onSwipe = {
            CoroutineScope(Dispatchers.IO).launch {
                val deleteResult = viewModel.deleteMap(mapDetails)
                deletePhotoFromInternalStorage(context, mapDetails.mapName)
                Log.d("MapDetails", "deleteResult: $deleteResult for map $mapDetails")
            }
        }
    )

    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .padding(8.dp)
    ) {
        SwipeableActionsBox(
            endActions = listOf(delete),
            backgroundUntilSwipeThreshold = MaterialTheme.colorScheme.errorContainer,
            swipeThreshold = 150.dp
        ) {

            Column(
                modifier = Modifier
                    .height(60.dp)
                    .clickable {
                        itemCallback(item)
                        //todo Handle item click. Will do later (to load a previously-saved map for visualizing, editing, and/or renaming)
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
                            .padding(5.dp,18.dp)
                            .weight(0.5f),
                        text = item.pictureFileName.takeLast(20)
                    )
                }

            }
        }
    }
}
