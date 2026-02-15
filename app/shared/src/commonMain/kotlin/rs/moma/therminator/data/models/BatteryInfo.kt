package rs.moma.therminator.data.models

data class BatteryInfo(
    val isCharging: Boolean?,
    val percentage: Float?
) {
    constructor(str: String) : this(
        isCharging = str.split(",")[0].toBooleanStrictOrNull(),
        percentage = str.split(",")[1].toFloatOrNull()
    )
}