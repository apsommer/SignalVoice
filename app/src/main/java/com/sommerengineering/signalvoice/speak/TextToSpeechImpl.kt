package com.sommerengineering.signalvoice.speak

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.core.os.bundleOf
import com.sommerengineering.signalvoice.session.ConnectionMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

const val volumeKey = TextToSpeech.Engine.KEY_PARAM_VOLUME

@Singleton
class TextToSpeechImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val connectionMonitor: ConnectionMonitor
) : TextToSpeech.OnInitListener {

    // system text to speech engine
    private val _textToSpeech = TextToSpeech(context, this)

    // flow initialization
    private var _isInit = MutableStateFlow(false)
    val isInit = _isInit.asStateFlow()

    // voice
    private lateinit var _voices: List<Voice>
    val voices
        get() = _voices
    private lateinit var _voice: Voice
    var voice
        get() = _voice
        set(value) {
            _voice = value
            _textToSpeech.voice = value
        }

    // speed
    private var _speed = 1f
    var speed
        get() = _speed
        set(value) {
            _speed = value
            _textToSpeech.setSpeechRate(value)
        }

    // pitch
    private var _pitch = 1f
    var pitch
        get() = _pitch
        set(value) {
            _pitch = value
            _textToSpeech.setPitch(value)
        }

    // volume (mute)
    private var _volume = 1f
    var isMute
        get() = _volume == 0f
        set(value) {
            _volume = if (value) 0f else 1f
        }

    fun isSpeaking() = _textToSpeech.isSpeaking
    fun stop() = _textToSpeech.stop()

    suspend fun speakQueued(
        timestamp: Long,
        message: String
    ) = suspendCancellableCoroutine { continuation ->

        // listen to speech progress
        val listener = object : UtteranceProgressListener() {

            override fun onStart(id: String?) = Unit

            // cancel coroutine on completion, error, or stop
            override fun onDone(id: String?) = finishCoroutine(id)
            override fun onStop(id: String?, interrupted: Boolean) = finishCoroutine(id)
            override fun onError(id: String?) = finishCoroutine(id)
            private fun finishCoroutine(id: String?) {
                if (id != timestamp.toString() || !continuation.isActive) return
                continuation.resume(Unit)
            }
        }

        _textToSpeech.setOnUtteranceProgressListener(listener)

        // speak message
        _textToSpeech.speak(
            SpeechParser.normalizeMessage(message),
            TextToSpeech.QUEUE_ADD,
            bundleOf(volumeKey to _volume),
            timestamp.toString()
        )
    }

    fun speakImmediate(utterance: String) =
        _textToSpeech.speak(
            SpeechParser.normalizeMessage(utterance),
            TextToSpeech.QUEUE_FLUSH,
            bundleOf(volumeKey to 1f),
            System.currentTimeMillis().toString()
        )

    override fun onInit(status: Int) {

        // method is unstable across different OEM
        // stabilize with multiple validity checks

        // validate successful initialization
        if (status != TextToSpeech.SUCCESS) {
            connectionMonitor.setTtsUnavailable()
            return
        }

        // validate engine voices
        val availableVoices =
            _textToSpeech.voices?.toList().orEmpty()
        if (availableVoices.isEmpty()) {
            connectionMonitor.setTtsUnavailable()
            return
        }

        // validate default voice
        val defaultVoice = _textToSpeech.defaultVoice
        if (defaultVoice == null) {
            connectionMonitor.setTtsUnavailable()
            return
        }
        
        // update state
        _voice = defaultVoice
        _voices = availableVoices
        _isInit.update { true }
    }
}