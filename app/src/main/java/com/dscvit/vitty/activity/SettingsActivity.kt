package com.dscvit.vitty.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.dscvit.vitty.R
import com.dscvit.vitty.databinding.ActivitySettingsBinding
import com.dscvit.vitty.notif.AlarmReceiver
import com.dscvit.vitty.util.Constants
import com.dscvit.vitty.util.Constants.BATTERY_OPTIM
import com.dscvit.vitty.util.Constants.EXAM_MODE
import com.dscvit.vitty.util.Constants.GDSCVIT_TAG
import com.dscvit.vitty.util.Constants.GDSCVIT_WEBSITE
import com.dscvit.vitty.util.Constants.GITHUB_REPO
import com.dscvit.vitty.util.Constants.GITHUB_REPO_LINK
import com.dscvit.vitty.util.Constants.IND_NOTIF
import java.util.Date

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupToolbar()
    }

    private fun setupToolbar() {
        binding.settingsToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.close -> {
                    onBackPressed()
                    true
                }
                else -> false
            }
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            setupPreferences()
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val rv = listView
            rv.setPadding(24, 0, 24, 0)
        }

        private fun setAlarm() {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = requireContext().getSystemService(ALARM_SERVICE) as AlarmManager
            val date = Date().time
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                date,
                (1000 * 60 * Constants.NOTIF_DELAY).toLong(),
                pendingIntent
            )
        }

        private fun cancelAlarm() {
            val alarmManager =
                requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            alarmManager.cancel(pendingIntent)
        }

        private fun setupNotifications() {
            val examMode: SwitchPreferenceCompat? = findPreference(EXAM_MODE)
            examMode?.setOnPreferenceChangeListener { _, newValue ->
                val prefs = requireContext().getSharedPreferences(Constants.USER_INFO, 0)
                if (newValue.toString() == "true") {
                    cancelAlarm()
                    prefs.edit().putBoolean(EXAM_MODE, true).apply()
                } else {
                    setAlarm()
                    prefs.edit().putBoolean(EXAM_MODE, false).apply()
                }
                true
            }
            val individualNotification: Preference? = findPreference(IND_NOTIF)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                individualNotification?.isVisible = false
            }
            individualNotification?.setOnPreferenceClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val settingsIntent: Intent =
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context?.packageName)
                    startActivity(settingsIntent)
                }
                true
            }
        }

        private fun setupBattery() {
            val batteryOptimization: Preference? = findPreference(BATTERY_OPTIM)
            val pm: PowerManager = context?.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (pm.isIgnoringBatteryOptimizations(context?.packageName)) {
                batteryOptimization?.summary =
                    "Keep the battery optimizations turned off to receive notifications on time"
            }

            batteryOptimization?.setOnPreferenceClickListener {
                if (!pm.isIgnoringBatteryOptimizations(context?.packageName)) {
                    Toast.makeText(
                        context,
                        "Please turn off the Battery Optimization Settings for VITTY to receive notifications on time.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Please keep the Battery Optimization Settings for VITTY turned off to receive notifications on time.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                val pmIntent = Intent()
                pmIntent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                startActivity(pmIntent)
                true
            }
        }

        private fun openWebsite(key: String, website: String) {
            val pref: Preference? = findPreference(key)
            pref?.setOnPreferenceClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(website)
                    )
                )
                true
            }
        }

        private fun setupAbout() {
            openWebsite(GITHUB_REPO, GITHUB_REPO_LINK)
            openWebsite(GDSCVIT_TAG, GDSCVIT_WEBSITE)
        }

        private fun setupPreferences() {
            setupNotifications()
            setupBattery()
            setupAbout()
        }
    }
}
