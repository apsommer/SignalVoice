package com.sommerengineering.signalvoice

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.provider.Settings
import android.speech.tts.Voice
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sommerengineering.signalvoice.firebase.FirebaseAnalyticsLogger
import com.sommerengineering.signalvoice.login.GoogleAuthenticator
import com.sommerengineering.signalvoice.messages.FeedMode
import com.sommerengineering.signalvoice.onboarding.webhook.VerificationState.RECEIVED
import com.sommerengineering.signalvoice.onboarding.webhook.VerificationState.WAITING
import com.sommerengineering.signalvoice.onboarding.webhook.VerificationUiState
import com.sommerengineering.signalvoice.session.ConnectionMonitor
import com.sommerengineering.signalvoice.session.ConnectionState
import com.sommerengineering.signalvoice.session.Session
import com.sommerengineering.signalvoice.session.SessionManager
import com.sommerengineering.signalvoice.source.Message
import com.sommerengineering.signalvoice.uitls.RomanNumerals
import com.sommerengineering.signalvoice.uitls.btcStream
import com.sommerengineering.signalvoice.uitls.clStream
import com.sommerengineering.signalvoice.uitls.e6Stream
import com.sommerengineering.signalvoice.uitls.esStream
import com.sommerengineering.signalvoice.uitls.gcStream
import com.sommerengineering.signalvoice.uitls.gitHubProvider
import com.sommerengineering.signalvoice.uitls.nqStream
import com.sommerengineering.signalvoice.uitls.webhookBaseUrl
import com.sommerengineering.signalvoice.uitls.znStream
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

