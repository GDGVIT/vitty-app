package com.dscvit.vitty.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.dscvit.vitty.BuildConfig
import com.dscvit.vitty.R
import com.dscvit.vitty.databinding.ActivitySettingsBinding
import com.dscvit.vitty.ui.settings.SettingsFragment
import com.dscvit.vitty.util.RemoteConfigUtils

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
        setupDetails()
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

    private fun setupDetails() {
        var details =
            "College Mode: ${RemoteConfigUtils.getOnlineModeDetails()}\nApp Version: ${BuildConfig.VERSION_NAME}"
        if (RemoteConfigUtils.getLatestVersion() > BuildConfig.VERSION_CODE)
            details += " (Update Available)"
        binding.settingDetails.text = details
    }
}
