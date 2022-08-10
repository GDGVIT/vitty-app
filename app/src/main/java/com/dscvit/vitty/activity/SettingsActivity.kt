package com.dscvit.vitty.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.dscvit.vitty.BuildConfig
import com.dscvit.vitty.R
import com.dscvit.vitty.databinding.ActivitySettingsBinding
import com.dscvit.vitty.ui.settings.SettingsFragment
import com.dscvit.vitty.util.Constants.VITTY_APP_URL
import com.dscvit.vitty.util.RemoteConfigUtils
import com.dscvit.vitty.util.Report

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
            "Version ${BuildConfig.VERSION_NAME}"
        if (RemoteConfigUtils.getLatestVersion() > BuildConfig.VERSION_CODE)
            details += " (Update Available)"
//        details += "\n\n[Long press to copy app details]"
        binding.settingDetails.apply {
            text = details
            setOnClickListener {
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(VITTY_APP_URL)
                        )
                    )
                } catch (e: Exception) {
                    Toast.makeText(context, "Browser not found!", Toast.LENGTH_LONG).show()
                }
            }
            setOnLongClickListener {
                val versionDetails = Report.details(context)
                Toast.makeText(
                    context,
                    "App Details Copied",
                    Toast.LENGTH_SHORT
                ).show()
                val clipboard: ClipboardManager? =
                    context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
                val clip = ClipData.newPlainText(
                    "APP-VERSION",
                    versionDetails
                )
                clipboard?.setPrimaryClip(clip)
                true
            }
        }
    }
}
