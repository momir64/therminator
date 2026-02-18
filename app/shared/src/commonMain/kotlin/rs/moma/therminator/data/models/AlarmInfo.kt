package rs.moma.therminator.data.models

import kotlinx.serialization.Serializable

@Serializable
enum class Speaker {
    REMOTE,
    LOCAL
}

@Serializable
data class AlarmInfo(
    val id: Int? = null,
    val name: String = "",
    val active: Boolean = false,
    val days: List<Int> = emptyList(),
    val hours: Int = 0,
    val minutes: Int = 0,
    val volume: Int = 50,
    val speaker: Speaker = Speaker.REMOTE,
    val tracks: List<String> = emptyList()
)
