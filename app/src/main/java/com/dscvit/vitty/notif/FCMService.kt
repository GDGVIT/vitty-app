package com.dscvit.vitty.notif

import com.dscvit.vitty.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class FCMService : FirebaseMessagingService() {
    override fun onMessageReceived(msg: RemoteMessage) {
        super.onMessageReceived(msg)
        msg.notification.let {
            if (it != null) {
                NotificationHelper.sendNotification(
                    this,
                    it.title!!,
                    it.body!!,
                    it.body!!,
                    getString(R.string.default_notification_channel_name),
                    Integer.MAX_VALUE,
                )
            }
        }
    }

    override fun onNewToken(token: String) {
        Timber.d("TOKEN FCM: $token")
    }
}
