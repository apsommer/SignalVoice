package com.sommerengineering.signalvoice

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

enum class UpdateRequirement {
    NONE,
    OPTIONAL,
    REQUIRED
}

data class UpdatePolicy(
    val minimumSupportedVersionCode: Int,
    val latestRecommendedVersionCode: Int
)

@Singleton
class UpdateManager @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope
) {

    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        val settings = remoteConfigSettings {
            minimumFetchIntervalInSeconds =
                if (BuildConfig.DEBUG) 0
                else 3600L
        }
        remoteConfig.setConfigSettingsAsync(settings)
    }

    suspend fun refresh() {
        remoteConfig.fetchAndActivate().await()
    }

    val minimumSupportedVersionCode = remoteConfig
        .getLong("minimum_supported_version_code")
        .toInt()

    val latestRecommendedVersionCode = remoteConfig
        .getLong("latest_recommended_version_code")
        .toInt()
}