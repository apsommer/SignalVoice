package com.sommerengineering.signalvoice.login

sealed class Session {

    abstract val uid: String
    abstract val isPremium: Boolean

    data object Guest : Session() {
        override val uid = ""
        override val isPremium = false
    }

    data class Authenticated(
        override val uid: String,
        override val isPremium: Boolean
    ) : Session()
}