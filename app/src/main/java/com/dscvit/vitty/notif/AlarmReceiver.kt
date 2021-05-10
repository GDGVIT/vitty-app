package com.dscvit.vitty.notif

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    var i = 0
    override fun onReceive(context: Context?, intent: Intent?) {

        var name = ""
        if (intent != null) {
            name = intent.getStringExtra("CLASS_NAME").toString()
        }

        if (context != null) {
            NotificationHelper.sendNotification(
                context,
                name,
                name,
                "WOW this is such a cool notification. $name. I love it! WOWOW Mazza aa gaya!",
                "Other",
                i++
            )
        }

//        val mediaPlayer: MediaPlayer = MediaPlayer.create(context, R.raw.alarm)
//        mediaPlayer.isLooping = false
//        mediaPlayer.start()
    }
}
