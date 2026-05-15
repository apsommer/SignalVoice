package com.sommerengineering.signalvoice.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sommerengineering.signalvoice.BuildConfig
import com.sommerengineering.signalvoice.MainViewModel
import com.sommerengineering.signalvoice.R
import com.sommerengineering.signalvoice.login.Session.Guest
import com.sommerengineering.signalvoice.source.MessageOrigin
import com.sommerengineering.signalvoice.source.btcAsset
import com.sommerengineering.signalvoice.source.clAsset
import com.sommerengineering.signalvoice.source.e6Asset
import com.sommerengineering.signalvoice.source.esAsset
import com.sommerengineering.signalvoice.source.gcAsset
import com.sommerengineering.signalvoice.source.nqAsset
import com.sommerengineering.signalvoice.source.znAsset
import com.sommerengineering.signalvoice.uitls.anonymousCustomDescription
import com.sommerengineering.signalvoice.uitls.customDescription
import com.sommerengineering.signalvoice.uitls.customDividerTitle
import com.sommerengineering.signalvoice.uitls.customTitle
import com.sommerengineering.signalvoice.uitls.descriptionAlpha
import com.sommerengineering.signalvoice.uitls.edgePadding
import com.sommerengineering.signalvoice.uitls.generalDividerTitle
import com.sommerengineering.signalvoice.uitls.manageSubscriptionDescription
import com.sommerengineering.signalvoice.uitls.manageSubscriptionTitle
import com.sommerengineering.signalvoice.uitls.pitchChangeUtterance
import com.sommerengineering.signalvoice.uitls.pitchTitle
import com.sommerengineering.signalvoice.uitls.premiumDividerTitle
import com.sommerengineering.signalvoice.uitls.screenTitle
import com.sommerengineering.signalvoice.uitls.settingsIconSize
import com.sommerengineering.signalvoice.uitls.signOutDescription
import com.sommerengineering.signalvoice.uitls.signOutTitle
import com.sommerengineering.signalvoice.uitls.speedChangeUtterance
import com.sommerengineering.signalvoice.uitls.speedTitle
import com.sommerengineering.signalvoice.uitls.streamsDividerTitle
import com.sommerengineering.signalvoice.uitls.subscriptionUrl
import com.sommerengineering.signalvoice.uitls.systemTtsDescription
import com.sommerengineering.signalvoice.uitls.systemTtsInstallVoicesAction
import com.sommerengineering.signalvoice.uitls.systemTtsTitle
import com.sommerengineering.signalvoice.uitls.voiceDividerTitle
import com.sommerengineering.signalvoice.uitls.voiceTitle

