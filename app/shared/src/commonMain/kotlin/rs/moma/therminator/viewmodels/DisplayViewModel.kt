package rs.moma.therminator.viewmodels

import rs.moma.therminator.data.models.ResponseStatus
import rs.moma.therminator.data.models.LocationInfo
import rs.moma.therminator.data.models.DisplayInfo
import rs.moma.therminator.ui.utils.ToastService
import kotlinx.coroutines.flow.MutableStateFlow
import rs.moma.therminator.data.remote.RestApi
import org.koin.core.component.KoinComponent
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import org.koin.core.component.inject
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.getValue


class DisplayViewModel : ViewModel(), KoinComponent {
    private val toastService: ToastService by inject()
    private val api: RestApi by inject()

    private val _location = MutableStateFlow(LocationInfo())
    val location = _location.asStateFlow()

    private val _display = MutableStateFlow<DisplayInfo?>(null)
    val display = _display.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun fetch() = viewModelScope.launch {
        _isLoading.value = true
        _location.value = api.getLocation()
        _display.value = api.getDisplay()
        _isLoading.value = false
    }

    fun setBrightness(brightness: Float) {
        _display.value = _display.value?.copy(brightness = brightness.roundToInt())
    }

    fun setAddress(address: String) {
        _location.value = LocationInfo(address)
    }

    fun findLocation() = viewModelScope.launch { checkLocation() }
    private suspend fun checkLocation() {
        _location.value = api.geocodeLocation(_location.value.location)
    }

    fun saveSettings() = viewModelScope.launch {
        var error = false

        if (_location.value.latitude == null || _location.value.longitude == null)
            checkLocation()

        if (api.updateLocation(_location.value) != ResponseStatus.Successful) {
            toastService.show("Failed to update location settings")
            error = true
        }

        _display.value?.takeIf { api.updateDisplay(it) != ResponseStatus.Successful }?.also {
            toastService.show("Failed to update display settings")
            error = true
        }

        if (!error) toastService.show("Settings saved successfully")
    }
}