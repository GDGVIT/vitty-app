package com.dscvit.vitty.receiver

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_CHOSEN_COMPONENT
import com.dscvit.vitty.util.Analytics

class ShareReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val clickedComponent: ComponentName? = intent?.getParcelableExtra(EXTRA_CHOSEN_COMPONENT)
        if (clickedComponent != null) {
            Analytics.share(clickedComponent.packageName)
        }
    }
}
