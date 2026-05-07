package com.sommerengineering.signalvoice.message

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.sommerengineering.signalvoice.source.MessageOrigin
import com.sommerengineering.signalvoice.source.OriginIcon
import com.sommerengineering.signalvoice.uitls.dividerThickness
import com.sommerengineering.signalvoice.uitls.messageItemExpansionTimeMillis
import com.sommerengineering.signalvoice.uitls.rowHorizontalPadding
import com.sommerengineering.signalvoice.uitls.rowIconPadding
import com.sommerengineering.signalvoice.uitls.rowVerticalPadding

@Composable
fun MessageItemUi(
    displayText: String,
    beautifulTimestamp: String,
    timestamp: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    origin: MessageOrigin,
    isExpanded: Boolean,
    isLocked: Boolean,
    onLockedClick: () -> Unit,
    isInteractive: Boolean = true, // false for onboarding only
    isShowDivider: Boolean
) {

    // build styled message
    val style = origin.style
    val styledMessageText = buildMessageText(
        displayText = displayText,
        style = style
    )

    // supress ripple in onboarding presentation
    val clickableModifier =
        if (isInteractive) {
            Modifier.combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
        } else {
            Modifier
        }

    Column {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min) // required for collapse/expand animation
                    .then(clickableModifier)
                    .animateContentSize(tween(messageItemExpansionTimeMillis))
                    .background(backgroundColor)
                    .padding(horizontal = rowHorizontalPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // accent rail
                RailAccent(style.primary)

                val modifier = Modifier
                    .weight(1f)
                    .padding(vertical = rowVerticalPadding)

                // collapsed
                if (!isExpanded) {

                    CollapsedMessageItem(
                        messageText = styledMessageText,
                        beautifulTimestamp = beautifulTimestamp,
                        isLocked = isLocked,
                        modifier = modifier
                    )
                }

                // expanded
                else {

                    ExpandedMessageItem(
                        messageText = styledMessageText,
                        beautifulTimestamp = beautifulTimestamp,
                        timestamp = timestamp,
                        isLocked = isLocked,
                        modifier = modifier
                    )

                    // origin image
                    Spacer(Modifier.width(rowIconPadding))
                    OriginIcon(
                        origin = origin,
                        isLocked = isLocked,
                        onLockedClick = onLockedClick
                    )
                }
            }

            // scrim over premium content
            if (isLocked) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            style.primary.copy(alpha = 0.05f)
                        )
                )
            }
        }

        // divider between rows
        if (isShowDivider) {
            HorizontalDivider(
                thickness = dividerThickness,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 1f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
