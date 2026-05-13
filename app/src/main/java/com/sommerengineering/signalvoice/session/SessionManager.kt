package com.sommerengineering.signalvoice.session

import com.google.firebase.auth.FirebaseAuth
import com.sommerengineering.signalvoice.ApplicationScope
import com.sommerengineering.signalvoice.BuildConfig
import com.sommerengineering.signalvoice.PREMIUM
import com.sommerengineering.signalvoice.PreferenceStore
import com.sommerengineering.signalvoice.UID
import com.sommerengineering.signalvoice.premium.BillingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val prefs: PreferenceStore,
    private val billingManager: BillingManager
) {

    private val auth = FirebaseAuth.getInstance()

    private val _session = MutableStateFlow<Session?>(null)
    val session = _session.asStateFlow()

    private var entitlementJob: Job? = null

    val uid: String
        get() = _session.value?.uid ?: error("Session not initialized")

    private fun onAuth() {

        val newUid = auth.currentUser?.uid ?: return

        // dedupe
        val currentUid = _session.value?.uid
        if (newUid == currentUid) return

        // initialize user without premium
        _session.value = Session(
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
        uid: String,
        isPremium: Boolean
    ) {

        // prevent race: validate session still active, and same user
        val current = _session.value ?: return
        if (current.uid != uid) return

        // update entitlement
        _session.value = current.copy(
            isPremium = isPremium
        )

        // persist entitlement to cache
        appScope.launch {
            updatePremium(uid, isPremium)
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

    init {

        // listen for purchase event
        appScope.launch {
            billingManager.purchaseEvents.collect {
                updateSession(
                    uid = uid,
                    isPremium = true
                )
            }
        }

        // check for anonymous guest
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) auth.signInAnonymously()

        // listen for auth state changes
        auth.addAuthStateListener { onAuth() }
    }
}
