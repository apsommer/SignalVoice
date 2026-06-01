package com.sommerengineering.signalvoice.session

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import com.sommerengineering.signalvoice.session.ConnectionState.Connected
import com.sommerengineering.signalvoice.session.ConnectionState.InternetUnavailable
import com.sommerengineering.signalvoice.session.ConnectionState.PlayServicesUnavailable
import com.sommerengineering.signalvoice.session.ConnectionState.TtsUnavailable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val connectivityManager =
        context.getSystemService(ConnectivityManager::class.java)

    private val _connectionState = MutableStateFlow(Connected)
    val connectionState = _connectionState.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _connectionState.value = Connected
        }

        override fun onLost(network: Network) {
            _connectionState.value = InternetUnavailable
        }
    }

    fun setPlayServicesUnavailable() {
        if (connectionState.value == Connected) {
            _connectionState.value = PlayServicesUnavailable
        }
    }

    fun setTtsUnavailable() {
        _connectionState.value = TtsUnavailable
    }

    init {
        connectivityManager.registerDefaultNetworkCallback(callback)
    }
}