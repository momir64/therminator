package rs.moma.therminator.data.models

import kotlinx.serialization.Serializable

enum class FileItemType {
    FOLDER,
    FILE
}

@Serializable
data class FileItem(
    val id: Int,
    val name: String,
    val type: FileItemType,
    val path: String,
    val title: String = "",
    val artist: String = "",
    val duration: Int = 0,
    val selected: Boolean = false
)