package rs.moma.therminator.ui.screens

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.PaddingValues
import org.jetbrains.compose.resources.DrawableResource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBars
import rs.moma.therminator.ui.dialogs.OfflineDialog
import rs.moma.therminator.viewmodels.MainViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.padding
import therminator.shared.generated.resources.Res
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import rs.moma.therminator.ui.theme.OutlineColor
import androidx.compose.foundation.layout.width
import rs.moma.therminator.ui.navigation.Screen
import therminator.shared.generated.resources.*
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject


@Composable
fun SettingsScreen(navController: NavController, topPadding: Int = 0) {
    val mainViewModel = koinInject<MainViewModel>()
    val isServerOffline by mainViewModel.isServerOffline.collectAsState()

    val lazyListState = rememberLazyListState()
    val options: List<Triple<String, Screen, DrawableResource>> = listOf(
        Triple("Files", Screen.Files, Res.drawable.ic_files),
        Triple("Camera", Screen.Camera, Res.drawable.ic_camera),
        Triple("Display", Screen.Display, Res.drawable.ic_display),
//        Triple("Weather", Screen.Weather, Res.drawable.ic_weather),
//        Triple("Battery", Screen.Battery, Res.drawable.ic_battery)
    )

    Column(Modifier.fillMaxSize().padding(top = topPadding.dp).imePadding()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(), start = 10.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton({ navController.popBackStack() }) {
                Icon(painterResource(Res.drawable.ic_back), "Log out", tint = OutlineColor)
            }
        }

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(options, key = { it.first }) { option ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { navController.navigate(option.second) }
                ) {
                    Row(
                        Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painterResource(option.third),
                            contentDescription = option.first,
                            Modifier.size(28.dp),
                            tint = OutlineColor
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(option.first, modifier = Modifier.padding(8.dp))
                        Spacer(Modifier.weight(1f))
                        Icon(
                            painterResource(Res.drawable.ic_enter),
                            contentDescription = "Open settings section",
                            Modifier.size(20.dp),
                            tint = OutlineColor
                        )
                    }
                }
            }
        }
    }

    if (isServerOffline)
        OfflineDialog { mainViewModel.login() }
}