package com.sommerengineering.signalvoice.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sommerengineering.signalvoice.uitls.descriptionAlpha
import com.sommerengineering.signalvoice.uitls.rowHeight
import com.sommerengineering.signalvoice.uitls.rowHorizontalPadding
import com.sommerengineering.signalvoice.uitls.rowIconPadding
import com.sommerengineering.signalvoice.uitls.settingsIconSize

@Composable
fun SliderItem(
    iconRes: Int,
    title: String,
    description: String,
    labelWidth: Dp,
    onLabelWidthChanged: (Dp) -> Unit,
    content: @Composable () -> Unit
) {

    val density = LocalDensity.current

    Surface {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(rowHeight)
                    .padding(
                        start = rowHorizontalPadding + 4.dp,
                        end = rowHorizontalPadding
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    modifier = Modifier
                        .then(
                            if (labelWidth > 0.dp) Modifier.width(labelWidth)
                            else Modifier
                        )
                        .onSizeChanged {
                            val widthDp = with(density) { it.width.toDp() }
                            if (widthDp > labelWidth) onLabelWidthChanged(widthDp)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        modifier = Modifier.size(settingsIconSize),
                        painter = painterResource(iconRes),
                        contentDescription = null
                    )
                    Spacer(Modifier.width(rowIconPadding + 4.dp))

                    Column {

                        // title
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                        )

                        // description
                        Text(
                            modifier = Modifier.padding(top = 4.dp),
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(descriptionAlpha),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.width(rowIconPadding))

                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    content()
                }
            }
        }
    }
}