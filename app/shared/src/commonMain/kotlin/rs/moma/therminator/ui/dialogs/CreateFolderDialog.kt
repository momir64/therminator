package rs.moma.therminator.ui.dialogs

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Color
import rs.moma.therminator.ui.theme.*
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*

@Composable
fun CreateFolderDialog(
    onCreate: (String) -> Unit,
    onCancel: () -> Unit
) {
    var folderName by remember { mutableStateOf(TextFieldValue("")) }
    val isValid = folderName.text.isNotBlank() && folderName.text.all { it.isLetterOrDigit() || it.isWhitespace() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(24.dp)
            .imePadding()
            .clickable(
                indication = null,
                onClick = { onCancel() },
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(CardColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 26.dp, vertical = 28.dp)
                .widthIn(max = 360.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (isValid) onCreate(folderName.text) }),
                label = { Text("Folder name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonColor),
                    modifier = Modifier.size(96.dp, 42.dp),
                    shape = RoundedCornerShape(8.dp),
                    onClick = onCancel
                ) {
                    Text("Cancel", color = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Button(
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = AccentColor.copy(alpha = 0.5f),
                        containerColor = AccentColor
                    ),
                    modifier = Modifier.size(96.dp, 42.dp),
                    onClick = { onCreate(folderName.text) },
                    shape = RoundedCornerShape(8.dp),
                    enabled = isValid,
                ) {
                    Text("Create", color = if (isValid) Color.White else OutlineColor)
                }
            }
        }
    }
}
