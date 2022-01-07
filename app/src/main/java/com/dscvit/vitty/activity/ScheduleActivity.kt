package com.dscvit.vitty.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import com.dscvit.vitty.BuildConfig
import com.dscvit.vitty.R
import com.dscvit.vitty.adapter.DayAdapter
import com.dscvit.vitty.databinding.ActivityScheduleBinding
import com.dscvit.vitty.util.Constants.EXAM_MODE
import com.dscvit.vitty.util.Constants.FIRST_TIME_SETUP
import com.dscvit.vitty.util.Constants.TIMETABLE_AVAILABLE
import com.dscvit.vitty.util.Constants.UID
import com.dscvit.vitty.util.Constants.UPDATE
import com.dscvit.vitty.util.Constants.UPDATE_CODE
import com.dscvit.vitty.util.Constants.USER_INFO
import com.dscvit.vitty.util.RemoteConfigUtils
import com.dscvit.vitty.util.VITMap
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
        prefs = getSharedPreferences(USER_INFO, 0)
        uid = prefs.getString("uid", "").toString()
        val classLocation = prefs.getString("openClassId", "").toString()
        if (classLocation != "") {
            prefs.edit().putString("openClassId", "").apply()
            VITMap.openClassMap(this, classLocation)
        }
        pageSetup()
        firstTimeSetup()
    }

    override fun onStart() {
        super.onStart()
        setupOnStart()
    }

    override fun onResume() {
        super.onResume()
        checkExamMode()
    }

    private fun checkExamMode() {
        if (!prefs.getBoolean(EXAM_MODE, false)) {
            binding.examModeAlert.visibility = View.GONE
            window.navigationBarColor = getColor(R.color.background)
            return
        }
        window.navigationBarColor = getColor(R.color.tab_back)
        binding.examModeAlert.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                startActivity(Intent(context, SettingsActivity::class.java))
            }
        }
        binding.examModeAlertIcon.setColorFilter(getColor(R.color.translucent))
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

    private fun pageSetup() {
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
                    logout()
                    true
                }
                R.id.settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
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

    private fun firstTimeSetup() {
        var max = 6
        val upCode = prefs.getInt(UPDATE_CODE, 0)
        if (!prefs.getBoolean(FIRST_TIME_SETUP, false) || upCode != BuildConfig.VERSION_CODE) {
            var count = 1
            val v: View = LayoutInflater
                .from(this)
                .inflate(R.layout.dialog_setup_complete, null)
            val dialog = MaterialAlertDialogBuilder(this)
                .setView(v)
                .setBackground(
                    AppCompatResources.getDrawable(
                        this,
                        R.color.transparent
                    )
                )
                .create()
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()

            val skip = v.findViewById<Button>(R.id.skip)
            val next = v.findViewById<Button>(R.id.next)
            val title = v.findViewById<TextView>(R.id.title)
            val desc = v.findViewById<TextView>(R.id.description)

            if (prefs.getBoolean(FIRST_TIME_SETUP, false) && upCode < BuildConfig.VERSION_CODE) {
                val msg = introMessage(max - 1)
                title.text = msg[0]
                desc.text = msg[1]
                count = max + 1
                skip.visibility = View.GONE
                next.text = getString(R.string.done)
            }

            skip.setOnClickListener {
                prefs.edit {
                    putBoolean(FIRST_TIME_SETUP, true)
                    putInt(UPDATE_CODE, BuildConfig.VERSION_CODE)
                    apply()
                }
                dialog.dismiss()
            }

            next.setOnClickListener {
                if (RemoteConfigUtils.getOnlineMode() && count == 4) count++
                val msg = introMessage(count)
                title.text = msg[0]
                desc.text = msg[1]
                if (count == max) {
                    skip.visibility = View.GONE
                    next.text = getString(R.string.done)
                }
                if (count > max) {
                    prefs.edit {
                        putBoolean(FIRST_TIME_SETUP, true)
                        putInt(UPDATE_CODE, BuildConfig.VERSION_CODE)
                        apply()
                    }
                    dialog.dismiss()
                }
                count++
            }
        }
    }

    private fun introMessage(pos: Int): List<String> {
        return when (pos) {
            0 -> listOf(getString(R.string.congratulations), getString(R.string.complete_msg))
            1 -> listOf(getString(R.string.widgets), getString(R.string.about_widgets))
            2 -> listOf(getString(R.string.notifications), getString(R.string.about_notifications))
            3 -> listOf(getString(R.string.battery), getString(R.string.about_battery))
            4 -> listOf(getString(R.string.nav), getString(R.string.about_nav))
            5 -> listOf(getString(R.string.new_updates), getString(R.string.about_new_updates))
            else -> listOf(getString(R.string.final_heading), getString(R.string.about_final))
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
            prefs.edit().apply {
                putInt(TIMETABLE_AVAILABLE, 0)
                putInt(UPDATE, 0)
                putString(UID, "")
                putBoolean(FIRST_TIME_SETUP, false)
                apply()
            }
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
