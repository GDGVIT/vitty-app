package com.dscvit.vitty.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.widget.RemoteViews
import com.dscvit.vitty.R
import com.dscvit.vitty.model.PeriodDetails
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementation of App Widget functionality.
 */
class NextClassWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateNextClassWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        if (context != null) {
            if (appWidgetManager != null) {
                updateNextClassWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateNextClassWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val widgetText = context.getString(R.string.appwidget_text)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.next_class_widget)
    val days = listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
    val calendar: Calendar = Calendar.getInstance()
//    views.setTextViewText(R.id.course_name, calendar.get(Calendar.MINUTE).toString())
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
    val db = FirebaseFirestore.getInstance()
    val sharedPref = context.getSharedPreferences("login_info", Context.MODE_PRIVATE)!!
    val uid = sharedPref.getString("uid", "")
    if (uid != null) {
        db.collection("users")
            .document(uid)
            .collection("timetable")
            .document(days[d])
            .collection("periods")
            .get()
            .addOnSuccessListener { result ->
                var pd: PeriodDetails? = null
                for (document in result) {
                    try {
                        val start = Calendar.getInstance()
                        start.time = document.getTimestamp("startTime")!!.toDate()
                        if (start.get(Calendar.HOUR) > calendar.get(Calendar.HOUR)) {
                            val end = Calendar.getInstance()
                            end.time = document.getTimestamp("endTime")!!.toDate()
                            if (end.get(Calendar.HOUR) > calendar.get(Calendar.HOUR)) {
                                pd = PeriodDetails(
                                    document.getString("courseName")!!,
                                    document.getTimestamp("startTime")!!,
                                    document.getTimestamp("endTime")!!,
                                    document.getString("slot")!!,
                                    document.getString("location")!!
                                )
                                break
                            }
                        }

                    } catch (e: Exception) {
                        // TODO
                    }
                }
                val startTime: Date = pd?.startTime?.toDate() as Date
                val simpleDateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                val sTime: String = simpleDateFormat.format(startTime).toUpperCase(Locale.ROOT)

                val endTime: Date = pd.endTime.toDate() as Date
                val eTime: String = simpleDateFormat.format(endTime).toUpperCase(Locale.ROOT)


                views.setTextViewText(R.id.course_name, pd.courseName)
                views.setTextViewText(R.id.period_time, "$sTime - $eTime")
                views.setTextViewText(R.id.slot_id, pd.slot)
                views.setTextViewText(R.id.class_id, pd.roomNo)
            }
            .addOnFailureListener { e ->
                Timber.d("Auth error: $e")
            }

    }


    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
