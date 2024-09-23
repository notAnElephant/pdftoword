package composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class MessageType {
    ERROR,
    WARNING
}

class MessageLabelState(
    var message: String,
    var type: MessageType,
)

@Composable
fun MessageLabel(mls: MessageLabelState) {
  val icon: ImageVector = when (mls.type) {
    MessageType.ERROR -> Icons.Outlined.Error
    MessageType.WARNING -> Icons.Outlined.Warning
}
    val iconColor: Color = when (mls.type) {
        MessageType.ERROR -> Color.Red
        MessageType.WARNING -> Color.Yellow
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = "Message Icon",
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(mls.message)
    }
}