package com.dscvit.vitty.util

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.dscvit.vitty.util.Constants.SAT_MODE
import com.dscvit.vitty.widget.NextClassWidget
import com.dscvit.vitty.widget.TodayWidget
import java.util.Calendar

object UtilFunctions {

    fun openLink(context: Context, url: String) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
            )
        } catch (e: Exception) {
            Toast.makeText(context, "Browser not found!", Toast.LENGTH_LONG).show()
        }
    }

    fun reloadWidgets(context: Context) {
        reloadWidget(context, NextClassWidget::class.java)
        reloadWidget(context, TodayWidget::class.java)
    }

    private fun reloadWidget(context: Context, cls: Class<*>) {
        val intent = Intent(context, cls)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, cls))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }

    fun getWeekYear(): String {
        val now: Calendar = Calendar.getInstance()
        return now.get(Calendar.WEEK_OF_YEAR).toString() + now.get(Calendar.YEAR).toString()
    }

    fun getSatModeCode(): String {
        return SAT_MODE + getWeekYear()
    }
}
