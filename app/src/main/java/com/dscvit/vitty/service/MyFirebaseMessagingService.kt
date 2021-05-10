package com.dscvit.vitty.service

import android.content.Context
import com.dscvit.vitty.model.PeriodDetails
import com.dscvit.vitty.notif.NotificationHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val days =
            listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
        val calendar: Calendar = Calendar.getInstance()
        val d = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
        fetchFirestore(applicationContext, days[d], calendar)
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    private fun fetchFirestore(
        context: Context,
        day: String,
        calendar: Calendar,
    ) =
        runBlocking {
            fetchData(context, day, calendar)
        }

    private suspend fun fetchData(
        context: Context,
        day: String,
        calendar: Calendar,
    ) =
        coroutineScope {
            val db = FirebaseFirestore.getInstance()
            val sharedPref = context.getSharedPreferences("login_info", MODE_PRIVATE)!!
            val uid = sharedPref.getString("uid", "")
            if (uid != null) {
                db.collection("users")
                    .document(uid)
                    .collection("timetable")
                    .document(day)
                    .collection("periods")
                    .get()
                    .addOnSuccessListener { result ->
                        var pd = PeriodDetails()
                        for (document in result) {
                            try {
                                calendar.add(Calendar.MINUTE, -15)
                                val start = Calendar.getInstance()
                                start.time = document.getTimestamp("startTime")!!.toDate()
                                if (start.time > calendar.time) {
                                    val end = Calendar.getInstance()
                                    end.time = document.getTimestamp("endTime")!!.toDate()
                                    if (end.time > calendar.time) {
//                                        pd = PeriodDetails(
//                                            document.getString("courseName")!!,
//                                            document.getTimestamp("startTime")!!,
//                                            document.getTimestamp("endTime")!!,
//                                            document.getString("slot")!!,
//                                            document.getString("location")!!
//                                        )

                                        val cn = document.getString("courseName")!!
                                        val simpleDateFormat =
                                            SimpleDateFormat("h:mm a", Locale.getDefault())
                                        val sTime: String =
                                            simpleDateFormat.format(document.getTimestamp("startTime")!!)
                                        NotificationHelper.sendNotification(
                                            context,
                                            "Up Next",
                                            cn,
                                            "You have $cn at $sTime",
                                            cn,
                                            1
                                        )
                                        break
                                    }
                                } else {
                                    pd.courseName = ""
                                    pd.roomNo = ":)"
                                    val simpleDateFormat =
                                        SimpleDateFormat("h:mm a", Locale.getDefault())
                                    val sTime: String =
                                        simpleDateFormat.format(calendar.time)
                                            .toUpperCase(Locale.ROOT)
                                    Timber.d("LOL: $sTime")
                                }
                            } catch (e: Exception) {
                                pd.courseName = ""
                                pd.roomNo = ":)"
                                Timber.d("F: $e")
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Timber.d("Error: $e")
                    }
            }
        }
}
