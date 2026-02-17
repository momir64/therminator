package rs.moma.therminator.ui.screens.settings.camera

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.wrapContentWidth
import rs.moma.therminator.ui.components.DropdownField
import rs.moma.therminator.data.models.CameraSettings
import rs.moma.therminator.ui.components.NumericField
import androidx.compose.material3.ButtonDefaults
import rs.moma.therminator.ui.theme.AccentColor
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*

@Composable
fun CameraSettings(
    state: CameraSettings,
    applyMask: Boolean,
    onStateChange: (CameraSettings) -> Unit,
    onApplyMaskChange: (Boolean) -> Unit,
    onUpdate: () -> Unit
) {
    Column(
        Modifier.wrapContentWidth().widthIn(max = 512.dp).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DropdownField("Resolution", state.resolution, ResolutionOptions, Modifier.weight(1f)) {
                onStateChange(state.copy(resolution = it))
            }
            DropdownField("Frame Rate", state.framerate, FramerateOptions, Modifier.weight(1f)) {
                onStateChange(state.copy(framerate = it))
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumericField("Min temperature", state.threshold.temperature, Modifier.weight(1f)) {
                onStateChange(state.copy(threshold = state.threshold.copy(temperature = it)))
            }
            Column {
                Spacer(Modifier.height(8.dp))
                Switch(checked = applyMask, onCheckedChange = { isChecked -> onApplyMaskChange(isChecked) })
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumericField("Min pixel count", state.threshold.pixels, Modifier.weight(3f)) {
                onStateChange(state.copy(threshold = state.threshold.copy(pixels = it)))
            }
            NumericField("Min consecutive frames", state.threshold.frames, Modifier.weight(4f)) {
                onStateChange(state.copy(threshold = state.threshold.copy(frames = it)))
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
            modifier = Modifier.height(48.dp).fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            onClick = onUpdate
        ) {
            Text("Update", color = Color.White)
        }

        Spacer(Modifier.height(36.dp))
    }
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