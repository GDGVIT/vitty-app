package com.dscvit.vitty.ui.schedule

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dscvit.vitty.BuildConfig
import com.dscvit.vitty.R
import com.dscvit.vitty.activity.InstructionsActivity
import com.dscvit.vitty.activity.SettingsActivity
import com.dscvit.vitty.activity.VITEventsActivity
import com.dscvit.vitty.adapter.DayAdapter
import com.dscvit.vitty.databinding.FragmentScheduleBinding
import com.dscvit.vitty.receiver.ShareReceiver
import com.dscvit.vitty.util.Constants
import com.dscvit.vitty.util.LogoutHelper
import com.dscvit.vitty.util.RemoteConfigUtils
import com.dscvit.vitty.util.UtilFunctions
import com.dscvit.vitty.util.VITMap
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    private lateinit var prefs: SharedPreferences
    private var uid = ""
    private val db = FirebaseFirestore.getInstance()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val scheduleViewModel =
            ViewModelProvider(this)[ScheduleViewModel::class.java]

        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        val root: View = binding.root

        prefs = requireContext().getSharedPreferences(Constants.USER_INFO, 0)
        uid = prefs.getString("uid", "").toString()
        val classLocation = prefs.getString("openClassId", "").toString()
        if (classLocation != "") {
            prefs.edit().putString("openClassId", "").apply()
            VITMap.openClassMap(requireContext(), classLocation)
        }
        pageSetup()
        firstTimeSetup()

        return root
    }


    private fun checkExamMode() {
        if (!prefs.getBoolean(Constants.EXAM_MODE, false)) {
            binding.examModeAlert.apply {
                visibility = View.INVISIBLE
                layoutParams =
                    RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 0).apply {
                        addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    }
            }
//            window.navigationBarColor = getColor(R.color.background)
            return
        }
//        window.navigationBarColor = getColor(R.color.tab_back)
        binding.examModeAlert.apply {
            visibility = View.VISIBLE
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }
            setOnClickListener {
                startActivity(Intent(context, SettingsActivity::class.java))
            }
        }
        binding.examModeAlertIcon.setColorFilter(requireContext().getColor(R.color.translucent))
    }

    private fun setupOnStart() {
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (UtilFunctions.isUpdated(document, prefs)) {
                    prefs.edit().putInt(Constants.TIMETABLE_AVAILABLE, 0).apply()
                    prefs.edit().putInt(Constants.UPDATE, 1).apply()
                    val intent = Intent(context, InstructionsActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
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

        binding.VITEventsButton.setOnClickListener {
            val intent = Intent(context, VITEventsActivity::class.java)
            startActivity(intent)
        }

//        binding.shareTimeTableButton.setOnClickListener {
//            Toast.makeText(context, "Share Timetable Alpha", Toast.LENGTH_LONG).show()
//            val rootView = window.decorView.findViewById<View>(R.id.pager)
//            rootView.setBackgroundColor(getColor(R.color.background))
//            UtilFunctions.takeScreenshotAndShare(this, UtilFunctions.getBitmapFromView(rootView))
//        }

        binding.scheduleToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.logout -> {
                    LogoutHelper.logout(requireContext(), requireContext() as Activity, prefs)
                    true
                }
                R.id.settings -> {
                    startActivity(Intent(context, SettingsActivity::class.java))
                    true
                }
                R.id.support -> {
                    UtilFunctions.openLink(requireContext(), getString(R.string.telegram_link))
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
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        Constants.SHARE_INTENT,
                        Intent(context, ShareReceiver::class.java),
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                        else
                            PendingIntent.FLAG_UPDATE_CURRENT
                    )
//                    val broadcastReceiver: BroadcastReceiver = ShareReceiver()
//                    registerReceiver(broadcastReceiver, IntentFilter(SHARE_ACTION))
                    startActivity(
                        Intent.createChooser(
                            shareIntent,
                            null,
                            pendingIntent.intentSender
                        )
                    )
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
        val max = 6
        val upCode = prefs.getInt(Constants.UPDATE_CODE, 0)
        if (!prefs.getBoolean(
                Constants.FIRST_TIME_SETUP,
                false
            ) || upCode != BuildConfig.VERSION_CODE
        ) {
            var count = 1
            val v: View = LayoutInflater
                .from(context)
                .inflate(R.layout.dialog_setup_complete, null)
            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setView(v)
                .setBackground(
                    AppCompatResources.getDrawable(
                        requireContext(),
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

            if (prefs.getBoolean(
                    Constants.FIRST_TIME_SETUP,
                    false
                ) && upCode < BuildConfig.VERSION_CODE
            ) {
                val msg = introMessage(max - 1)
                title.text = msg[0]
                desc.text = msg[1]
                count = max + 1
                skip.visibility = View.GONE
                next.text = getString(R.string.done)
            }

            skip.setOnClickListener {
                prefs.edit {
                    putBoolean(Constants.FIRST_TIME_SETUP, true)
                    putInt(Constants.UPDATE_CODE, BuildConfig.VERSION_CODE)
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
                        putBoolean(Constants.FIRST_TIME_SETUP, true)
                        putInt(Constants.UPDATE_CODE, BuildConfig.VERSION_CODE)
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

    override fun onStart() {
        super.onStart()
        setupOnStart()
    }

    override fun onResume() {
        super.onResume()
        checkExamMode()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}