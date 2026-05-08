package com.sommerengineering.signalvoice

enum class UpdateRequirement {
    NONE,
    OPTIONAL,
    REQUIRED
}

data class UpdatePolicy(
    val minimumSupportedVersionCode: Int,
    val latestRecommendedVersionCode: Int
)