private const val systemTtsAvailableDescription = "Install additional voices"
private const val systemTtsUnavailableDescription = "Install or enable a text-to-speech engine"
private const val authenticatedCustomDescription = "Webhook alerts"
private const val guestCustomDescription = "Sign in to set up webhooks"
private const val screenFullDescription = "Full screen"
private const val screenWindowedDescription = "Show system bars"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val connectionMonitor: ConnectionMonitor,
    private val sessionManager: SessionManager,
    private val repo: MainRepository,
    private val googleAuthenticator: GoogleAuthenticator,
    private val analytics: FirebaseAnalyticsLogger
) : ViewModel() {

    // session
    val connectionState = connectionMonitor.connectionState
    val session = sessionManager.session

    // paywall: authenticated user attempts to access premium stream
    private val _shouldLaunchPaywall = MutableSharedFlow<Unit>()
    val shouldLaunchPaywall = _shouldLaunchPaywall.asSharedFlow()

    fun launchPaywall() {
        viewModelScope.launch {
            _shouldLaunchPaywall.emit(Unit)
        }
    }

    val webhookUrl
        get() = webhookBaseUrl + session.value.uid

    // room database
    val messages = repo.messages.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        emptyList()
    )

    // feed mode: linear or grouped
    var feedMode by mutableStateOf(FeedMode.Linear)
        private set

    fun toggleFeedMode() {
        val newFeedMode = if (feedMode == FeedMode.Linear) FeedMode.Grouped else FeedMode.Linear
        feedMode = newFeedMode
        repo.updateFeedMode(newFeedMode)
    }

    // tts engine
    val hasTts
        get() = connectionState.value != ConnectionState.TtsUnavailable
    val systemTtsDescription
        get() =
            if (hasTts) systemTtsAvailableDescription
            else systemTtsUnavailableDescription

    // voice
    var voices by mutableStateOf<List<Voice>>(emptyList())
        private set
    private val beautifulVoiceNames = hashMapOf<String, String>()
    var voiceIndex by mutableStateOf(0)
        private set
    var voiceDescription by mutableStateOf("")
        private set

    fun setVoice(value: Voice) {
        repo.voice = value
        voiceIndex = voices.indexOfFirst { it.name == value.name }
        val beautifulVoice = beautifyVoiceName(value.name)
        voiceDescription = beautifulVoice
        speakUtterance(beautifulVoice)
    }

    // speed
    var speed by mutableFloatStateOf(1f)
        private set
    val speedDescription
        get() = speed.toString()

    fun updateSpeed(value: Float) {
        speed = value
        repo.speed = value
    }

    // pitch
    var pitch by mutableFloatStateOf(1f)
        private set
    val pitchDescription
        get() = pitch.toString()

    fun updatePitch(value: Float) {
        pitch = value
        repo.pitch = value
    }

    // listening
    val isListening = repo.isListening
    fun toggleListening(context: Context) {
        if (!areNotificationsEnabled) {
            launchSystemNotificationSettings(context)
            return
        }
        val enabled = !isListening.value
        repo.setListening(enabled)
        analytics.logListeningChanged(enabled)
    }

    suspend fun restoreListening() = repo.restoreListening()

    fun speakUtterance(utterance: String) =
        viewModelScope.launch {
            repo.speakPreview(utterance)
        }

    fun speakMessage(message: Message) =
        viewModelScope.launch { repo.speakMessage(message) }

    // onboarding
    var isOnboardingComplete by mutableStateOf(false)
        private set

    fun completeOnboarding() {
        isOnboardingComplete = true
        repo.completeOnboarding()
        analytics.logOnboardingComplete()
    }

    var isEmptyState by mutableStateOf(true)
        private set

    fun updateEmptyState(enabled: Boolean) {
        isEmptyState = enabled
        repo.updateEmptyState(enabled)
    }

    // stream ZN
    var isZN by mutableStateOf(true)
        private set

    fun updateZN(enabled: Boolean) {
        isZN = enabled
        repo.updateZN(enabled)
        analytics.logStreamChanged(znStream, enabled)
    }

    // stream NQ
    var isNQ by mutableStateOf(true)
        private set

    fun updateNQ(enabled: Boolean) {
        isNQ = enabled
        repo.updateNQ(enabled)
        analytics.logStreamChanged(nqStream, enabled)
    }

    // stream BTC
    var isBTC by mutableStateOf(true)
        private set

    fun updateBTC(enabled: Boolean) {
        isBTC = enabled
        repo.updateBTC(enabled)
        analytics.logStreamChanged(btcStream, enabled)
    }

    // stream ES
    var isES by mutableStateOf(true)
        private set

    fun updateES(enabled: Boolean) {
        isES = enabled
        repo.updateES(enabled)
        analytics.logStreamChanged(esStream, enabled)
    }

    // stream GC
    var isGC by mutableStateOf(true)
        private set

    fun updateGC(enabled: Boolean) {
        isGC = enabled
        repo.updateGC(enabled)
        analytics.logStreamChanged(gcStream, enabled)
    }

    // stream E6
    var isE6 by mutableStateOf(true)
        private set

    fun updateE6(enabled: Boolean) {
        isE6 = enabled
        repo.updateE6(enabled)
        analytics.logStreamChanged(e6Stream, enabled)
    }

    // stream CL
    var isCL by mutableStateOf(true)
        private set

    fun updateCL(enabled: Boolean) {
        isCL = enabled
        repo.updateCL(enabled)
        analytics.logStreamChanged(clStream, enabled)
    }

    // custom signal
    val customSignalDescription
        get() =
            if (session.value is Session.Guest) guestCustomDescription
            else authenticatedCustomDescription

    // fullscreen
    var isFullScreen by mutableStateOf(false)
        private set
    val fullScreenDescription
        get() = if (isFullScreen) screenFullDescription else screenWindowedDescription

    fun updateFullScreen(enabled: Boolean) {
        isFullScreen = enabled
        repo.updateFullScreen(enabled)
    }

    private fun refreshTtsSettingsUi() {
        createBeautifulVoices()
        speed = repo.speed
        pitch = repo.pitch
        voiceDescription = beautifyVoiceName(repo.voice.name)
    }

    fun signOut() =
        repo.signOut()

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // sign-in
    fun signInWithGoogle(
        context: Context,
        onAuthentication: () -> Unit
    ) = viewModelScope.launch {

        val credential = googleAuthenticator.getCredential(context)
        val isSuccess = sessionManager.signInWithCredential(credential)
        if (isSuccess) {
            onAuthentication()
        }
    }

    fun signInWithGitHub(
        context: Context,
        onAuthentication: () -> Unit
    ) = viewModelScope.launch {

        val isSuccess = sessionManager.signInWithProvider(
            context = context,
            provider = gitHubProvider
        )
        if (isSuccess) {
            onAuthentication()
        }
    }

    // notifications
    var hasRequestedNotificationPermission by mutableStateOf(false)
        private set
    var areNotificationsEnabled by mutableStateOf(true)
        private set
    private val _notificationPermissionResult = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1
    )
    val notificationPermissionResult = _notificationPermissionResult.asSharedFlow()

    fun onNotificationPermissionResult(enabled: Boolean) {
        hasRequestedNotificationPermission = true
        areNotificationsEnabled = enabled
        _notificationPermissionResult.tryEmit(Unit)
    }

    private var wasNotificationsEnabled: Boolean? = null
    fun updateNotificationsEnabled(enabled: Boolean) {

        // previous notification permission
        val wasEnabled = wasNotificationsEnabled

        // update state
        wasNotificationsEnabled = enabled
        areNotificationsEnabled = enabled

        // ignore first initialization, resume into stored preference
        if (wasEnabled == null) return

        // log state changes
        if (wasEnabled != enabled) {
            analytics.logNotificationsChanged(enabled)
        }

        // always enforce off
        if (!enabled) {
            repo.setListening(false)
            return
        }

        // auto recover for transition off -> on
        if (!wasEnabled) repo.setListening(true)
    }

    fun launchSystemNotificationSettings(context: Context) {
        context.startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(
                    Settings.EXTRA_APP_PACKAGE,
                    context.packageName
                )
            }
        )
    }

    // beautiful voice names
    fun beautifyVoiceName(name: String) = beautifulVoiceNames[name] ?: ""
    private fun createBeautifulVoices() {

        // group voices by locale
        voices = repo.voices.sortedBy { it.locale.toLanguageTag() }
        voiceIndex = voices.indexOfFirst { it.name == repo.voice.name }

        // add roman numerals relative to locale to match system settings format
        voices
            .groupBy { it.locale.toLanguageTag() }
            .values
            .forEach { localeGroupVoices ->
                localeGroupVoices.forEachIndexed { i, voice ->
                    beautifulVoiceNames[voice.name] =
                        "${voice.locale.displayName} • Voice ${RomanNumerals.toNumeral(i)}"
                }
            }
    }

    // copy webhook
    fun copyWebhook(
        context: Context,
    ) {

        // save url to clipboard
        val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("", webhookUrl)
        clipboardManager.setPrimaryClip(clip)
    }

    // onboarding: setup webhook, verify user signal received
    private var isVerifiedLocked = false
    private var verificationStartTime: Long? = null
    fun setVerificationStartTime() {
        verificationStartTime = System.currentTimeMillis()
        isVerifiedLocked = false
    }

    val verificationUiState = messages.map { messages ->
        val startTime = verificationStartTime
        val latestMessage =
            startTime?.let {
                messages.firstOrNull {
                    it.source != null && it.timestamp > startTime
                }
            }
        if (latestMessage != null) {
            isVerifiedLocked = true
            VerificationUiState(RECEIVED, latestMessage.message)
        } else {
            VerificationUiState(WAITING)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        VerificationUiState(WAITING)
    )

    ////////////////////////////////////////////////////////////////////////////////////////////////

    init {

        // load settings from preferences
        // block main thread is acceptable for datastore read ~3 ms each
        runBlocking {
            isOnboardingComplete = repo.loadOnboarding()
            isEmptyState = repo.loadEmptyState()
            isZN = repo.loadZN()
            isNQ = repo.loadNQ()
            isBTC = repo.loadBTC()
            isES = repo.loadES()
            isGC = repo.loadGC()
            isE6 = repo.loadE6()
            isCL = repo.loadCL()
            feedMode = repo.loadFeedMode()
            isFullScreen = repo.loadFullScreen()
        }

        // wait for repo to finish initializing tts engine, takes a few seconds
        viewModelScope.launch {
            repo.isTtsReady.filter { it }.first()
            refreshTtsSettingsUi()
        }

        // dismiss empty state card on first user signal
        viewModelScope.launch {
            messages.collect { messages ->
                val hasUserSignal = messages.any { it.source != null }
                if (hasUserSignal && isEmptyState) {
                    updateEmptyState(false)
                }
            }
        }

        // cold start hydration sync of streams: firebase to local db
        viewModelScope.launch {
            repo.hydrateStreamMessages()
        }
    }
}
