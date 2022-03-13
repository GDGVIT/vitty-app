package com.dscvit.vitty.util

import com.google.firebase.remoteconfig.FirebaseRemoteConfig

object RemoteConfigUtils {

    const val LATEST_VERSION = "latest_version_android"
    const val ONLINE_MODE = "online_college_mode"

    fun getLatestVersion(): Long = FirebaseRemoteConfig.getInstance().getLong(LATEST_VERSION)

    fun getOnlineMode(): Boolean = FirebaseRemoteConfig.getInstance().getBoolean(ONLINE_MODE)

    fun getOnlineModeDetails(): String {
        val status: Boolean = FirebaseRemoteConfig.getInstance().getBoolean(ONLINE_MODE)
        return if (status) "Online" else "Offline"
    }
}
