package com.dscvit.vitty.service

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.dscvit.vitty.R
import com.dscvit.vitty.util.Constants.PERIODS
import com.dscvit.vitty.util.Constants.TIME_SLOTS
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

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
    private var dataList: ArrayList<String>
    private var timeList: ArrayList<String>
    private var appWidgetId: Int
    private val intent1: Intent = intent

    init {
        val sharedPref = context.getSharedPreferences("login_info", Context.MODE_PRIVATE)!!
        var upNo = sharedPref.getInt("update_no", 0)
        val bundle1 = intent.getBundleExtra(PERIODS)!!
        val bundle2 = intent.getBundleExtra(TIME_SLOTS)!!
        dataList = bundle1.getStringArrayList(PERIODS)!!
        timeList = bundle2.getStringArrayList(TIME_SLOTS)!!
        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
    }

    override fun onCreate() {

    }

    override fun onDataSetChanged() {
        fetch(intent1)
    }

    override fun onDestroy() {
        return dataList.clear()
    }

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.item_period)
        views.setTextViewText(R.id.course_name_widget, dataList[position])
        views.setTextViewText(R.id.period_time, timeList[position])
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

    private fun fetch(intent: Intent){
        if (intent.extras != null) {
            if (intent.extras!!.containsKey(PERIODS)) {
                val bundle1 = intent.getBundleExtra(PERIODS)!!
                val bundle2 = intent.getBundleExtra(TIME_SLOTS)!!
                dataList = bundle1.getStringArrayList(PERIODS)!!
                timeList = bundle2.getStringArrayList(TIME_SLOTS)!!
                appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
            }
        }
    }
}