package com.sommerengineering.signalvoice.source

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.sommerengineering.signalvoice.premium.LockBadge
import com.sommerengineering.signalvoice.uitls.lockBadgeSize

@Composable
fun MessageOriginText(
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