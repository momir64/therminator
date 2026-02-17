package rs.moma.therminator.ui.screens

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.asPaddingValues
import rs.moma.therminator.ui.utils.BatteryIconProvider
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBars
import rs.moma.therminator.ui.dialogs.OfflineDialog
import rs.moma.therminator.viewmodels.MainViewModel
import androidx.compose.foundation.layout.padding
import therminator.shared.generated.resources.Res
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.width
import rs.moma.therminator.ui.navigation.Screen
import therminator.shared.generated.resources.*
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.IconButton
import androidx.navigation.NavHostController
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import rs.moma.therminator.ui.theme.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun HomeScreen(navController: NavHostController, topPadding: Int = 0) {
    val vm = koinInject<MainViewModel>()
    val isServerOffline by vm.isServerOffline.collectAsState()
    val batteryInfo by vm.batteryInfo.collectAsState()

    Box(Modifier.fillMaxSize().padding(top = topPadding.dp).imePadding()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(), start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton({ vm.logout() }) {
                Icon(painterResource(Res.drawable.ic_logout), "Log out", tint = OutlineColor)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                batteryInfo?.percentage?.let { Text("${it.roundToInt()}%", color = OutlineColor, fontSize = 20.sp) }
                Icon(painterResource(BatteryIconProvider.get(batteryInfo)), "Battery icon", tint = OutlineColor)
                Spacer(Modifier.width(4.dp))
                IconButton({ navController.navigate(Screen.Settings) }) {
                    Icon(painterResource(Res.drawable.ic_settings), "Settings", tint = OutlineColor)
                }
            }
        }

        Row(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { vm.test() },
                modifier = Modifier
                    .width(128.dp)
                    .height(72.dp),
                shape = RoundedCornerShape(16),
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
            ) { Text("TEST", color = Color.White, fontSize = 18.sp) }
        }
    }

    if (isServerOffline)
        OfflineDialog { vm.login() }
}