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

fun isStreamMessage(text: String): Boolean {

    // (Asset) • Signal primary • Signal secondary (optional) • Magnitude

    // split message into parts
    val parts = text
        .split("•")
        .map { it.trim() }
        .filter { it.isNotBlank() }

    // validate part count
    val isValidPartCount = parts.size >= 2 && 4 >= parts.size
    if (!isValidPartCount) return false

    // validate magnitude part (last part)
    val magnitudePart = parts.last()
    if (magnitudePart.isBlank()) return false

    // validate signal part (first part, or second part if asset is shown)
    val signalParts = parts.dropLast(1)
    val hasSignal = signalParts.any { it.isNotBlank() }

    return hasSignal
}

fun getAnnotatedAssetPart(
    firstPart: String,
    style: MessageItemStyle
): AnnotatedString? {

    // determine if asset is shown
    if (firstPart !in assetDisplayNames) return null

    // annotate asset
    return firstPart.let {
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
}

@Composable
fun buildUserSignalMessageText(
    displayText: String,
    style: MessageItemStyle
): MessageText {

    // split message into parts
    val parts = displayText.split("•").map { it.trim() }

    // annotate asset, if present (first part)
    val firstPart = parts.first()
    val assetAnnotated = getAnnotatedAssetPart(firstPart, style)
    val isShowAsset = assetAnnotated != null

    // remaining signal text
    val signalText =
        if (isShowAsset) parts.drop(1).joinToString(" • ")
        else displayText

    // validation
    val trimmedSignal = signalText.trim()
    val isBlankMessage = trimmedSignal.isBlank()
    val isTooLongMessage = trimmedSignal.length > 200

    val visibleSignalText =
        when {
            isBlankMessage -> "Your message is blank."
            isTooLongMessage -> trimmedSignal.take(200) + "..."
            else -> trimmedSignal
        }

    val signalAnnotated = buildAnnotatedString {

        withStyle(
            SpanStyle(
                color =
                    if (isBlankMessage) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        ) {
            append(visibleSignalText)
        }
    }

    val magnitudeAnnotated = buildAnnotatedString {

        withStyle(
            SpanStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 1f)
            )
        ) {
            append("User")
        }
    }

    return MessageText(
        asset = assetAnnotated,
        signal = signalAnnotated,
        magnitude = magnitudeAnnotated
    )
}

@Composable
fun buildBroadcastStreamMessageText(
    displayText: String,
    style: MessageItemStyle
): MessageText {

    // split message into parts
    val parts = displayText.split("•").map { it.trim() }

    // annotate asset, if present (first part)
    val firstPart = parts.first()
    val assetAnnotated = getAnnotatedAssetPart(firstPart, style)
    val isShowAsset = assetAnnotated != null

    // separate remaining parts
    val signalParts =
        if (isShowAsset) parts.drop(1)
        else parts

    val primaryPart = signalParts.first()

    val secondaryPart =
        if (signalParts.size > 2) signalParts[1]
        else null

    val magnitudeParts =
        if (signalParts.size > 3) signalParts.drop(2)
        else listOf(signalParts.last())

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

        magnitudeParts.forEachIndexed { index, part ->

            if (index > 0) {
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f)
                    )
                ) {
                    append(" • ")
                }
            }

            withStyle(
                SpanStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                )
            ) {
                append(part)
            }
        }
    }

    return MessageText(
        asset = assetAnnotated,
        signal = signalAnnotated,
        magnitude = magnitudeAnnotated
    )
}

@Composable
fun buildMessageText(
    displayText: String,
    style: MessageItemStyle
) =
    if (isStreamMessage(displayText)) buildBroadcastStreamMessageText(displayText, style)
    else buildUserSignalMessageText(displayText, style)

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