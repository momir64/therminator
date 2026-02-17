package rs.moma.therminator.ui.screens.settings.camera

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.wrapContentWidth
import rs.moma.therminator.data.models.CameraFrame
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.text.style.TextAlign
import rs.moma.therminator.ui.theme.AccentColor
import androidx.compose.foundation.background
import rs.moma.therminator.ui.theme.CardColor
import androidx.compose.foundation.layout.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.ui.geometry.Size
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*

@Composable
fun CameraDisplay(frame: CameraFrame?, isCameraOffline: Boolean, threshold: Float?, onRetry: () -> Unit) {
    if (isCameraOffline) {
        Column(
            Modifier
                .wrapContentWidth()
                .widthIn(max = 512.dp)
                .padding(horizontal = 24.dp)
                .aspectRatio(32f / 24f)
                .background(CardColor, RoundedCornerShape(12.dp)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(4.dp))
            Text("Connection to camera was lost.", fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(28.dp))
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                modifier = Modifier.size(96.dp, 42.dp),
                shape = RoundedCornerShape(8.dp),
                onClick = onRetry
            ) {
                Text("Retry", color = Color.White)
            }
        }
    } else frame?.let { frame ->
        Canvas(
            Modifier
                .wrapContentWidth()
                .widthIn(max = 512.dp)
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(12.dp))
                .aspectRatio(frame.width.toFloat() / frame.height)
        ) {
            val cellWidth = size.width / frame.width
            val cellHeight = size.height / frame.height
            frame.data.forEachIndexed { y, row ->
                row.forEachIndexed { x, value ->
                    drawRect(
                        color = inferno(value, frame.min, frame.range, threshold),
                        topLeft = Offset(x * cellWidth, y * cellHeight),
                        size = Size(cellWidth + 0.5f, cellHeight + 0.5f)
                    )
                }
            }
        }
    } ?: Box(
        Modifier
            .wrapContentWidth()
            .widthIn(max = 512.dp)
            .padding(horizontal = 24.dp)
            .aspectRatio(32f / 24f)
            .background(CardColor, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) { CircularProgressIndicator() }
}

private fun inferno(value: Float, min: Float, range: Float, threshold: Float?): Color {
    val normalized = ((value - min) / range).coerceIn(0f, 1f)
    val dimFactor = if (threshold != null && value < threshold) 0.3f else 1f
    val red = (255 * (normalized * 1.2f).coerceIn(0f, 1f) * dimFactor).toInt()
    val green = (255 * (normalized * normalized) * dimFactor).toInt()
    val blue = (255 * (1f - normalized) * dimFactor).toInt()
    return Color(red, green, blue)
}