package rs.moma.therminator.ui.screens.settings

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import org.jetbrains.compose.resources.painterResource
import rs.moma.therminator.viewmodels.DisplayViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.OutlinedTextField
import rs.moma.therminator.ui.dialogs.OfflineDialog
import rs.moma.therminator.viewmodels.MainViewModel
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import therminator.shared.generated.resources.Res
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SliderDefaults
import rs.moma.therminator.ui.theme.ButtonColor2
import rs.moma.therminator.ui.theme.OutlineColor
import rs.moma.therminator.ui.theme.PrimaryColor
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import rs.moma.therminator.ui.theme.AccentColor
import therminator.shared.generated.resources.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.IconButton
import androidx.navigation.NavHostController
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.runtime.getValue
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject


@Composable
fun DisplayScreen(navController: NavHostController, topPadding: Int = 0) {
    val focusManager = LocalFocusManager.current
    val mainViewModel = koinInject<MainViewModel>()
    val displayViewModel = koinInject<DisplayViewModel>()
    val display by displayViewModel.display.collectAsState()
    val location by displayViewModel.location.collectAsState()
    val isLoading by displayViewModel.isLoading.collectAsState()
    val isServerOffline by mainViewModel.isServerOffline.collectAsState()

    LaunchedEffect(Unit) {
        displayViewModel.fetch()
    }

    Column(
        Modifier.fillMaxSize().padding(top = topPadding.dp).imePadding()
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(), start = 10.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(navController::popBackStack) {
                Icon(painterResource(Res.drawable.ic_back), "Log out", tint = OutlineColor)
            }
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(Modifier.padding(horizontal = 24.dp)) {
                Spacer(Modifier.height(32.dp))

                OutlinedTextField(
                    value = location.location,
                    onValueChange = { displayViewModel.setAddress(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Weather location") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            displayViewModel.findLocation()
                            focusManager.clearFocus()
                        }) {
                            Icon(painterResource(Res.drawable.ic_search), contentDescription = "Search")
                        }
                    }
                )

                Spacer(Modifier.height(32.dp))

                Text("Display brightness", color = PrimaryColor, fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(
                        modifier = Modifier.weight(1f),
                        value = display?.brightness?.toFloat() ?: 0f,
                        onValueChange = { displayViewModel.setBrightness(it.coerceIn(1f, 100f)) },
                        valueRange = -10f..111f,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentColor,
                            activeTrackColor = AccentColor,
                            inactiveTrackColor = ButtonColor2
                        )
                    )
                    Text(
                        "${display?.brightness ?: 0}%",
                        Modifier.width(52.dp),
                        textAlign = TextAlign.End,
                        color = PrimaryColor,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(28.dp))

            Button(
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp).height(52.dp),
                shape = RoundedCornerShape(8.dp),
                onClick = {
                    displayViewModel.saveSettings()
                    focusManager.clearFocus()
                }
            ) {
                Text("Save", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (isLoading) {
        Box(
            Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }

    if (isServerOffline)
        OfflineDialog { mainViewModel.login() }
}