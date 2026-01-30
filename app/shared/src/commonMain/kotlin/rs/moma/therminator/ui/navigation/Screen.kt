package rs.moma.therminator.ui.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen() {
    @Serializable
    @SerialName("login")
    data object Login : Screen()

    @Serializable
    @SerialName("home")
    data object Home : Screen()

    @Serializable
    @SerialName("settings")
    data object Settings : Screen()

    @Serializable
    @SerialName("files")
    data object Files : Screen()

    @Serializable
    @SerialName("camera")
    data object Camera : Screen()

    @Serializable
    @SerialName("display")
    data object Display : Screen()

    @Serializable
    @SerialName("weather")
    data object Weather : Screen()

    @Serializable
    @SerialName("battery")
    data object Battery : Screen()
}