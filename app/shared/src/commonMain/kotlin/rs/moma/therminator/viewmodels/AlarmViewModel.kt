package rs.moma.therminator.viewmodels

import rs.moma.therminator.data.models.ResponseStatus
import rs.moma.therminator.data.models.FileItemType
import rs.moma.therminator.data.models.AlarmInfo
import rs.moma.therminator.ui.utils.ToastService
import kotlinx.coroutines.flow.MutableStateFlow
import rs.moma.therminator.data.models.FileItem
import rs.moma.therminator.data.remote.RestApi
import org.koin.core.component.KoinComponent
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import org.koin.core.component.inject
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
import kotlin.getValue

class AlarmViewModel : ViewModel(), KoinComponent {
    private val toastService: ToastService by inject()
    private val api: RestApi by inject()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _alarm = MutableStateFlow<AlarmInfo?>(null)
    val alarm = _alarm.asStateFlow()

    fun select(alarm: AlarmInfo) {
        _alarm.value = alarm
    }

    fun reset() {
        _alarm.value = null
    }

    fun test(alarm: AlarmInfo) = viewModelScope.launch {
        if (api.testAlarm(alarm) != ResponseStatus.Successful)
            toastService.show("Failed to start the test")
    }

    fun save(alarm: AlarmInfo, onSuccess: () -> Unit) = viewModelScope.launch {
        _isLoading.value = true
        if (api.updateAlarm(alarm.copy(active = true)) == ResponseStatus.Successful) onSuccess()
        else toastService.show("Failed to save alarm")
        _isLoading.value = false
    }

    fun delete(alarm: AlarmInfo, onSuccess: () -> Unit) = viewModelScope.launch {
        _isLoading.value = true
        if (api.deleteAlarm(alarm) == ResponseStatus.Successful) onSuccess()
        else toastService.show("Failed to delete alarm")
        _isLoading.value = false
    }

    fun getCheckStatus(alarm: AlarmInfo?, item: FileItem, allItems: List<FileItem>): Boolean {
        if (item.type == FileItemType.FILE) return alarm?.tracks?.contains((item.path + item.name)) == true
        val folderFiles = allItems.filter { it.type == FileItemType.FILE && it.path.startsWith(item.path + item.name) }
        return folderFiles.isNotEmpty() && folderFiles.all { alarm?.tracks?.contains(it.path + it.name) == true }
    }

    fun updateTracks(item: FileItem, checked: Boolean, allItems: List<FileItem>) {
        if (item.type == FileItemType.FILE && checked)
            _alarm.value = _alarm.value?.copy(tracks = _alarm.value?.tracks?.toMutableList()?.apply { add(item.path + item.name) } ?: mutableListOf(item.path + item.name))
        else if (item.type == FileItemType.FILE && !checked)
            _alarm.value = _alarm.value?.copy(tracks = _alarm.value?.tracks?.toMutableList()?.apply { remove(item.path + item.name) } ?: emptyList())
        else if (item.type == FileItemType.FOLDER && checked) {
            val newTracks = allItems.filter { it.type == FileItemType.FILE && it.path.startsWith(item.path + item.name) }.map { it.path + it.name }
            _alarm.value = _alarm.value?.copy(tracks = _alarm.value?.tracks?.toMutableList()?.apply { addAll(newTracks.filter { it !in this }) } ?: newTracks)
        } else if (item.type == FileItemType.FOLDER && !checked)
            _alarm.value = _alarm.value?.copy(tracks = _alarm.value?.tracks?.filter { !it.startsWith(item.path + item.name) } ?: emptyList())
    }
}