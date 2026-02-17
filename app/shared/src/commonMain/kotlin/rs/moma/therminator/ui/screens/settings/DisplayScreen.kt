package rs.moma.therminator.ui.screens.settings

import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import therminator.shared.generated.resources.ic_back
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.padding
import therminator.shared.generated.resources.Res
import rs.moma.therminator.ui.theme.OutlineColor
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.IconButton
import androidx.navigation.NavHostController
import androidx.compose.runtime.Composable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun DisplayScreen(navController: NavHostController, topPadding: Int = 0) {
    Box(Modifier.fillMaxSize().padding(top = topPadding.dp).imePadding()) {
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

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Display settings")
        }
    }
}