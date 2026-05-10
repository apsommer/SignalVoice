package com.sommerengineering.signalvoice.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.sommerengineering.signalvoice.R
import com.sommerengineering.signalvoice.uitls.lockBadgePadding


@Composable
fun LockBadge(modifier: Modifier = Modifier) {

    Box(
        modifier = modifier
    ) {

        // halo
        Box(
            Modifier
                .matchParentSize()
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = CircleShape
                )
        )

        // lock
        Icon(
            painter = painterResource(R.drawable.lock),
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .padding(lockBadgePadding),
            tint = Color.White.copy(alpha = 0.8f)
        )
    }
}