package rs.moma.therminator.ui.dialogs

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import rs.moma.therminator.ui.theme.CardColor
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import rs.moma.therminator.ui.theme.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(24.dp)
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(CardColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 26.dp, vertical = 28.dp)
                .widthIn(max = 360.dp)
                .fillMaxWidth()
        ) {
            Spacer(Modifier.height(4.dp))
            Text(title, fontSize = 24.sp)
            Spacer(Modifier.height(14.dp))
            Text(message, fontSize = 18.sp, color = OutlineColor)
            Spacer(Modifier.height(28.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonColor),
                    modifier = Modifier.size(96.dp, 42.dp),
                    shape = RoundedCornerShape(8.dp),
                    onClick = onDismiss
                ) {
                    Text("Cancel", color = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                    modifier = Modifier.size(96.dp, 42.dp),
                    shape = RoundedCornerShape(8.dp),
                    onClick = onConfirm
                ) {
                    Text("Delete", color = Color.White)
                }
            }
        }
    }
}