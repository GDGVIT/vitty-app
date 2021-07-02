package com.dscvit.vitty.notif

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.dscvit.vitty.model.PeriodDetails
import com.dscvit.vitty.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {
    private lateinit var prefs: SharedPreferences

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            prefs = context.getSharedPreferences(Constants.USER_INFO, 0)
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
            fetchFirestore(context, days[d], calendar)
        }
    }

    private fun sendNotif(
        context: Context?,
        pd: PeriodDetails,
        calendar: Calendar,
        start: Calendar
    ) {

        var notifId = prefs.getInt("notif_id", 1) % 20

        val diff = start.timeInMillis - calendar.timeInMillis
        if (diff < 1000 * 60 * 22 && diff > -(1000 * 60 * 5)) {
            val startTime: Date = pd.startTime.toDate()
            val simpleDateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val sTime: String = simpleDateFormat.format(startTime).uppercase(Locale.ROOT)

            if (pd.courseName.trim() != "") {
                if (context != null) {
                    NotificationHelper.sendNotification(
                        context,
                        "Up Next",
                        pd.courseName,
                        "You have ${pd.courseName} at $sTime",
                        pd.courseName,
                        notifId++
                    )
                    prefs.edit().putInt("notif_id", notifId).apply()
                }
            }
        }

//        val mediaPlayer: MediaPlayer = MediaPlayer.create(context, R.raw.alarm)
//        mediaPlayer.isLooping = false
//        mediaPlayer.start()
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
            val sharedPref = context.getSharedPreferences("login_info", Context.MODE_PRIVATE)!!
            val uid = sharedPref.getString("uid", "")
            var pd = PeriodDetails()
            val start = Calendar.getInstance()
            val s = Calendar.getInstance()
            val end = Calendar.getInstance()
            val e = Calendar.getInstance()
            if (uid != null && uid != "") {
                db.collection("users")
                    .document(uid)
                    .collection("timetable")
                    .document(day)
                    .collection("periods")
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            try {
                                s.time = document.getTimestamp("startTime")!!.toDate()
                                start[Calendar.HOUR_OF_DAY] = s[Calendar.HOUR_OF_DAY]
                                start[Calendar.MINUTE] = s[Calendar.MINUTE]
                                if (start.time > calendar.time) {
                                    e.time = document.getTimestamp("endTime")!!.toDate()
                                    end[Calendar.HOUR_OF_DAY] = e[Calendar.HOUR_OF_DAY]
                                    end[Calendar.MINUTE] = e[Calendar.MINUTE]
                                    if (end.time > calendar.time) {
                                        pd = PeriodDetails(
                                            document.getString("courseName")!!,
                                            document.getTimestamp("startTime")!!,
                                            document.getTimestamp("endTime")!!,
                                            document.getString("slot")!!,
                                            document.getString("location")!!
                                        )
                                        break
                                    }
                                } else {
                                    pd.courseName = ""
                                    val simpleDateFormat =
                                        SimpleDateFormat("h:mm a", Locale.getDefault())
                                    val sTime: String =
                                        simpleDateFormat.format(calendar.time)
                                            .uppercase(Locale.ROOT)
                                    Timber.d("LOL: $sTime")
                                }
                            } catch (e: Exception) {
                                pd.courseName = ""
                                Timber.d("F: $e")
                            }
                        }
                        sendNotif(context, pd, calendar, start)
                    }
                    .addOnFailureListener { err ->
                        Timber.d("Error: $err")
                    }
            } else {
                pd.courseName = ""
            }
        }
}
