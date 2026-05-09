package com.sommerengineering.signalvoice

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.sommerengineering.signalvoice.navigation.MainNavigation
import com.sommerengineering.signalvoice.premium.BillingManager
import com.sommerengineering.signalvoice.theme.AppTheme
import com.sommerengineering.signalvoice.uitls.channelDescription
import com.sommerengineering.signalvoice.uitls.channelGroupId
import com.sommerengineering.signalvoice.uitls.channelGroupName
import com.sommerengineering.signalvoice.uitls.channelId
import com.sommerengineering.signalvoice.uitls.channelName
import com.sommerengineering.signalvoice.uitls.logException
import com.sommerengineering.signalvoice.uitls.logMessage
import com.sommerengineering.signalvoice.update.UpdateRepository
import com.sommerengineering.signalvoice.update.UpdateRequirement
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var updateRepository: UpdateRepository

    @Inject
    lateinit var billingManager: BillingManager
    private val viewModel: MainViewModel by viewModels()

    val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            viewModel.onNotificationPermissionResult(it)
        }

    val updateLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode != RESULT_OK) {
                logMessage("Required update flow failed with code: ${it.resultCode}")
                finish()
            }
        }

    private fun initNotificationChannel() {

        // create channel
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        ) // >= DEFAULT to show in status bar

        channel.description = channelDescription
        channel.group = channelGroupId

        // register with system, system takes no action if channel already exists
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager
            .createNotificationChannelGroup(
                NotificationChannelGroup(
                    channelGroupId,
                    channelGroupName
                )
            )
        manager.createNotificationChannel(channel)
    }

    private fun areNotificationsEnabled(): Boolean {

        // query system for notification and channel
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = manager.getNotificationChannel(channelId)
        val areNotificationsEnabled = manager.areNotificationsEnabled()
                && channel.importance > NotificationManager.IMPORTANCE_NONE
        return areNotificationsEnabled
    }

    private fun applyFullScreen(isFullScreen: Boolean) {

        val controller = WindowCompat.getInsetsController(window, window.decorView)

        if (isFullScreen) {
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
            return
        }

        controller.show(WindowInsetsCompat.Type.systemBars())
    }

    private fun checkUpdates() {

        lifecycleScope.launch {

            // fetch update requirement from play store
            try {
                updateRepository.refresh()
            } catch (e: Exception) {
                logException(e)
                return@launch
            }

            // determine if update is required based on current version
            val updateRequirement =
                updateRepository.getUpdateRequirement(
                    currentVersionCode = BuildConfig.VERSION_CODE
                )

            when (updateRequirement) {

                UpdateRequirement.REQUIRED -> launchRequiredUpdateFlow()
                UpdateRequirement.OPTIONAL -> launchOptionalUpdateFlow()
                UpdateRequirement.NONE -> logMessage("App is current, no update required.")
            }
        }
    }

    private suspend fun launchRequiredUpdateFlow() {

        val playUpdateManager = AppUpdateManagerFactory.create(this)

        val updateInfo =
            try {
                playUpdateManager.appUpdateInfo.await()
            } catch (exception: Exception) {
                logException(exception)
                finish()
                return
            }

        val isUpdateAvailable =
            updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE

        // user backgrounds the app during update flow
        val isUpdateInProgress =
            updateInfo.updateAvailability() ==
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS

        if (!isUpdateAvailable && !isUpdateInProgress) {
            logMessage("App version unsupported but no Play update available.")
            finish()
            return
        }

        // return result to activity
        playUpdateManager.startUpdateFlowForResult(
            updateInfo,
            updateLauncher,
            AppUpdateOptions
                .newBuilder(AppUpdateType.IMMEDIATE)
                .build()
        )
    }

    private fun launchOptionalUpdateFlow() {
        // todo launch flow to encourage user to update app, but allow them to continue without updating
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) return
        viewModel.updateNotificationsEnabled(areNotificationsEnabled())
    }

    private fun listenForPaywall() {
        lifecycleScope.launch {
            viewModel.shouldLaunchPaywall.collect {
                billingManager.launchBillingFlow(this@MainActivity)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initNotificationChannel()
        checkUpdates()

        // launch compose tree
        setContent {

            // toggle full screen
            val isFullScreen = viewModel.isFullScreen
            LaunchedEffect(isFullScreen) { applyFullScreen(isFullScreen) }

            App(viewModel)
        }
    }
}

@Composable
fun App(
    viewModel: MainViewModel
) {

    // safe drawing area when not in fullscreen
    val insets =
        if (viewModel.isFullScreen) WindowInsets(0)
        else WindowInsets.safeDrawing

    AppTheme {
        Scaffold(contentWindowInsets = insets) { insetsPadding ->
            Box(Modifier.padding(insetsPadding)) {
                MainNavigation(viewModel)
            }
        }
    }
}



