package com.sommerengineering.signalvoice.login

import android.content.Context
import androidx.activity.ComponentActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.auth
import com.sommerengineering.signalvoice.uitls.gitHubProviderId
import com.sommerengineering.signalvoice.uitls.logException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubAuthenticator @Inject constructor() {

    private val provider =
        OAuthProvider.newBuilder(gitHubProviderId).build()

    suspend fun signIn(
        context: Context
    ): Boolean {

        return try {

            // launches web browser and backgrounds app
            Firebase.auth
                .startActivityForSignInWithProvider(
                    context as ComponentActivity,
                    provider
                )
                .await()

            true

        } catch (e: Exception) {

            logException(e)
            false
        }
    }
}