package com.sommerengineering.signalvoice.session

sealed interface ConnectionState {
    data object Connected : ConnectionState
    data object InternetUnavailable : ConnectionState
    data object PlayServicesUnavailable : ConnectionState
}