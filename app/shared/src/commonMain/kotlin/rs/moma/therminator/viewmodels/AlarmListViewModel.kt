package rs.moma.therminator.viewmodels

import rs.moma.therminator.data.models.ResponseStatus
import rs.moma.therminator.data.models.AlarmInfo
import rs.moma.therminator.ui.utils.ToastService
import kotlinx.coroutines.flow.MutableStateFlow
import rs.moma.therminator.data.remote.RestApi
import org.koin.core.component.KoinComponent
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import org.koin.core.component.inject
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
import kotlin.getValue


class AlarmListViewModel : ViewModel(), KoinComponent {
    private val toastService: ToastService by inject()
    private val api: RestApi by inject()

    private val _alarms = MutableStateFlow<List<AlarmInfo>>(emptyList())
    val alarms = _alarms.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        _isLoading.value = true
        refresh()
    }

    private suspend fun fetchAlarms() {
        _alarms.value = api.getAlarms()
        _isLoading.value = false
    }

    fun refresh(showCircle: Boolean = false, onDone: () -> Unit = {}) = viewModelScope.launch {
        _isRefreshing.value = showCircle
        fetchAlarms()
        _isRefreshing.value = false
        onDone()
    }

    fun toggleAlarm(alarm: AlarmInfo, active: Boolean) = viewModelScope.launch {
        _alarms.value = _alarms.value.map { it.copy(active = if (it.id == alarm.id) active else it.active) }
        if (api.updateAlarm(alarm.copy(active = active)) != ResponseStatus.Successful) {
            toastService.show("Failed to update alarm")
            fetchAlarms()
        }
    }
}