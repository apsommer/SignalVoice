package com.sommerengineering.signalvoice.session

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private const val INTERNET_UNAVAILABLE = "Internet connection off"
private const val PLAY_SERVICES_UNAVAILABLE = "Cannot connect to Play Services"

@Composable
fun ConnectionBottomBar(
    connectionState: ConnectionState
) {

    val text = when (connectionState) {
        is ConnectionState.Connected -> return
        is ConnectionState.InternetUnavailable -> INTERNET_UNAVAILABLE
        is ConnectionState.PlayServicesUnavailable -> PLAY_SERVICES_UNAVAILABLE
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer
    ) {

        Text(
            text = text,
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