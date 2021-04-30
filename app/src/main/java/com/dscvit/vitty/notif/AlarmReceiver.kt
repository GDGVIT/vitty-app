package com.dscvit.vitty.notif

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    var i = 0
    override fun onReceive(context: Context?, intent: Intent?) {

        if (context != null) {
            NotificationHelper.sendNotification(
                context,
                "Cool Notif Title",
                "Sekrt Msg",
                "WOW this is such a cool notification. I love it! WOWOW Mazza aa gaya!",
                "Notify",
                i++
            )
        }

//        val mediaPlayer: MediaPlayer = MediaPlayer.create(context, R.raw.alarm)
//        mediaPlayer.isLooping = false
//        mediaPlayer.start()
    }
}
