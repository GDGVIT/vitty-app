package com.dscvit.vitty.util

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import java.util.Calendar

object Analytics {
    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics
    fun share(packageName: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE) {
            param(FirebaseAnalytics.Param.CONTENT_TYPE, "text")
            param(FirebaseAnalytics.Param.ITEM_ID, Calendar.getInstance().timeInMillis)
            param(FirebaseAnalytics.Param.METHOD, packageName)
        }
    }

    fun notification(className: String) {
        firebaseAnalytics.logEvent("class_notifications") {
            param(FirebaseAnalytics.Param.CONTENT_TYPE, "text")
            param(FirebaseAnalytics.Param.ITEM_ID, Calendar.getInstance().timeInMillis)
            param("class_name", className)
        }
    }

    fun navigation(block: String) {
        firebaseAnalytics.logEvent("navigation") {
            param(FirebaseAnalytics.Param.CONTENT_TYPE, "text")
            param(FirebaseAnalytics.Param.ITEM_ID, Calendar.getInstance().timeInMillis)
            param("block", block)
        }
    }
}
