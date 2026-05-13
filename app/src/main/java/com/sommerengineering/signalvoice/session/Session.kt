package com.sommerengineering.signalvoice.session

sealed class Session {
    object Guest : Session()
    data class Authenticated(
        val uid: String,
        val isPremium: Boolean
    ) : Session()
}
