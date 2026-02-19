package rs.moma.therminator.ui.utils

import rs.moma.therminator.data.models.AlarmInfo
import kotlin.time.Instant
import kotlinx.datetime.*
import kotlin.time.Clock

fun minutesUntilAlarm(alarm: AlarmInfo, now: Instant = Clock.System.now()): Long {
    val timeZone = TimeZone.currentSystemDefault()
    val current = now.toLocalDateTime(timeZone)
    val alarmTime = (0..7).firstNotNullOf { daysAhead ->
        val date = current.date.plus(daysAhead, DateTimeUnit.DAY)
        val candidate = LocalDateTime(date.year, date.month, date.day, alarm.hours, alarm.minutes)
        candidate.takeIf { it > current && (alarm.days.isEmpty() || alarm.days.contains(date.dayOfWeek.ordinal % 7)) }
    }
    return (alarmTime.toInstant(timeZone) - now).inWholeMinutes
}

fun timeUntilAlarm(alarm: AlarmInfo, now: Instant = Clock.System.now()): Triple<Long, Long, Long> {
    val difference = minutesUntilAlarm(alarm, now)
    return Triple(difference / (60 * 24), difference / 60 % 24, difference % 60)
}