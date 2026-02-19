package rs.moma.therminator.ui.screens

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.asPaddingValues
import rs.moma.therminator.viewmodels.AlarmListViewModel
import androidx.compose.foundation.layout.PaddingValues
import rs.moma.therminator.ui.utils.BatteryIconProvider
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FloatingActionButton
import org.jetbrains.compose.resources.painterResource
import rs.moma.therminator.ui.screens.alarms.AlarmCard
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBars
import rs.moma.therminator.viewmodels.AlarmViewModel
import rs.moma.therminator.ui.dialogs.OfflineDialog
import rs.moma.therminator.viewmodels.MainViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.mutableLongStateOf
import rs.moma.therminator.ui.utils.timeUntilAlarm
import androidx.compose.foundation.layout.padding
import therminator.shared.generated.resources.Res
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.font.FontWeight
import rs.moma.therminator.ui.navigation.Screen
import therminator.shared.generated.resources.*
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import rs.moma.therminator.ui.theme.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun HomeScreen(navController: NavHostController, topPadding: Int = 0) {
    val lazyListState = rememberLazyListState()
    val mainViewModel = koinInject<MainViewModel>()
    val alarmViewModel = koinInject<AlarmViewModel>()
    val alarmListViewModel = koinInject<AlarmListViewModel>()
    val alarms by alarmListViewModel.alarms.collectAsState()
    val batteryInfo by mainViewModel.batteryInfo.collectAsState()
    val isLoading by alarmListViewModel.isLoading.collectAsState()
    val isRefreshing by alarmListViewModel.isRefreshing.collectAsState()
    val isServerOffline by mainViewModel.isServerOffline.collectAsState()

    var tick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            tick++
        }
    }

    Box(Modifier.fillMaxSize().padding(top = topPadding.dp)) {
        Column(Modifier.fillMaxSize().imePadding()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(), start = 12.dp, end = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton({ mainViewModel.logout() }) {
                    Icon(painterResource(Res.drawable.ic_logout), "Log out", tint = OutlineColor)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    batteryInfo?.percentage?.let { Text("${it.roundToInt()}%", color = OutlineColor, fontSize = 20.sp) }
                    Icon(painterResource(BatteryIconProvider.get(batteryInfo)), "Battery icon", tint = OutlineColor)
                    Spacer(Modifier.width(4.dp))
                    IconButton({ navController.navigate(Screen.Settings) }) {
                        Icon(painterResource(Res.drawable.ic_settings), "Settings", tint = OutlineColor)
                    }
                }
            }

            val sortedAlarms = alarms.sortedWith(compareBy({ !it.active }, { it.hours }, { it.minutes }, { it.name }))
            val nextAlarm = sortedAlarms.firstOrNull { it.active }
            Row(
                Modifier.height(36.dp).padding(bottom = 6.dp, start = 26.dp, end = 26.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                if (nextAlarm == null) {
                    Text("No alarms set", color = PrimaryColor2, fontSize = 15.sp)
                } else timeUntilAlarm(nextAlarm).also { _ -> tick }.let {
                    val time = buildString {
                        if (it.first != 0L) append(" ${it.first}d")
                        if (it.first != 0L || it.second != 0L) append(" ${it.second}h")
                        append(" ${it.third}m")
                    }
                    Row {
                        Text("Next alarm in", Modifier.alignByBaseline(), color = PrimaryColor2, fontSize = 15.sp)
                        Text(time, Modifier.alignByBaseline(), color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                modifier = Modifier.fillMaxSize(),
                onRefresh = { alarmListViewModel.refresh(true) }
            ) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(sortedAlarms, key = { it.id!! }) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            onClick = {
                                alarmViewModel.select(alarm)
                                navController.navigate(Screen.Alarm)
                            },
                            onSwitchClicked = { active -> alarmListViewModel.toggleAlarm(alarm, active) }
                        )
                    }
                    item { Spacer(Modifier.height(96.dp)) }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                alarmViewModel.reset()
                navController.navigate(Screen.Alarm)
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            shape = RoundedCornerShape(12.dp),
            containerColor = AccentColor,
            contentColor = Color.White
        ) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(Res.drawable.ic_add_alarm), "Add alarm", Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Text("Add alarm", fontSize = 16.sp, color = Color.White)
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