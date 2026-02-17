package rs.moma.therminator.ui.navigation

import rs.moma.therminator.ui.screens.settings.camera.CameraScreen
import rs.moma.therminator.ui.screens.settings.files.FilesScreen
import rs.moma.therminator.ui.screens.settings.BatteryScreen
import rs.moma.therminator.ui.screens.settings.DisplayScreen
import rs.moma.therminator.ui.screens.settings.WeatherScreen
import rs.moma.therminator.ui.screens.SettingsScreen
import androidx.navigation.compose.composable
import androidx.compose.animation.core.tween
import androidx.navigation.NavHostController
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import rs.moma.therminator.ui.screens.*
import androidx.compose.ui.Modifier

@Composable
fun Navigation(modifier: Modifier = Modifier, navController: NavHostController, topPadding: Int = 0, isLoggedIn: Boolean) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = if (isLoggedIn) Screen.Home else Screen.Login,
        enterTransition = { fadeIn(animationSpec = tween(100)) },
        exitTransition = { fadeOut(animationSpec = tween(100)) },
        popEnterTransition = { fadeIn(animationSpec = tween(100)) },
        popExitTransition = { fadeOut(animationSpec = tween(100)) }
    ) {
        composable<Screen.Login> { LoginScreen(topPadding) }
        composable<Screen.Home> { HomeScreen(navController, topPadding) }
        composable<Screen.Settings> { SettingsScreen(navController, topPadding) }
        composable<Screen.Files> { FilesScreen(navController, topPadding) }
        composable<Screen.Camera> { CameraScreen(navController, topPadding) }
        composable<Screen.Display> { DisplayScreen(navController, topPadding) }
        composable<Screen.Weather> { WeatherScreen(navController, topPadding) }
        composable<Screen.Battery> { BatteryScreen(navController, topPadding) }
    }
}