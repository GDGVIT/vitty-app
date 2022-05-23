package com.dscvit.vitty.util

import android.content.Context
import android.os.PowerManager
import com.dscvit.vitty.BuildConfig
import com.dscvit.vitty.R
import com.dscvit.vitty.util.Constants.EXAM_MODE

object Report {
    fun details(context: Context): String {
        val prefs = context.getSharedPreferences(Constants.USER_INFO, 0)
        val pm: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val appName = context.getString(R.string.app_name)
        val appId = BuildConfig.APPLICATION_ID
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE
        val batteryOptimizations =
            if (pm.isIgnoringBatteryOptimizations(context.packageName)) "Off" else "On"
        val collegeMode = RemoteConfigUtils.getOnlineModeDetails()
        val examMode = if (prefs.getBoolean(EXAM_MODE, false)) "On" else "Off"
        return "App Name: $appName\nApp ID: $appId\nVersion Name: $versionName\nVersion Code: $versionCode\nBattery Optimizations: $batteryOptimizations\nCollege Mode: $collegeMode\nExam Mode: $examMode"
    }
}
