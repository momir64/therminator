package rs.moma.therminator.ui.screens.alarms

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.asPaddingValues
import rs.moma.therminator.ui.dialogs.ConfirmationDialog
import rs.moma.therminator.viewmodels.AlarmListViewModel
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import org.jetbrains.compose.resources.painterResource
import rs.moma.therminator.ui.dialogs.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBars
import rs.moma.therminator.viewmodels.AlarmViewModel
import androidx.compose.material3.OutlinedTextField
import rs.moma.therminator.ui.dialogs.OfflineDialog
import rs.moma.therminator.viewmodels.MainViewModel
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import therminator.shared.generated.resources.Res
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SliderDefaults
import rs.moma.therminator.data.models.AlarmInfo
import rs.moma.therminator.ui.utils.ToastService
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import rs.moma.therminator.ui.navigation.Screen
import therminator.shared.generated.resources.*
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import rs.moma.therminator.data.models.Speaker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.clickable
import androidx.compose.material3.IconButton
import androidx.navigation.NavHostController
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import rs.moma.therminator.ui.utils.pad
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import rs.moma.therminator.ui.theme.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AlarmScreen(navController: NavHostController, topPadding: Int = 0) {
    val focusManager = LocalFocusManager.current
    val toastService = koinInject<ToastService>()
    val mainViewModel = koinInject<MainViewModel>()
    val alarmViewModel = koinInject<AlarmViewModel>()
    val alarmListViewModel = koinInject<AlarmListViewModel>()
    val isServerOffline by mainViewModel.isServerOffline.collectAsState()
    val isLoading by alarmViewModel.isLoading.collectAsState()
    val vmAlarm by alarmViewModel.alarm.collectAsState()

    var alarm by remember(vmAlarm) { mutableStateOf(vmAlarm ?: AlarmInfo()) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Box(
        Modifier.fillMaxSize().padding(top = topPadding.dp).imePadding()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusManager.clearFocus() },
        contentAlignment = Alignment.Center
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth()
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(), start = 10.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton({ navController.popBackStack() }) {
                    Icon(painterResource(Res.drawable.ic_back), "Back", tint = OutlineColor)
                }
                Row {
                    if (alarm.id != null) {
                        IconButton({ showDeleteConfirmation = true }) {
                            Icon(painterResource(Res.drawable.ic_delete), "Delete alarm", tint = OutlineColor)
                        }
                        Spacer(Modifier.width(4.dp))
                        IconButton({
                            alarm = alarm.copy(id = null)
                            toastService.show("Alarm will be saved as a copy")
                        }) {
                            Icon(painterResource(Res.drawable.ic_copy), "Copy alarm", tint = OutlineColor)
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                    IconButton(onClick = {
                        alarmViewModel.save(alarm) {
                            alarmListViewModel.refresh {
                                navController.popBackStack()
                            }
                        }
                    }) {
                        Icon(painterResource(Res.drawable.ic_save), "Save", tint = OutlineColor)
                    }
                }
            }

            Column(Modifier.fillMaxHeight().verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(24.dp))

                Column(Modifier.padding(horizontal = 24.dp)) {
                    Row(
                        Modifier.fillMaxWidth().clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showTimePicker = true },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(alarm.hours, alarm.minutes).forEachIndexed { index, value ->
                            if (index > 0) Text(":", Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 72.sp)
                            Box(
                                Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(ButtonColor2),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(value.pad(), Modifier.padding(vertical = 30.dp), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 72.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    OutlinedTextField(
                        value = alarm.name,
                        onValueChange = { alarm = alarm.copy(name = it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Alarm name") },
                        singleLine = true,
                    )

                    Spacer(Modifier.height(24.dp))

                    Card(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                alarmViewModel.select(alarm)
                                navController.navigate(Screen.TrackSelector)
                            },
                        colors = CardDefaults.cardColors(containerColor = ButtonColor2),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painterResource(Res.drawable.ic_track),
                                contentDescription = "Tracks icon",
                                Modifier.size(28.dp),
                                tint = PrimaryColor2
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Select alarm tracks", modifier = Modifier.padding(8.dp))
                            Spacer(Modifier.weight(1f))
                            Icon(
                                painterResource(Res.drawable.ic_enter),
                                contentDescription = "Select alarm tracks",
                                Modifier.size(20.dp),
                                tint = PrimaryColor2
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text("Repeat", color = PrimaryColor, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su").forEachIndexed { index, label ->
                            val selected = alarm.days.contains(index)
                            Box(
                                Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(24))
                                    .background(if (selected) AccentColor else ButtonColor2)
                                    .clickable { alarm = alarm.copy(days = if (selected) alarm.days - index else alarm.days + index) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label, fontSize = 14.sp, color = if (selected) Color.White else PrimaryColor,
                                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    Text("Speaker", color = PrimaryColor, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Speaker.entries.forEach { option ->
                            val selected = alarm.speaker == option
                            Box(
                                Modifier.weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) AccentColor else ButtonColor2)
                                    .clickable { alarm = alarm.copy(speaker = option) }
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    option.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                                    color = if (selected) Color.White else PrimaryColor,
                                    fontSize = 16.sp,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Slider(
                            modifier = Modifier.weight(1f),
                            value = alarm.volume.toFloat(),
                            onValueChange = { alarm = alarm.copy(volume = it.roundToInt().coerceIn(1, 100)) },
                            valueRange = -10f..111f,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentColor,
                                activeTrackColor = AccentColor,
                                inactiveTrackColor = ButtonColor2
                            )
                        )
                        Text(
                            "${alarm.volume}%",
                            Modifier.width(52.dp),
                            textAlign = TextAlign.End,
                            color = PrimaryColor,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(Modifier.weight(1f))
                Spacer(Modifier.height(28.dp))

                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp).height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    onClick = { alarmViewModel.test(alarm) }
                ) {
                    Text("SOUND TEST", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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

    if (showTimePicker) {
        TimePickerDialog(
            initialHour = alarm.hours,
            initialMinute = alarm.minutes,
            onConfirm = { hour, minute ->
                alarm = alarm.copy(hours = hour, minutes = minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    if (showDeleteConfirmation) {
        ConfirmationDialog(
            title = "Delete this alarm?",
            message = "This action cannot be undone.",
            onConfirm = {
                alarmViewModel.delete(alarm) {
                    alarmListViewModel.refresh {
                        navController.popBackStack()
                    }
                }
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