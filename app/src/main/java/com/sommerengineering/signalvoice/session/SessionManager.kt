package com.sommerengineering.signalvoice.session

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.sommerengineering.signalvoice.ApplicationScope
import com.sommerengineering.signalvoice.BuildConfig
import com.sommerengineering.signalvoice.PREMIUM
import com.sommerengineering.signalvoice.PreferenceStore
import com.sommerengineering.signalvoice.UID
import com.sommerengineering.signalvoice.premium.BillingManager
import com.sommerengineering.signalvoice.session.Session.Authenticated
import com.sommerengineering.signalvoice.session.Session.Guest
import com.sommerengineering.signalvoice.uitls.logException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val prefs: PreferenceStore,
    private val credentialManager: CredentialManager,
    private val billingManager: BillingManager
) {

    private val auth = FirebaseAuth.getInstance()

    private val _session = MutableStateFlow<Session>(Guest)
    val session = _session.asStateFlow()

    private var entitlementJob: Job? = null

    val uid: String
        get() = (session.value as? Authenticated)?.uid ?: ""

    val isPremium: Boolean
        get() = (session.value as? Authenticated)?.isPremium ?: false

    private fun onAuth() {

        val currentUser = auth.currentUser

        // sign-out
        if (currentUser == null) {
            _session.value = Guest
            return
        }

        val newUid = currentUser.uid

        // dedupe
        val currentUid = uid
        if (newUid == currentUid) return

        // initialize user without premium
        _session.value = Authenticated(
            uid = newUid,
            isPremium = false
        )

        // check premium entitlement
        entitlementJob?.cancel()
        entitlementJob = appScope.launch {

            // initialize entitlement from cache
            var isPremium = loadPremium(newUid)
            updateSession(newUid, isPremium)

            // fetch entitlement from network
            isPremium =
                if (BuildConfig.DEBUG) true
                else billingManager.isPremium()
            updateSession(newUid, isPremium)
        }
    }

    private fun updateSession(
        newUid: String,
        isPremium: Boolean
    ) {

        // prevent race: validate session still active, and same user
        if (newUid != uid) return

        // update entitlement
        val current = _session.value as? Authenticated ?: return
        _session.value = current.copy(
            isPremium = isPremium
        )

        // persist entitlement to cache
        appScope.launch {
            updatePremium(newUid, isPremium)
        }
    }

    private suspend fun loadPremium(
        uid: String
    ): Boolean {

        // retrieve cache
        val storedUid = prefs.read(UID)
        val storedPremium = prefs.read(PREMIUM) ?: false

        return storedUid == uid && storedPremium
    }

    private suspend fun updatePremium(
        uid: String,
        isPremium: Boolean
    ) {

        // dedupe
        val storedPremium = loadPremium(uid)
        if (isPremium == storedPremium) return

        prefs.write(UID, uid)
        prefs.write(PREMIUM, isPremium)
    }

    suspend fun signInWithCredential(
        credential: AuthCredential?
    ): Boolean {

        if (credential == null) return false

        // launches system google sign-in bottom sheet
        return try {
            auth.signInWithCredential(credential)
                .await()
            true

        } catch (e: Exception) {
            logException(e)
            false
        }
    }

    suspend fun signInWithProvider(
        context: Context,
        provider: OAuthProvider
    ): Boolean {

        // launches web browser and backgrounds app
        return try {
            auth.startActivityForSignInWithProvider(
                context as ComponentActivity,
                provider
            ).await()
            true

        } catch (e: Exception) {
            logException(e)
            false
        }
    }

    fun signOut() {
        auth.signOut()
        appScope.launch {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        }
    }

    init {

        // listen for purchase event
        appScope.launch {
            billingManager.purchaseEvents.collect {
                updateSession(
                    newUid = uid,
                    isPremium = true
                )
            }
        }

        // listen for auth state changes
        auth.addAuthStateListener { onAuth() }
    }
}
