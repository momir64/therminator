package rs.moma.therminator.viewmodels

import rs.moma.therminator.data.remote.NetworkResponseService
import rs.moma.therminator.data.remote.HttpClientManager
import rs.moma.therminator.data.models.ResponseStatus.*
import rs.moma.therminator.data.remote.AuthProvider
import rs.moma.therminator.data.models.BatteryInfo
import io.ktor.client.plugins.websocket.webSocket
import rs.moma.therminator.data.local.SecureStore
import rs.moma.therminator.ui.utils.ToastService
import kotlinx.coroutines.flow.MutableStateFlow
import rs.moma.therminator.data.remote.RestApi
import org.koin.core.component.KoinComponent
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.inject
import androidx.lifecycle.ViewModel
import io.ktor.websocket.readText
import io.ktor.client.HttpClient
import kotlinx.coroutines.launch
import io.ktor.websocket.Frame
import kotlinx.coroutines.Job

class MainViewModel : ViewModel(), KoinComponent {
    private val httpClientManager: HttpClientManager by inject()
    private val toastService: ToastService by inject()
    private val secureStore: SecureStore by inject()
    private val httpClient: HttpClient by inject()
    private var batteryWsJob: Job? = null
    private val api: RestApi by inject()

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _isServerOffline = MutableStateFlow(false)
    val isServerOffline = _isServerOffline.asStateFlow()

    private val _batteryInfo = MutableStateFlow<BatteryInfo?>(null)
    val batteryInfo = _batteryInfo.asStateFlow()

    init {
        httpClientManager.setScope(viewModelScope)

        NetworkResponseService.response.onEach {
            when (it) {
                Unauthorized -> {
                    toastService.show("Authentication failed, wrong password")
                    logout()
                }

                Error -> {
                    _isLoggedIn.value = secureStore.load() != null
                    _isServerOffline.value = true
                    println("Server unreachable")
                }

                Successful, Unsuccessful -> {
                    _isServerOffline.value = false
                    connectBatteryWebSocket()
                }
            }
        }.launchIn(viewModelScope)

        login(displayToast = false)
    }

    fun connectBatteryWebSocket() {
        if (batteryWsJob?.isActive == true) return
        val header = AuthProvider.getHeaderValue() ?: return
        batteryWsJob = viewModelScope.launch {
            try {
                val wsBaseUrl = api.baseUrl.replaceFirst("http", "ws")
                httpClient.webSocket("${wsBaseUrl}/battery") {
                    send(Frame.Text(header))
                    for (frame in incoming)
                        if (frame is Frame.Text)
                            _batteryInfo.emit(BatteryInfo(frame.readText()))
                }
            } catch (e: Exception) {
                println("Battery WebSocket error: $e")
            } finally {
                _isServerOffline.value = true
                batteryWsJob = null
            }
        }
    }

    fun login(password: String? = null, displayToast: Boolean = true) = viewModelScope.launch {
        val savedPassword = secureStore.load()
        val password = password ?: savedPassword
        AuthProvider.password = password

        if (password != null) api.ping().let {
            if (it == Successful) {
                secureStore.save(password)
                _isLoggedIn.value = true
            } else if (it == Error && displayToast) {
                toastService.show("Failed to connect to the server")
            }
        }

        _isLoggedIn.value = _isLoggedIn.value ?: (secureStore.load() != null)
    }

    fun logout() = viewModelScope.launch {
        secureStore.clear()
        AuthProvider.password = null
        _isLoggedIn.value = false
    }
}