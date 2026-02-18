package rs.moma.therminator

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import rs.moma.therminator.ui.theme.TherminatorTheme
import rs.moma.therminator.viewmodels.MainViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import org.koin.android.ext.android.inject
import androidx.activity.enableEdgeToEdge
import android.os.Bundle
import kotlin.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val vm: MainViewModel by inject()
        splashScreen.setKeepOnScreenCondition {
            vm.isLoggedIn.value == null
        }

        setContent {
            TherminatorTheme {
                App()
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}