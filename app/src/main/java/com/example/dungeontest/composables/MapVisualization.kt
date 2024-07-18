package com.example.dungeontest.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import coil.size.Size
import okhttp3.HttpUrl
import kotlin.math.roundToInt

@Composable
fun MapVisualization(dotAsString: String) {
    val currPrimaryColor = with(MaterialTheme.colorScheme.primary) {
        String.format("%.1f %.1f %.1f", red, green, blue)
    }
    val currSecondaryColor = with(MaterialTheme.colorScheme.secondary) {
        String.format("%.1f %.1f %.1f", red, green, blue)
    }
    val nodeShape = "circle"
    val bgColor = "transparent"

    // colorize the dot string
    val colorizedDotAsString = dotAsString
        .replace(
            "{",
            """{
            |bgcolor = $bgColor; 
            |node [color="$currPrimaryColor", shape=$nodeShape, fontcolor="$currPrimaryColor", penwidth=2.5];  
            |edge [color="$currSecondaryColor", penwidth=1.5];  
            |size = "50,50!";  
            |beautify = true;
            """.trimMargin()
        )

    // generate the required URL
    val graphUrl = HttpUrl.Builder()
        .scheme("https")
        .host("quickchart.io")
        .addPathSegment("graphviz")
        .addQueryParameter("graph", colorizedDotAsString)
        .addQueryParameter("format", "svg")
        .build()

    // display the image
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceAtLeast(1f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // reset back to default. Maybe incorporate animation to smoothen the effect later.
                        // also zoom focus area? Not too important but a nice QOL improvement if ever revisited
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .decoderFactory(SvgDecoder.Factory())
                .data(graphUrl)
                .size(Size.ORIGINAL) // Set the target size to load the image at.
                .build()
        )
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .graphicsLayer(scaleX = scale, scaleY = scale)
        )
    }

}