package rs.moma.therminator.viewmodels

import rs.moma.therminator.data.models.ResponseStatus.*
import rs.moma.therminator.data.models.CameraThreshold
import rs.moma.therminator.data.models.CameraSettings
import rs.moma.therminator.data.remote.AuthProvider
import rs.moma.therminator.data.models.CameraFrame
import io.ktor.client.plugins.websocket.webSocket
import rs.moma.therminator.ui.utils.ToastService
import kotlinx.coroutines.flow.MutableStateFlow
import rs.moma.therminator.data.remote.RestApi
import org.koin.core.component.KoinComponent
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import org.koin.core.component.inject
import androidx.lifecycle.ViewModel
import io.ktor.websocket.readText
import io.ktor.client.HttpClient
import kotlinx.coroutines.launch
import io.ktor.websocket.Frame
import kotlinx.coroutines.Job
import kotlin.getValue


class CameraViewModel : ViewModel(), KoinComponent {
    private val toastService: ToastService by inject()
    private val httpClient: HttpClient by inject()
    private val api: RestApi by inject()
    private var cameraWsJob: Job? = null

    private val _isCameraOffline = MutableStateFlow(false)
    val isCameraOffline = _isCameraOffline.asStateFlow()

    private val _cameraSettings = MutableStateFlow<CameraSettings?>(null)
    val cameraSettings = _cameraSettings.asStateFlow()

    private val _cameraFrame = MutableStateFlow<CameraFrame?>(null)
    val cameraFrame = _cameraFrame.asStateFlow()

    init {
        viewModelScope.launch { getCameraSettings() }
        startCameraWs()
    }

    fun startCameraWs() {
        if (cameraWsJob?.isActive == true) return
        val header = AuthProvider.getHeaderValue() ?: return
        cameraWsJob = viewModelScope.launch {
            try {
                val wsBaseUrl = api.baseUrl.replaceFirst("http", "ws")
                httpClient.webSocket("${wsBaseUrl}/camera") {
                    send(Frame.Text(header))
                    _isCameraOffline.value = false
                    for (frame in incoming)
                        if (frame is Frame.Text)
                            _cameraFrame.emit(CameraFrame(frame.readText()))
                }
            } catch (e: Exception) {
                println("Camera WebSocket error: $e")
            } finally {
                _isCameraOffline.value = true
                _cameraFrame.value = null
                cameraWsJob = null
            }
        }
    }

    private suspend fun getCameraSettings() {
        _cameraSettings.value = api.getCameraSettings()
    }

    fun updateCameraSettings(resolution: Int, framerate: Int, temperature: Float, pixels: Int, frames: Int) = viewModelScope.launch {
        val cameraSettings = CameraSettings(resolution, framerate, CameraThreshold(temperature, pixels, frames))
        when (api.updateCameraSettings(cameraSettings)) {
            Successful -> toastService.show("Camera settings updated successfully")
            else -> toastService.show("Failed to update camera settings")
        }
        _cameraSettings.value = cameraSettings
    }
}