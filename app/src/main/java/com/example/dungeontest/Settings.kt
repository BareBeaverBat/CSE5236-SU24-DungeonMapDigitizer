package com.example.dungeontest

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dungeontest.composables.TextInputField
import com.example.dungeontest.model.SettingsViewModel
import com.example.dungeontest.model.cardInfos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    drawerState: DrawerState,
    scope: CoroutineScope,
) {
    val viewModel: SettingsViewModel = viewModel<SettingsViewModel>()
    // Remembering state
    val snackbarHostState = remember { SnackbarHostState() }

    // Checking device orientation
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Managing focus and keyboard
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Value for the text field from persistent state(preferences datastore)
    var tokenValue = viewModel.tokenValue
    var selectedModel = viewModel.selectedModel

    // Value for the text field from user input before hitting save
    val tokenValueInput = rememberSaveable { mutableStateOf<String?>(null) }
    val selectedModelInput = rememberSaveable { mutableIntStateOf(-1) }


    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = {

            if (tokenValueInput.value != null) {
                tokenValue = tokenValueInput.value!!
                viewModel.saveToken(tokenValue)
            }

            if (selectedModelInput.intValue != -1) {
                selectedModel = selectedModelInput.intValue
                viewModel.saveSelectedModel(selectedModel)
            }

            scope.launch {
                withContext(Dispatchers.Main) {
                    if (snackbarHostState.currentSnackbarData == null) {
                        snackbarHostState.showSnackbar("Data saved successfully")
                    }
                }
            }

            }) {

                Icon(Icons.Filled.Check, contentDescription = "Save")
                Text("Save")

            }
        },
        floatingActionButtonPosition = FabPosition.Center
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
                Spacer(modifier = Modifier.width(2.dp))
                TextInputField(tokenValue, tokenValueInput, "Your OpenAI Key")
                Spacer(modifier = Modifier.width(2.dp))
                if (isLandscape) {
                    LazyRow(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp)

                    ) {
                        items(cardInfos) { models ->
                            ModelCard(models.id, models.title, models.description, selectedModel, selectedModelInput)
                            Spacer(modifier = Modifier.width(20.dp))
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(cardInfos) { models ->
                            ModelCard(models.id, models.title, models.description, selectedModel, selectedModelInput)
                        }
                    }
                }
            }

        }
    }
}


@Composable
fun ModelCard(id: Int, title: String, description: String, selectedModel: Int, selectedModelInput: MutableIntState) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val referencedSelection =
        if (selectedModelInput.intValue != -1) selectedModelInput.intValue
        else  selectedModel

    val txtColor =
        if (referencedSelection == id)
            MaterialTheme.colorScheme.primary
        else
            Color.Gray

    AnimatedBorderCard(
        modifier = Modifier
            .width(320.dp)
            .height(180.dp)
            .fillMaxWidth(),

        shape = RoundedCornerShape(12.dp),
        borderWidth = if (referencedSelection != id) 1.dp else 2.dp,
        gradient =
            if (referencedSelection == id)
                Brush.linearGradient(listOf(Color.Magenta, Color.Cyan))
            else
                Brush.linearGradient(listOf(Color.Gray,Color.Gray)),
        onCardClick = {}
    ) {
        Column(
            modifier = Modifier
                .clickable {
                    selectedModelInput.intValue = id
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }


        ){
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .width(320.dp)
                    .height(180.dp)
            ) {
                Column(
                    modifier = Modifier
                        .width(160.dp)

                ) {

                    //Title
                    Text(
                        color = txtColor,

                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .wrapContentHeight()
                            .fillMaxWidth()
                    )

                    //Description
                    Text(
                        color = txtColor,
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                    )


                }
                Spacer(modifier = Modifier.width(100.dp))
                Column(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    RadioButton(
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = Color.Gray
                        ),
                        selected =
                            if (selectedModelInput.intValue != -1) //value is initialized but no user input
                                selectedModelInput.intValue == id
                            else
                                selectedModel == id,
                        onClick = null
                    )
                }
            }

        }

    }
}

@Composable
fun AnimatedBorderCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(size = 0.dp),
    borderWidth: Dp = 2.dp,
    gradient: Brush = Brush.sweepGradient(listOf(Color.Gray, Color.White)),
    animationDuration: Int = 5000,
    onCardClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Infinite Color Animation")
    val degrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Infinite Colors"
    )

    Surface(
        modifier = modifier
            .clip(shape)
            .clickable { onCardClick() },
        shape = shape
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(borderWidth)
                .drawWithContent {
                    rotate(degrees = degrees) {
                        drawCircle(
                            brush = gradient,
                            radius = size.width,
                            blendMode = BlendMode.SrcIn,
                        )
                    }
                    drawContent()
                },
            color = MaterialTheme.colorScheme.surface,
            shape = shape
        ) {
            content()
        }
    }
}