@Composable
fun SettingsDrawer(
    viewModel: MainViewModel,
    onSignOut: () -> Unit,
    onCustomSignalClick: () -> Unit,
    appBarHeight: Dp
) {

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val session by viewModel.session.collectAsState()

    val speed = viewModel.speed
    val pitch = viewModel.pitch
    val voiceDescription = viewModel.voiceDescription
    val speedDescription = viewModel.speedDescription
    val pitchDescription = viewModel.pitchDescription

    val isZN = viewModel.isZN
    val isNQ = viewModel.isNQ
    val isBTC = viewModel.isBTC
    val isES = viewModel.isES
    val isGC = viewModel.isGC
    val isE6 = viewModel.isE6
    val isCL = viewModel.isCL

    val isFullScreen = viewModel.isFullScreen
    val fullScreenDescription = viewModel.fullScreenDescription

    var isShowVoiceDialog by remember { mutableStateOf(false) }

    // measure width of speed slider for proper alignment of pitch slider
    var labelWidth by remember { mutableStateOf(0.dp) }

    Box {

        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    top =
                        if (isFullScreen) appBarHeight
                        else 0.dp
                )
        ) {

            // divider
            item {
                DividerItem(voiceDividerTitle)
            }

            // voice
            item {
                DialogItem(
                    iconRes = R.drawable.voice,
                    title = voiceTitle,
                    description = voiceDescription,
                    onClick = { isShowVoiceDialog = true }) {

                    if (isShowVoiceDialog) {
                        VoiceDialog(
                            viewModel = viewModel,
                            onItemSelected = {
                                viewModel.setVoice(it)
                                isShowVoiceDialog = false
                            },
                            onDismiss = {
                                isShowVoiceDialog = false
                            })
                    }
                }
            }

            // speed
            item {
                SliderItem(
                    iconRes = R.drawable.speed,
                    title = speedTitle,
                    description = speedDescription,
                    labelWidth = labelWidth,
                    onLabelWidthChanged = { labelWidth = it }
                ) {

                    SliderImpl(
                        initPosition = speed,
                        onValueChanged = { viewModel.updateSpeed(it) },
                        onValueChangeFinished = {
                            viewModel.speakUtterance(speedChangeUtterance + it)
                        })
                }
            }

            // pitch
            item {
                SliderItem(
                    iconRes = R.drawable.pitch,
                    title = pitchTitle,
                    description = pitchDescription,
                    labelWidth = labelWidth,
                    onLabelWidthChanged = { labelWidth = it }
                ) {

                    SliderImpl(
                        initPosition = pitch,
                        onValueChanged = { viewModel.updatePitch(it) },
                        onValueChangeFinished = {
                            viewModel.speakUtterance(pitchChangeUtterance + it)
                        })
                }
            }

            // system tts settings
            item {
                LinkItem(
                    iconRes = R.drawable.settings,
                    title = systemTtsTitle,
                    description = systemTtsDescription,
                    onClick = {
                        with(context) {
                            startActivity(
                                Intent(systemTtsInstallVoicesAction)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    })
            }

            // divider
            item {
                DividerItem(streamsDividerTitle)
            }

            // stream ZN
            item {
                StreamSwitchItem(
                    viewModel = viewModel,
                    origin = MessageOrigin.BroadcastStream(znAsset),
                    enabled = isZN,
                    updateStream = { viewModel.updateZN(it) },
                    onLockedClick = { viewModel.launchPaywall() }
                )
            }

            // stream NQ
            item {
                StreamSwitchItem(
                    viewModel = viewModel,
                    origin = MessageOrigin.BroadcastStream(nqAsset),
                    enabled = isNQ,
                    updateStream = { viewModel.updateNQ(it) },
                    onLockedClick = { viewModel.launchPaywall() }
                )
            }

            // stream BTC
            item {
                StreamSwitchItem(
                    viewModel = viewModel,
                    origin = MessageOrigin.BroadcastStream(btcAsset),
                    enabled = isBTC,
                    updateStream = { viewModel.updateBTC(it) },
                    onLockedClick = { viewModel.launchPaywall() }
                )
            }

            // divider
            item {
                DividerItem(premiumDividerTitle)
            }

            // stream ES
            item {
                StreamSwitchItem(
                    viewModel = viewModel,
                    origin = MessageOrigin.BroadcastStream(esAsset),
                    enabled = isES,
                    updateStream = { viewModel.updateES(it) },
                    onLockedClick = { viewModel.launchPaywall() }
                )
            }

            // stream GC
            item {
                StreamSwitchItem(
                    viewModel = viewModel,
                    origin = MessageOrigin.BroadcastStream(gcAsset),
                    enabled = isGC,
                    updateStream = { viewModel.updateGC(it) },
                    onLockedClick = { viewModel.launchPaywall() }
                )
            }

            // stream E6
            item {
                StreamSwitchItem(
                    viewModel = viewModel,
                    origin = MessageOrigin.BroadcastStream(e6Asset),
                    enabled = isE6,
                    updateStream = { viewModel.updateE6(it) },
                    onLockedClick = { viewModel.launchPaywall() }
                )
            }

            // stream CL
            item {
                StreamSwitchItem(
                    viewModel = viewModel,
                    origin = MessageOrigin.BroadcastStream(clAsset),
                    enabled = isCL,
                    updateStream = { viewModel.updateCL(it) },
                    onLockedClick = { viewModel.launchPaywall() }
                )
            }

            // divider
            item {
                DividerItem(customDividerTitle)
            }

            // webhook
            item {
                DialogItem(
                    iconRes = R.drawable.webhook,
                    title = customTitle,
                    description =
                        if (session is Guest) anonymousCustomDescription
                        else customDescription,
                    onClick = onCustomSignalClick
                ) {
                    Icon(
                        painter = painterResource(R.drawable.chevron),
                        contentDescription = null
                    )
                }
            }

            // divider
            item {
                DividerItem(generalDividerTitle)
            }

            // full screen
            item {
                SwitchItem(
                    icon = {
                        Box(
                            modifier = Modifier.size(settingsIconSize),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.fullscreen),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    },
                    title = screenTitle,
                    description = fullScreenDescription
                ) {

                    Switch(
                        checked = isFullScreen,
                        onCheckedChange = { viewModel.updateFullScreen(it) },
                        colors = SwitchDefaults.colors(

                            // active
                            checkedThumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                            checkedBorderColor = Color.Transparent,

                            // inactive
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f),
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            // manage subscription
            item {
                LinkItem(
                    iconRes = R.drawable.subscription,
                    title = manageSubscriptionTitle,
                    description = manageSubscriptionDescription,
                    onClick = { uriHandler.openUri(subscriptionUrl) })
            }

            // sign-out
            item {
                LinkItem(
                    iconRes = R.drawable.sign_out,
                    iconTint = true,
                    title = signOutTitle,
                    description = signOutDescription,
                    onClick = { onSignOut() })
            }

            // version code
            item {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = edgePadding, end = edgePadding, bottom = edgePadding),
                    text = "v" + BuildConfig.VERSION_NAME,
                    textAlign = TextAlign.End,
                    color = LocalContentColor.current.copy(descriptionAlpha),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // prevent scroll into notch area when fullscreen
        if (isFullScreen) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(appBarHeight)
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
            )
        }
    }
}