package com.dscvit.vitty.notif

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dscvit.vitty.R
import com.dscvit.vitty.activity.AuthActivity
import com.dscvit.vitty.util.Constants.NOTIF_INTENT

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
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
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

        if (classId != "") {
            val clickIntent = Intent(context, AuthActivity::class.java)
            clickIntent.putExtra("classId", classId)
            val mapPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(R.drawable.ic_nav, "Directions", mapPendingIntent)
        }

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}
