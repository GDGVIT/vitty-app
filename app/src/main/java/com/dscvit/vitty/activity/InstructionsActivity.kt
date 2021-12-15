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
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import com.dscvit.vitty.BuildConfig
import com.dscvit.vitty.R
import com.dscvit.vitty.databinding.ActivityInstructionsBinding
import com.dscvit.vitty.notif.AlarmReceiver
import com.dscvit.vitty.notif.NotificationHelper
import com.dscvit.vitty.util.Constants.ALARM_INTENT
import com.dscvit.vitty.util.Constants.EXAM_MODE
import com.dscvit.vitty.util.Constants.GROUP_ID
import com.dscvit.vitty.util.Constants.GROUP_ID_2
import com.dscvit.vitty.util.Constants.NOTIFICATION_CHANNELS
import com.dscvit.vitty.util.Constants.NOTIF_DELAY
import com.dscvit.vitty.util.Constants.TIMETABLE_AVAILABLE
import com.dscvit.vitty.util.Constants.UID
import com.dscvit.vitty.util.Constants.UPDATE
import com.dscvit.vitty.util.Constants.USER_INFO
import com.dscvit.vitty.util.Constants.VERSION_CODE
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
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

        setupToolbar()
        setGDSCVITChannel()

        binding.doneButton.setOnClickListener {
            setupDoneButton()
        }
    }

    override fun onStart() {
        super.onStart()
        if (prefs.getInt(UPDATE, 0) == 1) {
            createNotificationChannels()
            Toast.makeText(this, getString(R.string.updated), Toast.LENGTH_SHORT)
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
        setNotificationGroup()
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
                            GROUP_ID
                        )
                        newNotifChannels.add(cn)
                        Timber.d(cn)
                    }
                    saveArray(newNotifChannels, NOTIFICATION_CHANNELS, this)

                    if (day == "sunday")
                        tellUpdated()
                }
                .addOnFailureListener { e ->
                    Timber.d("Error: $e")
                }
        }
    }

    private fun tellUpdated() {
        prefs.edit().putInt(TIMETABLE_AVAILABLE, 1).apply()
        prefs.edit().putInt(UPDATE, 0).apply()
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

    private fun setGDSCVITChannel() {
        if (!prefs.getBoolean("gdscvitChannelCreated", false)) {
            NotificationHelper.createNotificationGroup(
                this,
                getString(R.string.gdscvit),
                GROUP_ID_2
            )
            NotificationHelper.createNotificationChannel(
                this,
                getString(R.string.default_notification_channel_name),
                "Notifications from GDSC VIT",
                GROUP_ID_2
            )
            prefs.edit {
                putBoolean("gdscvitChannelCreated", true)
                apply()
            }
        }
    }

    private fun setNotificationGroup() {
        if (!prefs.getBoolean("groupCreated", false)) {
            NotificationHelper.createNotificationGroup(
                this,
                getString(R.string.notif_group),
                GROUP_ID
            )
            prefs.edit {
                putBoolean("groupCreated", true)
                apply()
            }
        }
    }

    private fun setAlarm() {
        if (!prefs.getBoolean(EXAM_MODE, false)) {
            if (prefs.getInt(VERSION_CODE, 0) != BuildConfig.VERSION_CODE) {
                val intent = Intent(this, AlarmReceiver::class.java)

                val pendingIntent =
                    PendingIntent.getBroadcast(
                        this, ALARM_INTENT, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

                val date = Date().time

                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    date,
                    (1000 * 60 * NOTIF_DELAY).toLong(),
                    pendingIntent
                )

                prefs.edit {
                    putInt(VERSION_CODE, BuildConfig.VERSION_CODE)
                    apply()
                }
            }
        }
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

    private fun setupToolbar() {
        binding.instructionsToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.logout -> {
                    logout()
                    true
                }
                else -> false
            }
        }
    }

    private fun logout() {
        val v: View = LayoutInflater
            .from(this)
            .inflate(R.layout.dialog_logout, null)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(v)
            .setBackground(
                AppCompatResources.getDrawable(
                    this,
                    R.color.transparent
                )
            )
            .create()

        dialog.show()

        val cancel = v.findViewById<Button>(R.id.cancel)
        val logout = v.findViewById<Button>(R.id.logout)

        cancel.setOnClickListener {
            dialog.dismiss()
        }

        logout.setOnClickListener {
            prefs.edit().putInt(TIMETABLE_AVAILABLE, 0).apply()
            prefs.edit().putInt(UPDATE, 0).apply()
            prefs.edit().putString(UID, "").apply()
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
