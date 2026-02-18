package rs.moma.therminator.data.models

import kotlinx.serialization.Serializable

@Serializable
data class LocationInfo(
    val location: String = "",
    val latitude: Float? = null,
    val longitude: Float? = null
)