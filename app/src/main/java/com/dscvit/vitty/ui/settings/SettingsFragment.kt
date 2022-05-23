package com.dscvit.vitty.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.dscvit.vitty.R
import com.dscvit.vitty.util.Constants
import com.dscvit.vitty.util.LogoutHelper
import com.dscvit.vitty.util.NotificationHelper
import com.dscvit.vitty.util.UtilFunctions.getSatModeCode
import com.dscvit.vitty.util.UtilFunctions.reloadWidgets

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

    private fun setupNotifications() {
        val prefs = requireContext().getSharedPreferences(Constants.USER_INFO, 0)
        val examMode: SwitchPreferenceCompat? = findPreference(Constants.EXAM_MODE)
        if (examMode != null) {
            examMode.isChecked = prefs.getBoolean(Constants.EXAM_MODE, false)
        }
        examMode?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue.toString() == "true") {
                NotificationHelper.cancelAlarm(requireContext())
                prefs.edit().putBoolean(Constants.EXAM_MODE, true).apply()
            } else {
                NotificationHelper.setAlarm(requireContext())
                prefs.edit().putBoolean(Constants.EXAM_MODE, false).apply()
            }
            reloadWidgets(requireContext())
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

    private fun setupEffects() {
        val examMode: SwitchPreferenceCompat? = findPreference(Constants.VIBRATION_MODE)
        examMode?.setOnPreferenceChangeListener { _, newValue ->
            val prefs = requireContext().getSharedPreferences(Constants.USER_INFO, 0)
            if (newValue.toString() == "true") {
                prefs.edit().putBoolean(Constants.VIBRATION_MODE, true).apply()
            } else {
                prefs.edit().putBoolean(Constants.VIBRATION_MODE, false).apply()
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
//        val cat: PreferenceCategory? = findPreference("account_title")
//        cat?.title = prefs.getString("sign_in_method", "Google") + " Account Details"
        val account: Preference? = findPreference("account")
        account?.setOnPreferenceClickListener {
            LogoutHelper.logout(requireContext(), requireActivity(), prefs)
            true
        }
    }

    private fun setupAbout() {
        openWebsite(Constants.GDSCVIT_TAG, Constants.GDSCVIT_WEBSITE)
        openWebsite(Constants.GITHUB_REPO, Constants.GITHUB_REPO_LINK)
        openWebsite(Constants.CHANGE_TIMETABLE, Constants.VITTY_URL)
    }

    private fun setupClass() {
        val prefs = requireContext().getSharedPreferences(Constants.USER_INFO, 0)
        val satClass: ListPreference? = findPreference("sat_mode")
        satClass?.value = prefs.getString(getSatModeCode(), "saturday")
        satClass?.setOnPreferenceChangeListener { _, newValue ->
            prefs.edit().putString(getSatModeCode(), newValue.toString()).apply()
            reloadWidgets(requireContext())
            true
        }
    }

    private fun setupRefreshWidgets() {
        val pref: Preference? = findPreference("refresh_widgets")
        pref?.setOnPreferenceClickListener {
            reloadWidgets(requireContext())
//            Toast.makeText(context, "Refreshed!", Toast.LENGTH_LONG).show()
            pref.isEnabled = false
            pref.isSelectable = false
            pref.summary = "Refreshed!"
            true
        }
    }

    private fun setupPreferences() {
        setupAccountDetails()
        setupClass()
        setupNotifications()
//        setupEffects()
        setupBattery()
        setupRefreshWidgets()
        setupAbout()
    }
}
