package com.sommerengineering.signalvoice.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import com.sommerengineering.signalvoice.R
import com.sommerengineering.signalvoice.uitls.edgePadding

@Composable
fun OnboardingScreen(
    title: String,
    subTitle: String? = null,
    pageNumber: Int,
    buttonText: String,
    onNextClick: () -> Unit,
    isNextEnabled: Boolean = true,
    onCloseClick: (() -> Unit)? = null,
    content: @Composable (() -> Unit)
) {

    // safe drawing area when in fullscreen
    val insets =
        if (WindowInsets.systemBars.getTop(LocalDensity.current) == 0) WindowInsets.safeDrawing
        else WindowInsets(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(insets)
            .padding(edgePadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // page indicator
        PageIndicator(
            pageNumber = pageNumber,
            modifier = Modifier.padding(top = edgePadding)
        )

        // title
        OnboardingText(
            title = title,
            subTitle = subTitle,
            modifier = Modifier
                .padding(edgePadding)
        )

        BackgroundGlowContainer(
            modifier = Modifier.weight(1f),
        ) {
            content()
        }

        // page indicators and button
        OnboardingButton(
            buttonText = buttonText,
            onNextClick = onNextClick,
            isNextEnabled = isNextEnabled,
            modifier = Modifier.fillMaxWidth()
        )
    }

    // close button
    if (onCloseClick != null) {
        IconButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(edgePadding / 2)
                .wrapContentWidth(Alignment.End),
            onClick = { onCloseClick() }) {
            Icon(
                painter = painterResource(R.drawable.close),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }

}




