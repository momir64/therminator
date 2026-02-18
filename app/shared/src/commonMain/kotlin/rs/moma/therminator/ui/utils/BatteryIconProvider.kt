package rs.moma.therminator.ui.utils

import org.jetbrains.compose.resources.DrawableResource
import rs.moma.therminator.data.models.BatteryInfo
import therminator.shared.generated.resources.Res
import therminator.shared.generated.resources.*

object BatteryIconProvider {
    fun get(batteryInfo: BatteryInfo?): DrawableResource {
        if (batteryInfo == null) return Res.drawable.ic_no_battery
        val percentage = batteryInfo.percentage
        val charging = batteryInfo.isCharging
        if (charging == null || percentage == null) return Res.drawable.ic_no_battery

        return when {
            !charging && percentage in 0f..20f -> Res.drawable.ic_battery_0
            !charging && percentage in 20f..30f -> Res.drawable.ic_battery_1
            !charging && percentage in 30f..40f -> Res.drawable.ic_battery_2
            !charging && percentage in 40f..50f -> Res.drawable.ic_battery_3
            !charging && percentage in 50f..60f -> Res.drawable.ic_battery_4
            !charging && percentage in 60f..75f -> Res.drawable.ic_battery_5
            !charging && percentage in 75f..90f -> Res.drawable.ic_battery_6
            !charging && percentage in 90f..100f -> Res.drawable.ic_battery_7
            charging && percentage in 0f..20f -> Res.drawable.ic_battery_charging_0
            charging && percentage in 20f..30f -> Res.drawable.ic_battery_charging_1
            charging && percentage in 30f..40f -> Res.drawable.ic_battery_charging_2
            charging && percentage in 40f..50f -> Res.drawable.ic_battery_charging_3
            charging && percentage in 50f..60f -> Res.drawable.ic_battery_charging_4
            charging && percentage in 60f..75f -> Res.drawable.ic_battery_charging_5
            charging && percentage in 75f..90f -> Res.drawable.ic_battery_charging_6
            charging && percentage in 90f..100f -> Res.drawable.ic_battery_charging_7
            else -> Res.drawable.ic_no_battery
        }
    }
}