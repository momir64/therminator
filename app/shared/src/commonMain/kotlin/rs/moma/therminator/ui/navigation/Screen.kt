package rs.moma.therminator.ui.navigation

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
sealed class Screen() {
    @Serializable
    @SerialName("login")
    data object Login : Screen()

    @Serializable
    @SerialName("home")
    data object Home : Screen()

    @Serializable
    @SerialName("alarm")
    data object Alarm : Screen()

    @Serializable
    @SerialName("trackSelector")
    data object TrackSelector : Screen()

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