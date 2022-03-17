package com.dscvit.vitty.service

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.dscvit.vitty.R
import timber.log.Timber

class TodayWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return AppWidgetListView(
            this.applicationContext,
            intent
        )
    }
}

class AppWidgetListView(
    private val context: Context,
    intent: Intent,
) : RemoteViewsService.RemoteViewsFactory {
    private var dataList: Array<String?>
    private var timeList: Array<String?>
    private var roomList: Array<String?>
    private var appWidgetId: Int

    init {
        dataList = loadArray("courses_today", context)
        timeList = loadArray("time_today", context)
        roomList = loadArray("class_rooms", context)
        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
    }

    override fun onCreate() {
        fetch()
    }

    override fun onDataSetChanged() {
        fetch()
    }

    override fun onDestroy() {
    }

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.item_period)
        try {
            if (dataList.isNotEmpty()) {
                views.setTextViewText(R.id.course_name_widget, dataList[position])
                views.setTextViewText(R.id.period_time, timeList[position])
                views.setTextViewText(R.id.room_num, roomList[position])
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            Timber.d(e.toString())
        }
        return views
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    private fun fetch() {
        dataList = loadArray("courses_today", context)
        timeList = loadArray("time_today", context)
    }

    private fun loadArray(arrayName: String, context: Context): Array<String?> {
        val prefs = context.getSharedPreferences("login_info", Context.MODE_PRIVATE)
        val size = prefs.getInt(arrayName + "_size", 0)
        val array = arrayOfNulls<String>(size)
        for (i in 0 until size) array[i] = prefs.getString(arrayName + "_" + i, null)
        return array
    }
}
