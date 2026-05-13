package com.sommerengineering.signalvoice.login

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.sommerengineering.signalvoice.R
import com.sommerengineering.signalvoice.uitls.logException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthenticator @Inject constructor(
    private val credentialManager: CredentialManager
) {

    suspend fun getCredential(context: Context): AuthCredential? {

        try {

            // bottom sheet ui
            val signInOptions = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // false to initiate sign-up flow, if needed
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(true)
                .build()

            // build google sign-in request
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOptions)
                .build()

            // launch system google sign-in dialog
            val response = credentialManager.getCredential(context, request)

            // extract google id token
            val credential = response.credential
            if (credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                return null
            }
            val googleToken = GoogleIdTokenCredential.createFrom(credential.data).idToken

            // sign-in to firebase with google id token
            return GoogleAuthProvider.getCredential(googleToken, null)

        } catch (e: Exception) {

            // ignore user cancelled dialog
            if (e !is GetCredentialCancellationException) {
                logException(e)
            }
        }

        return null
    }
}