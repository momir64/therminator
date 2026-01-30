package rs.moma.therminator.data.models

import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.float
import kotlinx.serialization.json.Json

private const val MIN_TEMPERATURE = 24f
private const val MIN_RANGE = 10f

data class CameraFrame(
    val data: List<List<Float>>
) {
    val height = data.size
    val width = data.firstOrNull()?.size ?: 0
    val min = data.flatten().minOrNull()?.coerceAtLeast(MIN_TEMPERATURE) ?: MIN_TEMPERATURE
    val range = data.flatten().maxOrNull()?.minus(min)?.coerceAtLeast(MIN_RANGE) ?: MIN_RANGE

    constructor(str: String) : this(
        Json.parseToJsonElement(str).jsonArray.map { row ->
            row.jsonArray.map { it.jsonPrimitive.float }
        }
    )
}