package rs.moma.therminator.viewmodels

import rs.moma.therminator.data.models.ResponseStatus
import rs.moma.therminator.data.models.FileItemType
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


class FilesViewModel : ViewModel(), KoinComponent {
    private val toastService: ToastService by inject()
    private val api: RestApi by inject()

    private val _items = MutableStateFlow<List<FileItem>>(emptyList())
    val items = _items.asStateFlow()

    private val _currentPath = MutableStateFlow("/")
    val currentPath = _currentPath.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _isSelectOn = MutableStateFlow(false)
    val isSelectOn = _isSelectOn.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        _isLoading.value = true
        refresh()
    }

    private suspend fun fetchFileItems() {
        _items.value = api.getFileItems()
        _isLoading.value = false
    }

    fun refresh(showCircle: Boolean = false) = viewModelScope.launch {
        _isRefreshing.value = showCircle
        fetchFileItems()
        _isRefreshing.value = false
    }

    private fun navigateTo(path: String) {
        _currentPath.value = "$path/"
    }

    fun goBack() {
        navigateTo(_currentPath.value.trimEnd('/').substringBeforeLast("/"))
    }

    fun clicked(item: FileItem) {
        if (_isSelectOn.value) {
            _items.value = _items.value.map { if (it.id == item.id) it.copy(selected = !it.selected) else it }
            _isSelectOn.value = _items.value.any { it.selected }
        } else if (item.type == FileItemType.FOLDER) {
            navigateTo(item.path + item.name)
        }
    }

    fun toggleSelect(on: Boolean) {
        if (!on) _items.value = _items.value.map { it.copy(selected = false) }
        _isSelectOn.value = on
    }

    fun createFolder(path: String) = viewModelScope.launch {
        _isLoading.value = true
        when (api.createFolder(path)) {
            ResponseStatus.Successful -> fetchFileItems()
            else -> {
                toastService.show("Failed to create the folder")
                _isLoading.value = false
            }
        }
    }

    fun uploadTracks(files: List<Pair<String, ByteArray>>) = viewModelScope.launch {
        _isLoading.value = true
        when (api.uploadTracks(_currentPath.value, files)) {
            ResponseStatus.Successful -> fetchFileItems()
            else -> {
                toastService.show("Failed to upload tracks")
                _isLoading.value = false
            }
        }
    }

    fun deleteSelectedFiles() = viewModelScope.launch {
        val paths = _items.value.filter { it.selected }.map { it.path + it.name }
        _isLoading.value = true
        toggleSelect(false)
        when (api.deleteFiles(paths)) {
            ResponseStatus.Successful -> fetchFileItems()
            else -> {
                toastService.show("Failed to delete files")
                _isLoading.value = false
            }
        }
    }
}