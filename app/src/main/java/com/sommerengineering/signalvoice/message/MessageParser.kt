package com.sommerengineering.signalvoice.message

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.sommerengineering.signalvoice.source.assetDisplayNames
import com.sommerengineering.signalvoice.source.sourceDisplayNames

data class AnnotatedMessage(
    val asset: AnnotatedString? = null,
    val signal: AnnotatedString,
    val magnitude: AnnotatedString? = null
)

object MessageParser {

    @Composable
    fun buildAnnotatedMessage(
        displayText: String,
        style: MessageItemStyle
    ): AnnotatedMessage {

        // split message into parts
        val parts = displayText
            .split("•")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        // annotate source, if present (first part)
        val firstPart = parts.firstOrNull()
        val assetAnnotated =
            firstPart?.let {
                getAnnotatedSourcePart(it, style)
            }

        val isShowAsset = assetAnnotated != null

        // remove optional asset/source prefix
        val contentParts =
            if (isShowAsset) parts.drop(1)
            else parts

        // annotate remaining content
        val annotatedContent =
            if (isStream(contentParts)) buildStreamAnnotatedMessage(contentParts)
            else buildSourceAnnotatedMessage(contentParts)

        return annotatedContent.copy(
            asset = assetAnnotated
        )
    }

    private fun isStream(contentParts: List<String>): Boolean {

        // validate stream part count, stream requires: signal + magnitude
        if (contentParts.size < 2) return false

        // magnitude heuristic
        val lastPart = contentParts.last()
        val isMagnitude = lastPart.any { it.isDigit() } || "%" in lastPart
        return isMagnitude
    }

    @Composable
    private fun buildStreamAnnotatedMessage(
        signalParts: List<String>
    ): AnnotatedMessage {

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

        return AnnotatedMessage(
            signal = signalAnnotated,
            magnitude = magnitudeAnnotated
        )
    }

    @Composable
    private fun buildSourceAnnotatedMessage(
        contentParts: List<String>
    ): AnnotatedMessage {

        // defensive combination
        val signalText = contentParts.joinToString(" • ")

        // validation
        val trimmedSignal = signalText.trim()
        val isBlankMessage = trimmedSignal.isBlank()

        val visibleSignalText =
            if (isBlankMessage) "Your message is blank."
            else trimmedSignal

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

        return AnnotatedMessage(
            signal = signalAnnotated
        )
    }

    fun getAnnotatedSourcePart(
        firstPart: String,
        style: MessageItemStyle
    ): AnnotatedString? {

        // determine if asset is shown
        val isAssetShown = firstPart in assetDisplayNames || firstPart in sourceDisplayNames
        if (!isAssetShown) return null

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
}