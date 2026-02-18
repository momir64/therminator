package rs.moma.therminator.ui.screens.alarms

import androidx.navigationevent.compose.rememberNavigationEventState
import rs.moma.therminator.ui.screens.settings.files.FileItemCard
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBars
import rs.moma.therminator.viewmodels.AlarmViewModel
import rs.moma.therminator.viewmodels.FilesViewModel
import androidx.navigationevent.NavigationEventInfo
import rs.moma.therminator.data.models.FileItemType
import rs.moma.therminator.ui.dialogs.OfflineDialog
import rs.moma.therminator.viewmodels.MainViewModel
import androidx.compose.foundation.lazy.LazyColumn
import rs.moma.therminator.ui.utils.naturalCompare
import androidx.compose.foundation.layout.padding
import therminator.shared.generated.resources.Res
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import rs.moma.therminator.ui.theme.OutlineColor
import rs.moma.therminator.data.models.FileItem
import therminator.shared.generated.resources.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.IconButton
import androidx.navigation.NavHostController
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject

@Composable
fun TrackSelectorScreen(navController: NavHostController, topPadding: Int = 0) {
    val lazyListState = rememberLazyListState()
    val mainViewModel = koinInject<MainViewModel>()
    val filesViewModel = koinInject<FilesViewModel>()
    val alarmViewModel = koinInject<AlarmViewModel>()
    val items by filesViewModel.items.collectAsState()
    val alarm by alarmViewModel.alarm.collectAsState()
    val isLoading by filesViewModel.isLoading.collectAsState()
    val currentPath by filesViewModel.currentPath.collectAsState()
    val isRefreshing by filesViewModel.isRefreshing.collectAsState()
    val isServerOffline by mainViewModel.isServerOffline.collectAsState()

    LaunchedEffect(Unit) {
        filesViewModel.refresh()
    }

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = true,
        onBackCompleted = {
            if (currentPath == "/") navController.popBackStack()
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
                    if (currentPath == "/") navController.popBackStack()
                    else filesViewModel.goBack()
                }) {
                    Icon(painterResource(Res.drawable.ic_back), "Log out", tint = OutlineColor)
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
                        val checked = alarmViewModel.getCheckStatus(alarm, item, items)
                        FileItemCard(
                            item = item,
                            onClick = {
                                if (item.type == FileItemType.FILE)
                                    alarmViewModel.updateTracks(item, !checked, items)
                                filesViewModel.clicked(item)
                            },
                            onCheckClicked = { checked -> alarmViewModel.updateTracks(item, checked, items) },
                            checked = checked
                        )
                    }
                    item { Spacer(Modifier.height(96.dp)) }
                }
            }
        }
    }

    if (isLoading) {
        Box(
            Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }

    if (isServerOffline)
        OfflineDialog { mainViewModel.login() }
}