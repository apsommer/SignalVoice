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
    val asset: AnnotatedString?,
    val signal: AnnotatedString,
    val magnitude: AnnotatedString
)

@Composable
fun buildMessageText(
    displayText: String,
    style: MessageItemStyle
): MessageText {

    // (Asset) • Signal primary • Signal secondary (optional) • Magnitude

    // split message into parts
    val parts = displayText.split("•").map { it.trim() }

    // determine if asset is shown
    val isShowAsset = parts.first() in assetDisplayNames

    // separate asset + remaining parts
    val assetPart = if (isShowAsset) parts.first() else null
    val signalParts =
        if (isShowAsset) parts.drop(1)
        else parts

    // todo temp fallback (test data)
    // .............................................................................................

    if (signalParts.size < 2) {

        val signalAnnotated = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            ) {
                append(displayText)
            }
        }

        val magnitudeAnnotated = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.95f)
                )
            ) {
                append("123") // fallback magnitude
            }
        }

        return MessageText(
            asset = null, // UI handles asset prepend
            signal = signalAnnotated,
            magnitude = magnitudeAnnotated
        )
    }

    // .............................................................................................
    // todo end temp fallback (test data)

    val primaryPart = signalParts[0]
    val magnitudePart = signalParts.last()

    val secondaryPart =
        if (signalParts.size > 2) signalParts[1]
        else null

    // annotate asset
    val assetAnnotated = assetPart?.let {
        buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontWeight = FontWeight.SemiBold,
                    color = style.primary.copy(alpha = 0.85f)
                )
            ) {
                append(it)
            }
        }
    }

    // annotate primary + secondary (signal)
    val signalAnnotated = buildAnnotatedString {

        // primary
        withStyle(
            SpanStyle(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        ) {
            append(primaryPart)
        }

        // secondary (optional)
        secondaryPart?.let {

            withStyle(
                SpanStyle(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f)
                )
            ) {
                append(" • ")
            }

            withStyle(
                SpanStyle(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            ) {
                append(it)
            }
        }
    }

    // annotate magnitude (always present)
    val magnitudeAnnotated = buildAnnotatedString {
        withStyle(
            SpanStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 1f)
            )
        ) {
            append(magnitudePart)
        }
    }

    return MessageText(
        asset = assetAnnotated,
        signal = signalAnnotated,
        magnitude = magnitudeAnnotated
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