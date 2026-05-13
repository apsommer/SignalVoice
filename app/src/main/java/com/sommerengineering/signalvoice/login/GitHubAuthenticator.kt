package com.sommerengineering.signalvoice.login

import com.google.firebase.auth.OAuthProvider
import com.sommerengineering.signalvoice.uitls.gitHubProviderId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubAuthenticator @Inject constructor() {

    val provider =
        OAuthProvider.newBuilder(gitHubProviderId).build()
}