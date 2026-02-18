package rs.moma.therminator.ui.screens.alarms

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.padding
import therminator.shared.generated.resources.Res
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.SwitchDefaults
import rs.moma.therminator.data.models.AlarmInfo
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.font.FontWeight
import therminator.shared.generated.resources.*
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Switch
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


@Composable
fun AlarmCard(
    alarm: AlarmInfo,
    onClick: () -> Unit,
    onSwitchClicked: (Boolean) -> Unit = {}
) {
    val textColor = if (alarm.active) Color.White else OutlineColor.copy(alpha = 0.75f)
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (alarm.active) ButtonColor2 else CardColor)
    ) {
        Row(Modifier.fillMaxSize().height(IntrinsicSize.Min).padding(horizontal = 12.dp)) {
            Column(Modifier.padding(vertical = 10.dp)) {
                Text(
                    "${alarm.hours.pad()}:${alarm.minutes.pad()}",
                    Modifier.padding(bottom = 6.dp),
                    fontWeight = FontWeight.Medium,
                    fontSize = 36.sp,
                    color = textColor
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(if (alarm.active) Res.drawable.ic_alarm_filled else Res.drawable.ic_alarm),
                        contentDescription = alarm.name,
                        modifier = Modifier.size(20.dp),
                        tint = textColor
                    )
                    Spacer(Modifier.width(4.dp))
                    val days = when {
                        alarm.days.containsAll(listOf(0, 1, 2, 3, 4, 5, 6)) || alarm.days.isEmpty() -> "EVERY DAY"
                        alarm.days.containsAll(listOf(0, 1, 2, 3, 4)) && alarm.days.size == 5 -> "WEEKDAYS"
                        alarm.days.containsAll(listOf(5, 6)) && alarm.days.size == 2 -> "WEEKENDS"
                        else -> null
                    }
                    if (days != null) {
                        Text(days, Modifier.padding(start = 4.dp), fontSize = 17.sp, color = textColor, fontWeight = FontWeight.Light)
                    } else {
                        for (i in 0..6) {
                            Text(
                                week[i], Modifier.padding(start = 4.dp), fontSize = 17.sp,
                                fontWeight = if (alarm.days.contains(i)) FontWeight.Medium else FontWeight.Light,
                                color = if (alarm.days.contains(i)) textColor else textColor.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(
                Modifier.fillMaxHeight().weight(1f).padding(bottom = 10.dp, top = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Switch(
                    checked = alarm.active,
                    onCheckedChange = onSwitchClicked,
                    thumbContent = if (!alarm.active) {
                        { Spacer(Modifier.size(SwitchDefaults.IconSize)) }
                    } else null,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = OutlineColor,
                        uncheckedThumbColor = OutlineColor,
                        uncheckedTrackColor = ButtonColor,
                        uncheckedBorderColor = Color.Transparent
                    )
                )
                Text(alarm.name, overflow = TextOverflow.Ellipsis, maxLines = 1, fontSize = 16.sp, color = textColor, fontWeight = FontWeight.Light)
            }
        }
    }
}

private val week = arrayOf("M", "T", "W", "T", "F", "S", "S")