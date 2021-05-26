package com.dscvit.vitty.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import com.dscvit.vitty.R
import com.dscvit.vitty.adapter.DayAdapter
import com.dscvit.vitty.databinding.ActivityScheduleBinding
import com.dscvit.vitty.util.Constants.TIMETABLE_AVAILABLE
import com.dscvit.vitty.util.Constants.UID
import com.dscvit.vitty.util.Constants.UPDATE
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class ScheduleActivity : FragmentActivity() {

    private lateinit var binding: ActivityScheduleBinding
    private val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    private lateinit var prefs: SharedPreferences
    private var uid = ""
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_schedule)
        prefs = getSharedPreferences("login_info", 0)
        uid = prefs.getString("uid", "").toString()
        pageSetup(this)
    }

    override fun onStart() {
        super.onStart()
        setupOnStart()
    }

    private fun setupOnStart() {
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.getBoolean("isUpdated") == true) {
                    prefs.edit().putInt(TIMETABLE_AVAILABLE, 0).apply()
                    prefs.edit().putInt(UPDATE, 1).apply()
                    val intent = Intent(this, InstructionsActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
    }

    private fun pageSetup(context: Context) {
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

        binding.scheduleToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.logout -> {
                    logout(context)
                    true
                }
                R.id.notifications -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val settingsIntent: Intent =
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                        startActivity(settingsIntent)
                    } else {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        val uri: Uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    true
                }
                R.id.battery -> {
                    val pm: PowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                    if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                        Toast.makeText(
                            this,
                            "Please turn off the Battery Optimization Settings for VITTY to receive notifications on time.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Please keep the Battery Optimization Settings for VITTY turned off to receive notifications on time.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    val pmIntent = Intent()
                    pmIntent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                    startActivity(pmIntent)
                    true
                }
                R.id.share -> {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                    }
                    shareIntent.putExtra(
                        Intent.EXTRA_TEXT,
                        getString(R.string.share_text)
                    )
                    startActivity(Intent.createChooser(shareIntent, "Share"))
                    true
                }
                else -> false
            }
        }
        val pagerAdapter = DayAdapter(this)
        binding.pager.adapter = pagerAdapter
        TabLayoutMediator(
            binding.tabs, binding.pager
        ) { tab, position -> tab.text = days[position] }
            .attach()

        binding.pager.currentItem = d
    }

    private fun logout(context: Context) {
        val v: View = LayoutInflater
            .from(context)
            .inflate(R.layout.dialog_logout, null)

        val dialog = MaterialAlertDialogBuilder(context)
            .setView(v)
            .setBackground(
                AppCompatResources.getDrawable(
                    context,
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
