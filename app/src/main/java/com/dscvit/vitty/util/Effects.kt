package com.dscvit.vitty.util

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.dscvit.vitty.util.Constants.VIBRATION_MODE

object Effects {

    fun vibrateOnClick(context: Context) {
        val prefs = context.getSharedPreferences(Constants.USER_INFO, 0)
        if (prefs.getBoolean(VIBRATION_MODE, false)) {
            val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                context.getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(1, 1))
            } else {
                vib.vibrate(1)
            }
        }
    }
}
