package com.dscvit.vitty.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.dscvit.vitty.util.Constants
import com.dscvit.vitty.util.Constants.ALARM_INTENT
import com.dscvit.vitty.util.Constants.EXAM_MODE
import com.dscvit.vitty.util.Constants.USER_INFO
import com.dscvit.vitty.util.UtilFunctions.reloadWidgets
import java.util.Date

class DeviceBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action.equals(
                "android.intent.action.BOOT_COMPLETED"
            ) || intent.action.equals(
                    "android.intent.action.MY_PACKAGE_REPLACED"
                )
        ) {
            val prefs = context?.getSharedPreferences(USER_INFO, 0)
            if (!prefs?.getBoolean(EXAM_MODE, false)!!) {
                val i = Intent(context, AlarmReceiver::class.java)
                val pendingIntent =
                    PendingIntent.getBroadcast(
                        context, ALARM_INTENT, i,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                val alarmManager =
                    context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
                val date = Date().time
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    date,
                    (1000 * 60 * Constants.NOTIF_DELAY).toLong(),
                    pendingIntent
                )
            }
            reloadWidgets(context)
        }
    }
}
