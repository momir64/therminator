package rs.moma.therminator.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CameraThreshold(
    val temperature: Float,
    val pixels: Int,
    val frames: Int
)