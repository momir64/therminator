package rs.moma.therminator

import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.fillMaxSize
import rs.moma.therminator.ui.theme.TherminatorTheme
import rs.moma.therminator.ui.navigation.Navigation
import rs.moma.therminator.viewmodels.MainViewModel
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import androidx.compose.runtime.*

@Composable
fun App() {
    val viewModel = koinInject<MainViewModel>()
    val navController = rememberNavController()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    TherminatorTheme {
        Surface(Modifier.fillMaxSize()) {
            isLoggedIn?.let {
                Navigation(
                    navController = navController,
                    isLoggedIn = it
                )
            }
        }
    }
}