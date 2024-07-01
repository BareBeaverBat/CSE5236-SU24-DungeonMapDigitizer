package com.example.dungeontest.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun MultiFloatingActionButton(items: List<MiniFabItems>) {
    var expanded by remember {mutableStateOf(false)}

    Column(horizontalAlignment = Alignment.End) {
        AnimatedVisibility(visible = expanded,
            enter = fadeIn()+ slideInVertically(initialOffsetY = {it}) + expandVertically(),
            exit = fadeOut() + slideOutVertically(targetOffsetY = {it}) + shrinkVertically()
        ){
            LazyColumn(
                modifier = Modifier
                    .padding(bottom = 25.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End,
            ){

                items(items.size){
                    Spacer(modifier = Modifier.height(10.dp))
                    MiniFab(items[it])
                    if (it == items.count() - 1) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        val transition = updateTransition(targetState = expanded, label = "transition")
        val rotation by transition.animateFloat(label = "rotation") {
            if (it) 315f else 0f
        }

        FloatingActionButton(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .rotate(rotation),
            containerColor = MaterialTheme.colorScheme.primary,
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "")
        }
    }
}

@Composable
fun MiniFab(option: MiniFabItems) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 3.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Box(modifier = Modifier) {
            Text(
                text = option.title,
                color = MaterialTheme.colorScheme.onSurface,

            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        FloatingActionButton(
            shape = CircleShape,
            onClick = {
                option.onClick()
            },
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.size(50.dp)
        ) {
            Icon(imageVector = option.icon, contentDescription = "")
        }
    }
}

data class MiniFabItems(
    val icon: ImageVector,
    val title: String,
    val onClick: () -> Unit
)