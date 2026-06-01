package com.sommerengineering.signalvoice.session

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sommerengineering.signalvoice.session.ConnectionState.Connected
import com.sommerengineering.signalvoice.session.ConnectionState.InternetUnavailable
import com.sommerengineering.signalvoice.session.ConnectionState.PlayServicesUnavailable
import com.sommerengineering.signalvoice.session.ConnectionState.TtsUnavailable

private const val INTERNET_UNAVAILABLE = "Internet unavailable"
private const val PLAY_SERVICES_UNAVAILABLE = "Cannot connect to real-time services"
private const val TTS_UNAVAILABLE = "Install or enable a text-to-speech engine"

@Composable
fun ConnectionBottomBar(
    connectionState: ConnectionState
) {

    val text = when (connectionState) {
        Connected -> null
        InternetUnavailable -> INTERNET_UNAVAILABLE
        PlayServicesUnavailable -> PLAY_SERVICES_UNAVAILABLE
        TtsUnavailable -> TTS_UNAVAILABLE
    }

    AnimatedVisibility(
        visible = text != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.errorContainer,
            tonalElevation = 2.dp
        ) {

            Text(
                text = text ?: "",
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 10.dp
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}