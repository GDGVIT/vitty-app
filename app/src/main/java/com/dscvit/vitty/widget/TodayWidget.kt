package com.dscvit.vitty.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import com.dscvit.vitty.R
import com.dscvit.vitty.activity.AuthActivity
import com.dscvit.vitty.service.TodayWidgetService
import com.dscvit.vitty.util.ArraySaverLoader.saveArray
import com.dscvit.vitty.util.Constants.PERIODS
import com.dscvit.vitty.util.Constants.TIME_SLOTS
import com.dscvit.vitty.util.Constants.TODAY_INTENT
import com.dscvit.vitty.util.UtilFunctions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Implementation of App Widget functionality.
 */
class TodayWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateTodayWidget(context, appWidgetManager, appWidgetId, null, null, null)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateTodayWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    courseList: ArrayList<String>?,
    timeList: ArrayList<String>?,
    roomList: ArrayList<String>?
) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.today_widget)
    val intent = Intent(context, AuthActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        context,
        TODAY_INTENT,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.today_widget, pendingIntent)
    val days = listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
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

    if (courseList == null) {
        fetchTodayFirestore(context, days[d], appWidgetManager, appWidgetId)
    } else if (courseList.isNotEmpty() || courseList.isEmpty()) {
        saveArray(courseList, "courses_today", context)
        saveArray(timeList!!, "time_today", context)
        saveArray(roomList!!, "class_rooms", context)
        val serviceIntent = Intent(context, TodayWidgetService::class.java)
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        val bundle1 = Bundle()
        bundle1.putStringArrayList(
            PERIODS,
            courseList
        )
        val bundle2 = Bundle()
        bundle2.putStringArrayList(
            TIME_SLOTS,
            timeList
        )
        views.setRemoteAdapter(R.id.periods, serviceIntent)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.periods)
    }

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

fun fetchTodayFirestore(
    context: Context,
    day: String,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
) =
    runBlocking {
        fetchTodayData(context, day, appWidgetManager, appWidgetId)
    }

suspend fun fetchTodayData(
    context: Context,
    oldDay: String,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
) =
    coroutineScope {
        val db = FirebaseFirestore.getInstance()
        val sharedPref = context.getSharedPreferences("login_info", Context.MODE_PRIVATE)!!
        val day = if (oldDay == "saturday") sharedPref.getString(
            UtilFunctions.getSatModeCode(),
            "saturday"
        ).toString() else oldDay
        val uid = sharedPref.getString("uid", "")
        val courseList: ArrayList<String> = ArrayList()
        val timeList: ArrayList<String> = ArrayList()
        val roomList: ArrayList<String> = ArrayList()
        if (uid != null && uid != "") {
            db.collection("users")
                .document(uid)
                .collection("timetable")
                .document(day)
                .collection("periods")
                .get(Source.CACHE)
                .addOnSuccessListener { result ->
                    for (document in result) {
                        try {
                            val startTime: Date = document.getTimestamp("startTime")!!.toDate()
                            val simpleDateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                            val sTime: String =
                                simpleDateFormat.format(startTime).uppercase()

                            val endTime: Date = document.getTimestamp("endTime")!!.toDate()
                            val eTime: String =
                                simpleDateFormat.format(endTime).uppercase()

                            courseList.add(document.getString("courseName")!!)
                            timeList.add("$sTime - $eTime")
                            roomList.add(document.getString("location")!!)
                        } catch (e: Exception) {
                            Timber.d("Error: $e")
                        }
                    }
                    updateTodayWidget(context, appWidgetManager, appWidgetId, courseList, timeList, roomList)
                }
                .addOnFailureListener { e ->
                    Timber.d("Error YO: $e")
                }
        } else {
            updateTodayWidget(context, appWidgetManager, appWidgetId, courseList, timeList, roomList)
        }
    }
