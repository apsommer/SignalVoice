package com.sommerengineering.signalvoice.message

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sommerengineering.signalvoice.premium.LockBadge
import com.sommerengineering.signalvoice.source.assetDisplayNames
import com.sommerengineering.signalvoice.uitls.lockBadgeSize

data class MessageText(
    val assetAnnotated: AnnotatedString?,
    val bodyAnnotated: AnnotatedString
)

@Composable
fun buildMessageText(
    displayText: String,
    style: MessageItemStyle
): MessageText {

    // (Asset) • Event • Variable message that may include numbers • Variable message that may include numbers

    // split message into parts
    val parts = displayText.split("•").map { it.trim() }

    // determine if asset is shown
    val isShowAsset = parts.first() in assetDisplayNames
    val assetPart = if (isShowAsset) parts.first() else null
    val bodyParts = if (isShowAsset) parts.drop(1) else parts

    // annotate asset name if present
    val assetAnnotated = assetPart?.let {
        buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontWeight = FontWeight.SemiBold,
                    color = style.primary.copy(alpha = 0.85f)
                )
            ) {
                append(parts.first())
            }
        }
    }

    // annotate rest of message
    val bodyAnnotated = buildAnnotatedString {

        bodyParts.forEachIndexed { index, part ->

            val isEvent = index == 0

            // separator between each part
            if (index > 0) {
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    )
                ) {
                    append(" • ")
                }
            }

            val spanStyle = when {

                // primary event
                isEvent -> SpanStyle(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                // numbers included
                part.any { it.isDigit() } -> SpanStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.95f)
                )

                // secondary text
                else -> SpanStyle(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            withStyle(spanStyle) {
                append(part)
            }
        }
    }

    return MessageText(
        assetAnnotated = assetAnnotated,
        bodyAnnotated = bodyAnnotated
    )
}

@Composable
fun AssetText(
    annotatedText: AnnotatedString,
    isLocked: Boolean
) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = annotatedText,
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.size(4.dp))

        // lock badge, if premium
        if (isLocked) {
            LockBadge(
                modifier = Modifier.size(lockBadgeSize)
            )
        }

        Spacer(modifier = Modifier.size(4.dp))
    }

}