package rs.moma.therminator.data.models

import kotlinx.serialization.Serializable

@Serializable
data class DisplayInfo(
    val brightness: Int,
)