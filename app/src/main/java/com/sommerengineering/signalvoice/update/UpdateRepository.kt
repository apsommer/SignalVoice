package com.sommerengineering.signalvoice.update

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.sommerengineering.signalvoice.R
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepository @Inject constructor() {

    private val minimumSupportedVersionCode = "minimum_supported_version_code"
    private val latestRecommendedVersionCode = "latest_recommended_version_code"

    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    suspend fun refresh() {
        remoteConfig.fetchAndActivate().await()
    }

    fun getUpdateRequirement(
        currentVersionCode: Int
    ): UpdateRequirement {

        // fetch remote values
        val minimumSupportedVersionCode = remoteConfig
            .getLong(minimumSupportedVersionCode)
            .toInt()

        val latestRecommendedVersionCode = remoteConfig
            .getLong(latestRecommendedVersionCode)
            .toInt()

        // return requirement based on current version
        return when {
            minimumSupportedVersionCode > currentVersionCode -> UpdateRequirement.REQUIRED
            latestRecommendedVersionCode > currentVersionCode -> UpdateRequirement.OPTIONAL
            else -> UpdateRequirement.NONE
        }
    }

    init {

        // prevent aggressive caching
        val settings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0L
        }

        remoteConfig.setConfigSettingsAsync(settings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }
}