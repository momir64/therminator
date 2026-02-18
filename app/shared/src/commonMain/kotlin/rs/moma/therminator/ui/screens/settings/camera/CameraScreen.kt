package rs.moma.therminator.ui.screens.settings.camera

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import org.jetbrains.compose.resources.painterResource
import rs.moma.therminator.data.models.CameraThreshold
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import rs.moma.therminator.data.models.CameraSettings
import rs.moma.therminator.viewmodels.CameraViewModel
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBars
import rs.moma.therminator.ui.dialogs.OfflineDialog
import rs.moma.therminator.viewmodels.MainViewModel
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import therminator.shared.generated.resources.Res
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.DisposableEffect
import rs.moma.therminator.ui.theme.OutlineColor
import rs.moma.therminator.ui.theme.AccentColor
import therminator.shared.generated.resources.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.IconButton
import androidx.navigation.NavHostController
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject

@Composable
fun CameraScreen(navController: NavHostController, topPadding: Int = 0) {
    val focusManager = LocalFocusManager.current
    val mainViewModel = koinInject<MainViewModel>()
    val cameraViewModel = koinInject<CameraViewModel>()
    val frame by cameraViewModel.cameraFrame.collectAsState()
    val settings by cameraViewModel.cameraSettings.collectAsState()
    val isServerOffline by mainViewModel.isServerOffline.collectAsState()
    val isCameraOffline by cameraViewModel.isCameraOffline.collectAsState()

    var settingsState by remember { mutableStateOf(CameraSettings(0, 0, CameraThreshold(null, null, null))) }
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
        settings?.let { settingsState = it }
    }

    Column(
        Modifier.fillMaxSize().padding(top = topPadding.dp).imePadding()
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(), start = 10.dp, end = 12.dp),
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

            CameraDisplay(
                isCameraOffline = isCameraOffline,
                frame = frame,
                threshold = if (applyMask) settingsState.threshold.temperature else null,
                onRetry = { cameraViewModel.startCameraWs() }
            )

            CameraSettings(
                state = settingsState,
                applyMask = applyMask,
                onStateChange = { settingsState = it },
                onApplyMaskChange = { applyMask = it }
            )

            Spacer(Modifier.weight(1f))

            Spacer(Modifier.height(16.dp))

            Button(
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp).height(48.dp),
                shape = RoundedCornerShape(8.dp),
                onClick = {
                    val temp = settingsState.threshold.temperature ?: return@Button
                    val pix = settingsState.threshold.pixels ?: return@Button
                    val fr = settingsState.threshold.frames ?: return@Button
                    cameraViewModel.updateCameraSettings(settingsState.resolution, settingsState.framerate, temp, pix, fr)
                }
            ) {
                Text("Save", color = Color.White)
            }
        }
    }

    if (isServerOffline)
        OfflineDialog { mainViewModel.login() }
}