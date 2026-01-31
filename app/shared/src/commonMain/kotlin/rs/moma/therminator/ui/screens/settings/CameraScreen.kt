package rs.moma.therminator.ui.screens.settings

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import org.jetbrains.compose.resources.painterResource
import rs.moma.therminator.ui.components.DropdownField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import rs.moma.therminator.ui.components.NumericField
import rs.moma.therminator.viewmodels.CameraViewModel
import therminator.shared.generated.resources.ic_back
import androidx.compose.foundation.layout.statusBars
import rs.moma.therminator.ui.dialogs.OfflineDialog
import rs.moma.therminator.viewmodels.MainViewModel
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import therminator.shared.generated.resources.Res
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.DisposableEffect
import rs.moma.therminator.ui.theme.OutlineColor
import androidx.compose.ui.text.style.TextAlign
import rs.moma.therminator.ui.theme.AccentColor
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import rs.moma.therminator.ui.theme.CardColor
import androidx.compose.material3.IconButton
import androidx.navigation.NavHostController
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject


@Composable
fun CameraScreen(navController: NavHostController) {
    val focusManager = LocalFocusManager.current
    val mainViewModel = koinInject<MainViewModel>()
    val cameraViewModel = koinInject<CameraViewModel>()
    val frame by cameraViewModel.cameraFrame.collectAsState()
    val settings by cameraViewModel.cameraSettings.collectAsState()
    val isServerOffline by mainViewModel.isServerOffline.collectAsState()
    val isCameraOffline by cameraViewModel.isCameraOffline.collectAsState()

    var framerate by remember { mutableStateOf(0) }
    var resolution by remember { mutableStateOf(0) }
    var frames by remember { mutableStateOf<Int?>(null) }
    var pixels by remember { mutableStateOf<Int?>(null) }
    var temperature by remember { mutableStateOf<Float?>(null) }
    var applyMask by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        cameraViewModel.onEnter()
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraViewModel.endCameraWs()
        }
    }

    LaunchedEffect(settings) {
        settings?.let {
            framerate = it.framerate
            resolution = it.resolution
            frames = it.threshold.frames
            pixels = it.threshold.pixels
            temperature = it.threshold.temperature
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(), start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton({ navController.popBackStack() }) {
                Icon(painterResource(Res.drawable.ic_back), "Back", tint = OutlineColor)
            }
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
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
                        onClick = { cameraViewModel.startCameraWs() }
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
                                color = inferno(value, frame.min, frame.range, if (applyMask) temperature else null),
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

            Column(
                Modifier.wrapContentWidth().widthIn(max = 512.dp).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DropdownField("Resolution", resolution, ResolutionOptions, Modifier.weight(1f)) { resolution = it }
                    DropdownField("Frame Rate", framerate, FramerateOptions, Modifier.weight(1f)) { framerate = it }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumericField("Min temperature", temperature, Modifier.weight(1f)) { temperature = it }
                    Column {
                        Spacer(Modifier.height(8.dp))
                        Switch(checked = applyMask, onCheckedChange = { isChecked -> applyMask = isChecked })
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumericField("Min pixel count", pixels, Modifier.weight(3f)) { pixels = it }
                    NumericField("Min consecutive frames", frames, Modifier.weight(4f)) { frames = it }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                    modifier = Modifier.height(48.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        val temperature = temperature ?: return@Button
                        val pixels = pixels ?: return@Button
                        val frames = frames ?: return@Button
                        cameraViewModel.updateCameraSettings(resolution, framerate, temperature, pixels, frames)
                    }
                ) {
                    Text("Update", color = Color.White)
                }

                Spacer(Modifier.height(36.dp))
            }
        }
    }

    if (isServerOffline)
        OfflineDialog { mainViewModel.login() }
}

private fun inferno(value: Float, min: Float, range: Float, threshold: Float?): Color {
    val normalized = ((value - min) / range).coerceIn(0f, 1f)
    val dimFactor = if (threshold != null && value < threshold) 0.3f else 1f
    val red = (255 * (normalized * 1.2f).coerceIn(0f, 1f) * dimFactor).toInt()
    val green = (255 * (normalized * normalized) * dimFactor).toInt()
    val blue = (255 * (1f - normalized) * dimFactor).toInt()
    return Color(red, green, blue)
}

private val ResolutionOptions = listOf(
    0 to "16-bit",
    1 to "17-bit",
    2 to "18-bit",
    3 to "19-bit"
)

private val FramerateOptions = listOf(
    0 to "0.5 Hz",
    1 to "1 Hz",
    2 to "2 Hz",
    3 to "4 Hz",
    4 to "8 Hz",
    5 to "16 Hz",
    6 to "32 Hz",
    7 to "64 Hz"
)