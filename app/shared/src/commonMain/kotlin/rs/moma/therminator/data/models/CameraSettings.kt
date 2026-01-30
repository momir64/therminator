package rs.moma.therminator.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CameraSettings(
    val resolution: Int,
    val framerate: Int,
    val threshold: CameraThreshold
)