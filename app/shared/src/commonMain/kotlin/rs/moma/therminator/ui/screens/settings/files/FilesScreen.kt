package rs.moma.therminator.ui.screens.settings.files

import androidx.navigationevent.compose.rememberNavigationEventState
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.navigationevent.compose.NavigationBackHandler
import therminator.shared.generated.resources.ic_add_tracks
import androidx.compose.foundation.layout.asPaddingValues
import rs.moma.therminator.ui.dialogs.ConfirmationDialog
import rs.moma.therminator.ui.dialogs.CreateFolderDialog
import therminator.shared.generated.resources.ic_new_dir
import androidx.compose.foundation.layout.PaddingValues
import therminator.shared.generated.resources.ic_delete
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.rememberCoroutineScope
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import therminator.shared.generated.resources.ic_back
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBars
import rs.moma.therminator.viewmodels.FilesViewModel
import androidx.navigationevent.NavigationEventInfo
import io.github.vinceglb.filekit.core.PlatformFile
import rs.moma.therminator.data.models.FileItemType
import rs.moma.therminator.ui.dialogs.OfflineDialog
import rs.moma.therminator.viewmodels.MainViewModel
import androidx.compose.foundation.lazy.LazyColumn
import rs.moma.therminator.ui.utils.naturalCompare
import androidx.compose.foundation.layout.padding
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import therminator.shared.generated.resources.Res
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import rs.moma.therminator.ui.theme.OutlineColor
import androidx.compose.foundation.layout.width
import rs.moma.therminator.data.models.FileItem
import rs.moma.therminator.ui.theme.AccentColor
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.IconButton
import androidx.navigation.NavHostController
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import kotlinx.coroutines.launch

@Composable
fun FilesScreen(navController: NavHostController, topPadding: Int = 0) {
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val mainViewModel = koinInject<MainViewModel>()
    val filesViewModel = koinInject<FilesViewModel>()
    val items by filesViewModel.items.collectAsState()
    val isSelectOn by filesViewModel.isSelectOn.collectAsState()
    val currentPath by filesViewModel.currentPath.collectAsState()
    val isRefreshing by filesViewModel.isRefreshing.collectAsState()
    val isServerOffline by mainViewModel.isServerOffline.collectAsState()
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val filePicker = rememberFilePickerLauncher(
        type = PickerType.File(extensions = listOf("mp3", "flac", "wav", "m4a", "ogg")),
        mode = PickerMode.Multiple(),
        title = "Select audio files"
    ) { files: List<PlatformFile>? ->
        files?.let { platformFiles ->
            scope.launch {
                val fileData = platformFiles.map { file -> file.name to file.readBytes() }
                filesViewModel.uploadTracks(fileData)
            }
        }
    }

    LaunchedEffect(Unit) {
        filesViewModel.refresh()
    }

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = true,
        onBackCompleted = {
            if (isSelectOn) filesViewModel.toggleSelect(false)
            else if (currentPath == "/") navController.popBackStack()
            else filesViewModel.goBack()
        }
    )

    Box(Modifier.fillMaxSize().padding(top = topPadding.dp)) {
        Column(Modifier.fillMaxSize().imePadding()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(), start = 10.dp, end = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton({
                    if (isSelectOn) filesViewModel.toggleSelect(false)
                    else if (currentPath == "/") navController.popBackStack()
                    else filesViewModel.goBack()
                }) {
                    Icon(painterResource(Res.drawable.ic_back), "Log out", tint = OutlineColor)
                }
                Row {
                    if (isSelectOn) {
                        IconButton({ showDeleteConfirmation = true }) {
                            Icon(painterResource(Res.drawable.ic_delete), "Delete selected", tint = OutlineColor)
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                    IconButton({ showCreateFolderDialog = true }) {
                        Icon(painterResource(Res.drawable.ic_new_dir), "Create folder", tint = OutlineColor)
                    }
                }
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                modifier = Modifier.fillMaxSize(),
                onRefresh = { filesViewModel.refresh(true) }
            ) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    val sortedItems = items.filter { it.path == currentPath }
                        .sortedWith(compareBy<FileItem> { it.type != FileItemType.FOLDER }
                            .thenComparator { a, b -> naturalCompare(a.title.ifEmpty { a.name }, b.title.ifEmpty { b.name }) })
                    items(sortedItems, key = { it.id }) { item ->
                        FileItemCard(
                            item = item,
                            onClick = { filesViewModel.clicked(item) },
                            onLongClick = {
                                filesViewModel.toggleSelect(true)
                                filesViewModel.clicked(item)
                            }
                        )
                    }
                    item { Spacer(Modifier.height(96.dp)) }
                }
            }
        }

        FloatingActionButton(
            onClick = { filePicker.launch() },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = AccentColor,
            contentColor = Color.White
        ) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(Res.drawable.ic_add_tracks), "Add tracks", Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Text("Add tracks", fontSize = 16.sp, color = Color.White)
            }
        }
    }

    if (showCreateFolderDialog) {
        CreateFolderDialog({ folderName ->
            println(currentPath + folderName)
            filesViewModel.createFolder(currentPath + folderName)
            showCreateFolderDialog = false
        }) {
            showCreateFolderDialog = false
        }
    }

    if (showDeleteConfirmation) {
        val selectedFolders = items.filter { it.selected && it.type == FileItemType.FOLDER }
        val totalCount = items.count { it.selected && it.type == FileItemType.FILE } + items.count { item ->
            item.type == FileItemType.FILE && selectedFolders.any { folder -> item.path.startsWith(folder.path + folder.name + "/") }
        }
        ConfirmationDialog(
            title = "Delete $totalCount file${if (totalCount != 1) "s" else ""}?",
            message = "This action cannot be undone.",
            onConfirm = {
                filesViewModel.deleteSelectedFiles()
                showDeleteConfirmation = false
            },
            onDismiss = {
                showDeleteConfirmation = false
            }
        )
    }

    if (isServerOffline)
        OfflineDialog { mainViewModel.login() }
}