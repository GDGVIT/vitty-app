package com.dscvit.vitty.util

import android.appwidget.AppWidgetManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import com.dscvit.vitty.util.Constants.SAT_MODE
import com.dscvit.vitty.widget.NextClassWidget
import com.dscvit.vitty.widget.TodayWidget
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    fun copyItem(context: Context, item: String, label: String, url: String) {
        Toast.makeText(
            context,
            "$item Copied",
            Toast.LENGTH_LONG
        ).show()
        val clipboard: ClipboardManager? =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText(label, url)
        clipboard?.setPrimaryClip(clip)
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

    private fun getWeekYear(): String {
        val now: Calendar = Calendar.getInstance()
        return now.get(Calendar.WEEK_OF_YEAR).toString() + now.get(Calendar.YEAR).toString()
    }

    fun getSatModeCode(): String {
        return SAT_MODE + getWeekYear()
    }

    fun isUpdated(document: DocumentSnapshot, preferences: SharedPreferences): Boolean {
        return try {
            val oldTimetableVersion = preferences.getLong("TIMETABLE_VERSION", 0)
            val timetableVersion = document.getLong("timetableVersion")
            if (timetableVersion!! > 0)
                oldTimetableVersion < timetableVersion
            else
                document.getBoolean("isUpdated") == true
        } catch (e: Exception) {
            document.getBoolean("isUpdated") == true
        }
    }

    fun getBitmapFromView(view: View): Bitmap {
        val bitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun takeScreenshotAndShare(context: Context, bitmap: Bitmap) {
        val dateFormatter = SimpleDateFormat(
            "yyyy-MM-dd 'at' HH-mm-ss z", Locale.getDefault()
        )
        val bitmapPath: String =
            MediaStore.Images.Media.insertImage(
                context.contentResolver, bitmap,
                "VITTY Schedule taken on ${
                dateFormatter.format(
                    Date()
                )
                }",
                null
            )
        val bitmapUri = Uri.parse(bitmapPath)

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/png"
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri)
        context.startActivity(Intent.createChooser(intent, "Share"))
    }
}
