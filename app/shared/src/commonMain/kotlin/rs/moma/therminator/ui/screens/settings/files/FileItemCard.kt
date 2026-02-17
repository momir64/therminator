package rs.moma.therminator.ui.screens.settings.files

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.foundation.shape.RoundedCornerShape
import therminator.shared.generated.resources.ic_folder
import androidx.compose.foundation.layout.fillMaxWidth
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.combinedClickable
import rs.moma.therminator.data.models.FileItemType
import androidx.compose.foundation.layout.padding
import therminator.shared.generated.resources.Res
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import rs.moma.therminator.ui.theme.OutlineColor
import androidx.compose.foundation.layout.width
import rs.moma.therminator.data.models.FileItem
import rs.moma.therminator.ui.theme.ButtonColor
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.Row
import rs.moma.therminator.ui.theme.CardColor
import androidx.compose.runtime.Composable
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FileItemCard(
    item: FileItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = if (item.selected) null else ripple(bounded = true),
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = if (item.selected) ButtonColor.copy(alpha = 0.8f) else CardColor)
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.type == FileItemType.FOLDER) {
                Icon(
                    painterResource(Res.drawable.ic_folder),
                    contentDescription = item.name,
                    Modifier.size(28.dp),
                    tint = OutlineColor
                )
                Spacer(Modifier.width(8.dp))
                Text(item.name, modifier = Modifier.padding(8.dp))
            } else {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
                ) {
                    println(item)
                    Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                        Text(item.title, fontSize = 16.sp, fontWeight = Bold, maxLines = 1, overflow = Ellipsis)
                        Spacer(Modifier.height(2.dp))
                        Text(item.artist, fontSize = 14.sp, maxLines = 1, overflow = Ellipsis)
                    }
                    Text(
                        "${item.duration / 60}:${(item.duration % 60).toString().padStart(2, '0')}",
                        modifier = Modifier.padding(4.dp),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}