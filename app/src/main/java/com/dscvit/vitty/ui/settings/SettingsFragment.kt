package com.dscvit.vitty.ui.settings

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
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.dscvit.vitty.R
import com.dscvit.vitty.notif.AlarmReceiver
import com.dscvit.vitty.util.Constants
import com.dscvit.vitty.util.LogoutHelper
import java.util.Date

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
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            Constants.ALARM_INTENT,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager =
            requireContext().getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
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
        val pendingIntent = PendingIntent.getBroadcast(
            context, Constants.ALARM_INTENT, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun setupNotifications() {
        val examMode: SwitchPreferenceCompat? = findPreference(Constants.EXAM_MODE)
        examMode?.setOnPreferenceChangeListener { _, newValue ->
            val prefs = requireContext().getSharedPreferences(Constants.USER_INFO, 0)
            if (newValue.toString() == "true") {
                cancelAlarm()
                prefs.edit().putBoolean(Constants.EXAM_MODE, true).apply()
            } else {
                setAlarm()
                prefs.edit().putBoolean(Constants.EXAM_MODE, false).apply()
            }
            true
        }
        val individualNotification: Preference? = findPreference(Constants.IND_NOTIF)
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
        val batteryOptimization: Preference? = findPreference(Constants.BATTERY_OPTIM)
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
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(website)
                    )
                )
                true
            } catch (e: Exception) {
                Toast.makeText(context, "Browser not found!", Toast.LENGTH_LONG).show()
                false
            }
        }
    }

    private fun setupAccountDetails() {
        val prefs = requireContext().getSharedPreferences(Constants.USER_INFO, 0)
        val cat: PreferenceCategory? = findPreference("account_title")
        cat?.title = prefs.getString("sign_in_method", "Google") + " Account Details"

        val account: Preference? = findPreference("account")
        account?.setOnPreferenceClickListener {
            LogoutHelper.logout(requireContext(), requireActivity(), prefs)
            true
        }
    }

    private fun setupAbout() {
        openWebsite(Constants.GITHUB_REPO, Constants.GITHUB_REPO_LINK)
        openWebsite(Constants.GDSCVIT_TAG, Constants.GDSCVIT_WEBSITE)
    }

    private fun setupPreferences() {
        setupAccountDetails()
        setupNotifications()
        setupBattery()
        setupAbout()
    }
}
