package com.sommerengineering.signalvoice.firebase

import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.database
import com.sommerengineering.signalvoice.source.Message
import com.sommerengineering.signalvoice.uitls.databaseUrl
import com.sommerengineering.signalvoice.uitls.logMessage
import com.sommerengineering.signalvoice.uitls.messageKey
import com.sommerengineering.signalvoice.uitls.sourceKey
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class FirebaseDatabaseImpl @Inject constructor() {

    private val STREAMS = "streams"
    private val SIGNALS = "signals"
    private val TOKENS = "tokens"
    private val USERS = "users"

    private val db = Firebase.database(databaseUrl)
    private var uid = ""

    fun setUid(newUid: String) {
        if (uid == newUid) return
        uid = newUid
    }

    suspend fun fetchStreamMessages(stream: String) =
        suspendCancellableCoroutine { continuation ->
            db.getReference(STREAMS)
                .child(stream)
                .get()
                .addOnSuccessListener { snapshot ->
                    val messages = snapshot.children.mapNotNull {
                        it.toStreamMessage(stream)
                    }
                    continuation.resume(messages)
                }.addOnFailureListener { continuation.resume(emptyList()) }
        }

    suspend fun fetchUserMessages(): List<Message> {

        // anonymous user has no messages
        val currentUid = uid
        if (currentUid.isEmpty()) return emptyList()

        return suspendCancellableCoroutine { continuation ->
            db.getReference(SIGNALS)
                .child(currentUid)
                .get()
                .addOnSuccessListener { snapshot ->
                    val messages = snapshot.children.mapNotNull {
                        it.toUserMessage()
                    }
                    continuation.resume(messages)
                }.addOnFailureListener { continuation.resume(emptyList()) }
        }
    }

    private fun DataSnapshot.toStreamMessage(stream: String): Message? {

        // validate attributes
        val timestamp = key?.toLongOrNull() ?: return null
        val message = child(messageKey).value as? String ?: return null

        return Message(timestamp, message, stream, null)
    }

    private fun DataSnapshot.toUserMessage(): Message? {

        // validate attributes
        val timestamp = key?.toLongOrNull() ?: return null
        val message = child(messageKey).value as? String ?: return null
        val source = child(sourceKey).value as? String ?: return null

        return Message(timestamp, message, null, source)
    }

    fun writeToken(newToken: String) {

        val currentUid = uid

        // write token: uid
        logMessage("writeToken: newToken=$newToken, currentUid=$currentUid")
        db.getReference(TOKENS)
            .child(newToken)
            .setValue(currentUid)

        // write uid: token
        if (currentUid.isEmpty()) return
        db.getReference(USERS)
            .child(currentUid)
            .setValue(newToken)
    }
}