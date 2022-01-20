package com.dscvit.vitty.util

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import timber.log.Timber

object RemoteConfigUtils {

    private const val LATEST_VERSION = "latest_version_android"
    private const val ONLINE_MODE = "online_college_mode"

    private val DEFAULTS: HashMap<String, Any> =
        hashMapOf(
            LATEST_VERSION to BuildConfig.VERSION_CODE,
            ONLINE_MODE to true
        )

    private lateinit var remoteConfig: FirebaseRemoteConfig

    fun init() {
        remoteConfig = getFirebaseRemoteConfig()
    }

    private fun getFirebaseRemoteConfig(): FirebaseRemoteConfig {

        val remoteConfig = Firebase.remoteConfig

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) {
                0
            } else {
                30 * 60
            }
        }

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(DEFAULTS)

        remoteConfig.fetchAndActivate().addOnCompleteListener {
            Timber.d("addOnCompleteListener")
        }

        return remoteConfig
    }

    fun getLatestVersion(): Long = remoteConfig.getLong(LATEST_VERSION)

    fun getOnlineMode(): Boolean = remoteConfig.getBoolean(ONLINE_MODE)

    fun getOnlineModeDetails(): String {
        val status: Boolean = remoteConfig.getBoolean(ONLINE_MODE)
        return if (status) "Online" else "Offline"
    }
}
