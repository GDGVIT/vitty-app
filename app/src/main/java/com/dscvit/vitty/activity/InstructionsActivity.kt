package com.dscvit.vitty.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.dscvit.vitty.R
import com.dscvit.vitty.databinding.ActivityInstructionsBinding
import com.dscvit.vitty.notif.AlarmReceiver
import com.dscvit.vitty.notif.NotificationHelper
import com.dscvit.vitty.util.Constants.NOTIFICATION_CHANNELS
import com.dscvit.vitty.util.Constants.NOTIF_DELAY
import com.dscvit.vitty.util.Constants.TIMETABLE_AVAILABLE
import com.dscvit.vitty.util.Constants.UID
import com.dscvit.vitty.util.Constants.UPDATE
import com.dscvit.vitty.util.Constants.USER_INFO
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber
import java.util.Date

class InstructionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInstructionsBinding
    private val days =
        listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
    private lateinit var prefs: SharedPreferences
    private val db = FirebaseFirestore.getInstance()
    private var uid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_instructions)
        prefs = getSharedPreferences(USER_INFO, 0)
        uid = prefs.getString(UID, "").toString()

        binding.doneButton.setOnClickListener {
            setupDoneButton()
        }
    }

    override fun onStart() {
        super.onStart()
        if (prefs.getInt(UPDATE, 0) == 1) {
            createNotificationChannels()
            Toast.makeText(this, getString(R.string.updated), Toast.LENGTH_LONG)
                .show()
        }
        if (prefs.getInt(TIMETABLE_AVAILABLE, 0) == 1) {
            setAlarm()
            val intent = Intent(this, ScheduleActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupDoneButton() {
        binding.loadingView.visibility = View.VISIBLE
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.getBoolean("isTimetableAvailable") == true) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        createNotificationChannels()
                    } else {
                        tellUpdated()
                    }
                } else {
                    binding.loadingView.visibility = View.GONE
                    Toast.makeText(this, getString(R.string.follow_instructions), Toast.LENGTH_LONG)
                        .show()
                }
            }
    }

    private fun createNotificationChannels() {
        binding.loadingView.visibility = View.VISIBLE
        val notifChannels = loadArray(NOTIFICATION_CHANNELS, this)
        for (notifChannel in notifChannels) {
            if (notifChannel != null) {
                NotificationHelper.deleteNotificationChannel(this, notifChannel.toString())
            }
        }
        val newNotifChannels: ArrayList<String> = ArrayList()
        for (day in days) {
            db.collection("users")
                .document(uid)
                .collection("timetable")
                .document(day)
                .collection("periods")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        var cn = document.getString("courseName")
                        cn = if (cn.isNullOrEmpty()) "Default" else cn
                        val cc = document.getString("courseCode")
                        NotificationHelper.createNotificationChannel(
                            this,
                            cn,
                            "Course Code: $cc",
                        )
                        newNotifChannels.add(cn)
                        Timber.d(cn)
                    }
                    saveArray(newNotifChannels, NOTIFICATION_CHANNELS, this)

                    prefs.edit().putInt(TIMETABLE_AVAILABLE, 1).apply()
                    prefs.edit().putInt(UPDATE, 0).apply()
                    if (day == "sunday")
                        tellUpdated()
                }
                .addOnFailureListener { e ->
                    Timber.d("Error: $e")
                }
        }
        NotificationHelper.createNotificationChannel(
            this,
            "Other",
            "Miscellaneous Notifications",
        )
    }

    private fun tellUpdated() {
        val updated = hashMapOf(
            "isTimetableAvailable" to true,
            "isUpdated" to false
        )
        db.collection("users")
            .document(uid)
            .set(updated)
            .addOnSuccessListener {
                setAlarm()
                val pm: PowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    Toast.makeText(
                        this,
                        "Please turn off the Battery Optimization Settings for VITTY to receive notifications on time.",
                        Toast.LENGTH_LONG
                    ).show()
                    val pmIntent = Intent()
                    pmIntent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                    startActivity(pmIntent)
                } else {
                    val intent = Intent(this, ScheduleActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Timber.d("Error: $e")
            }
    }

    private fun setAlarm() {
        val intent = Intent(this, AlarmReceiver::class.java)
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)

        val pendingIntent =
            PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        val date = Date().time

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            date,
            (1000 * 60 * NOTIF_DELAY).toLong(),
            pendingIntent
        )
    }

    private fun loadArray(arrayName: String, context: Context): Array<String?> {
        val prefs = context.getSharedPreferences(USER_INFO, Context.MODE_PRIVATE)
        val size = prefs.getInt(arrayName + "_size", 0)
        val array = arrayOfNulls<String>(size)
        for (i in 0 until size) array[i] = prefs.getString(arrayName + "_" + i, null)
        return array
    }

    private fun saveArray(array: ArrayList<String>, arrayName: String, context: Context): Boolean {
        val prefs = context.getSharedPreferences(USER_INFO, 0)
        val editor = prefs.edit()
        editor.putInt(arrayName + "_size", array.size)
        for (i in array.indices) editor.putString(arrayName + "_" + i, array[i])
        return editor.commit()
    }
}
