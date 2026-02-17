package rs.moma.therminator.ui.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.fillMaxWidth
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.OutlinedTextField
import rs.moma.therminator.viewmodels.MainViewModel
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.padding
import therminator.shared.generated.resources.Res
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.input.ImeAction
import therminator.shared.generated.resources.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import rs.moma.therminator.ui.theme.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject


@Composable
fun LoginScreen(topPadding: Int = 0) {
    var password by remember { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val vm = koinInject<MainViewModel>()

    Box(
        Modifier.fillMaxSize().padding(top = topPadding.dp).imePadding()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusManager.clearFocus() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.width(280.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (password.isNotEmpty()) vm.login(password) }),
                trailingIcon = {
                    IconButton({ show = !show }) {
                        Icon(painterResource(if (show) Res.drawable.ic_visibility_off else Res.drawable.ic_visibility), null)
                    }
                }
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { vm.login(password) },
                enabled = password.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16),
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor, disabledContainerColor = ButtonColor)
            ) { Text("Login", color = Color.White) }
        }
    }
}