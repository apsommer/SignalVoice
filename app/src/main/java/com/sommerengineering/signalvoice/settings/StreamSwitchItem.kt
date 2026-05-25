package com.sommerengineering.signalvoice.settings

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.sommerengineering.signalvoice.MainViewModel
import com.sommerengineering.signalvoice.source.MessageOrigin
import com.sommerengineering.signalvoice.source.OriginIcon
import com.sommerengineering.signalvoice.uitls.streamDescriptionAlpha

@Composable
fun StreamSwitchItem(
    viewModel: MainViewModel,
    origin: MessageOrigin,
    enabled: Boolean,
    updateStream: (Boolean) -> Unit,
    onLockedClick: () -> Unit,
) {

    val style = origin.style

    // premium locked state
    val session by viewModel.session.collectAsState()
    val isLocked = origin.isPremium && !session.isPremium

    SwitchItem(
        icon = {
            OriginIcon(
                origin = origin,
                isSettings = true,
                isLocked = isLocked,
                onLockedClick = onLockedClick
            )
        },
        title = origin.displayName,
        description = origin.signalDescription,
        titleColor = if (enabled) style.primary else null,
        descriptionColor = if (enabled) style.primary.copy(streamDescriptionAlpha) else null
    ) {
        Switch(
            checked = enabled,
            onCheckedChange = { updateStream(it) },
            colors = SwitchDefaults.colors(

                // active
                checkedThumbColor = lerp(style.primary, Color.Black, 0.20f),
                checkedTrackColor = style.primary.copy(alpha = 0.22f),
                checkedBorderColor = Color.Transparent,

                // inactive
                uncheckedThumbColor = lerp(style.primary, Color.Black, 0.42f),
                uncheckedTrackColor = style.primary.copy(alpha = 0.10f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}