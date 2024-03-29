package com.dscvit.vitty.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dscvit.vitty.R
import com.dscvit.vitty.activity.AuthActivity
import com.dscvit.vitty.activity.NavigationActivity
import com.dscvit.vitty.receiver.AlarmReceiver
import com.dscvit.vitty.util.Constants.GROUP_ID_2
import com.dscvit.vitty.util.Constants.NOTIF_INTENT
import java.util.Date

object NotificationHelper {
    fun createNotificationChannel(
        context: Context,
        name: String,
        descriptionText: String,
        groupId: String,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "${context.packageName}-$name"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                group = groupId
            }

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            channel.setSound(
                Uri.parse("android.resource://" + context.packageName + "/" + R.raw.notification),
                audioAttributes
            )
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            try {
                notificationManager.createNotificationChannel(channel)
            } catch (e: Exception) {
                try {
                    var groupName = context.getString(R.string.notif_group)
                    if (groupId == GROUP_ID_2) {
                        groupName = context.getString(R.string.gdscvit)
                    }
                    createNotificationGroup(context, groupName, groupId)
                    notificationManager.createNotificationChannel(channel)
                } catch (e: Exception) {
                    Toast.makeText(context, "An unknown error occurred :(", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    fun createNotificationGroup(
        context: Context,
        groupName: String,
        groupId: String,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannelGroup(
                NotificationChannelGroup(
                    groupId,
                    groupName
                )
            )
        }
    }

    fun deleteNotificationChannel(
        context: Context,
        name: String,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "${context.packageName}-$name"
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.deleteNotificationChannel(channelId)
        }
    }

    fun sendNotification(
        context: Context,
        title: String,
        text: String,
        bigText: String,
        channelName: String,
        notificationId: Int,
        classId: String
    ) {
        val channelId = "${context.packageName}-$channelName"
        val intent = Intent(context, AuthActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, NOTIF_INTENT, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notif)
            .setColor(ContextCompat.getColor(context, R.color.background))
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(bigText)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (classId != "" && !RemoteConfigUtils.getOnlineMode()) {
            val clickIntent = Intent(context, NavigationActivity::class.java)
            clickIntent.putExtra("classId", classId)
            val mapPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(R.drawable.ic_nav, "Directions to $classId", mapPendingIntent)
        }

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    fun setAlarm(context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            Constants.ALARM_INTENT,
            intent,
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

    fun cancelAlarm(context: Context) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, Constants.ALARM_INTENT, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
