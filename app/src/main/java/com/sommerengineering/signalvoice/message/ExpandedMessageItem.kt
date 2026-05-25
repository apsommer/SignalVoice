package com.sommerengineering.signalvoice.message

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sommerengineering.signalvoice.source.MessageOriginText
import com.sommerengineering.signalvoice.theme.timestampTextStyle
import com.sommerengineering.signalvoice.uitls.TimestampFormatter

@Composable
fun ExpandedMessageItem(
    annotatedMessage: AnnotatedMessage,
    beautifulTimestamp: String,
    timestamp: Long,
    isLocked: Boolean,
    modifier: Modifier = Modifier
) {

    // separate message parts
    val assetAnnotated = annotatedMessage.asset
    val signalAnnotated = annotatedMessage.signal
    val magnitudeAnnotated = annotatedMessage.magnitude

    Column(modifier) {

        // origin name (feed mode linear)
        if (assetAnnotated != null) {
            MessageOriginText(
                annotatedText = assetAnnotated,
                isLocked = isLocked
            )
            Spacer(modifier = Modifier.height(2.dp))
        }

        // signal
        Text(
            text = signalAnnotated,
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(Modifier.height(8.dp))

        // magnitude
        if (magnitudeAnnotated != null) {
            Text(
                text = magnitudeAnnotated,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.height(12.dp))

        // compact timestamp
        Text(
            text = beautifulTimestamp,
            style = timestampTextStyle,
            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
        )

        Spacer(Modifier.height(2.dp))

        // full timestamp
        Text(
            text = TimestampFormatter.beautifyFull(timestamp),
            style = timestampTextStyle,
            color = MaterialTheme.colorScheme.onSurface.copy(0.45f)
        )
    }
}