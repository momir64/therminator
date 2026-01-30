package rs.moma.therminator.ui.navigation

import rs.moma.therminator.ui.screens.settings.BatteryScreen
import rs.moma.therminator.ui.screens.settings.DisplayScreen
import rs.moma.therminator.ui.screens.settings.WeatherScreen
import rs.moma.therminator.ui.screens.settings.CameraScreen
import rs.moma.therminator.ui.screens.settings.FilesScreen
import androidx.navigation.compose.composable
import androidx.compose.animation.core.tween
import androidx.navigation.NavHostController
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import rs.moma.therminator.ui.screens.*
import androidx.compose.ui.Modifier
import rs.moma.therminator.ui.screens.SettingsScreen

@Composable
fun Navigation(modifier: Modifier = Modifier, navController: NavHostController, isLoggedIn: Boolean) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = if (isLoggedIn) Screen.Home else Screen.Login,
        enterTransition = { fadeIn(animationSpec = tween(100)) },
        exitTransition = { fadeOut(animationSpec = tween(100)) },
        popEnterTransition = { fadeIn(animationSpec = tween(100)) },
        popExitTransition = { fadeOut(animationSpec = tween(100)) }
    ) {
        composable<Screen.Login> { LoginScreen() }
        composable<Screen.Home> { HomeScreen(navController) }
        composable<Screen.Settings> { SettingsScreen(navController) }
        composable<Screen.Files> { FilesScreen(navController) }
        composable<Screen.Camera> { CameraScreen(navController) }
        composable<Screen.Display> { DisplayScreen(navController) }
        composable<Screen.Weather> { WeatherScreen(navController) }
        composable<Screen.Battery> { BatteryScreen(navController) }
    }
}