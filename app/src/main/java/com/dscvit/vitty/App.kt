package com.dscvit.vitty

import android.app.Application
import com.dscvit.vitty.util.RemoteConfigUtils.LATEST_VERSION
import com.dscvit.vitty.util.RemoteConfigUtils.ONLINE_MODE
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import timber.log.Timber

class App : Application() {

    private lateinit var remoteConfig: FirebaseRemoteConfig

    private val defaultConfig: HashMap<String, Any> =
        hashMapOf(
            LATEST_VERSION to BuildConfig.VERSION_CODE,
            ONLINE_MODE to false
        )

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
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
        remoteConfig.setDefaultsAsync(defaultConfig)

        remoteConfig.fetchAndActivate().addOnCompleteListener {
            Timber.d("addOnCompleteListener")
        }

        return remoteConfig
    }
}
