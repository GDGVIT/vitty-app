package com.dscvit.vitty.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.dscvit.vitty.R
import com.dscvit.vitty.databinding.ActivityInstructionsBinding
import com.dscvit.vitty.notif.NotificationHelper
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

class InstructionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInstructionsBinding
    private val days =
        listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_instructions)
        binding.doneButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannels()
            } else {
                val intent = Intent(this, ScheduleActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun createNotificationChannels() {
        val notifChannels = loadArray("notif_channels", this)
        for (notifChannel in notifChannels) {
            if (notifChannel != null) {
                NotificationHelper.deleteNotificationChannel(this, notifChannel.toString())
            }
        }
        val newNotifChannels: ArrayList<String> = ArrayList()
        val db = FirebaseFirestore.getInstance()
        val sharedPref = getSharedPreferences("login_info", Context.MODE_PRIVATE)!!
        val uid = sharedPref.getString("uid", "")
        if (uid != null) {
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
                        saveArray(newNotifChannels, "notif_channels", this)
                        val intent = Intent(this, ScheduleActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Timber.d("Error: $e")
                    }
            }
        }
        NotificationHelper.createNotificationChannel(
            this,
            "Other",
            "Miscellaneous Notifications",
        )
    }

    private fun loadArray(arrayName: String, context: Context): Array<String?> {
        val prefs = context.getSharedPreferences("login_info", Context.MODE_PRIVATE)
        val size = prefs.getInt(arrayName + "_size", 0)
        val array = arrayOfNulls<String>(size)
        for (i in 0 until size) array[i] = prefs.getString(arrayName + "_" + i, null)
        return array
    }

    private fun saveArray(array: ArrayList<String>, arrayName: String, context: Context): Boolean {
        val prefs = context.getSharedPreferences("login_info", 0)
        val editor = prefs.edit()
        editor.putInt(arrayName + "_size", array.size)
        for (i in array.indices) editor.putString(arrayName + "_" + i, array[i])
        return editor.commit()
    }
}
