package com.dscvit.vitty.ui.schedule

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dscvit.vitty.R
import com.dscvit.vitty.adapter.PeriodAdapter
import com.dscvit.vitty.databinding.FragmentDayBinding
import com.dscvit.vitty.model.PeriodDetails
import com.dscvit.vitty.util.Constants.DEFAULT_QUOTE
import com.dscvit.vitty.util.Constants.USER_INFO
import com.dscvit.vitty.util.Quote
import com.dscvit.vitty.util.UtilFunctions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import timber.log.Timber

class DayFragment : Fragment() {

    private lateinit var binding: FragmentDayBinding
    private val courseList: ArrayList<PeriodDetails> = ArrayList()
    private var fragID = -1
    private lateinit var sharedPref: SharedPreferences
    private val db = FirebaseFirestore.getInstance()
    private val days =
        listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
    lateinit var day: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_day,
            container,
            false
        )
        fragID = requireArguments().getString("frag_id")?.toInt()!!
        getData()
        return binding.root
    }

    private fun getData() {
        sharedPref = activity?.getSharedPreferences(USER_INFO, Context.MODE_PRIVATE)!!
        courseList.clear()
        val uid = sharedPref.getString("uid", "")
        day = if (days[fragID] == "saturday") sharedPref.getString(
            UtilFunctions.getSatModeCode(),
            "saturday"
        ).toString() else days[fragID]
        if (uid != null) {
            db.collection("users")
                .document(uid)
                .collection("timetable")
                .document(day)
                .collection("periods")
                .get(Source.CACHE)
                .addOnSuccessListener { result ->
                    for (document in result) {
                        try {
                            val pd = PeriodDetails(
                                document.getString("courseCode")!!,
                                document.getString("courseName")!!,
                                document.getTimestamp("startTime")!!,
                                document.getTimestamp("endTime")!!,
                                document.getString("slot")!!,
                                document.getString("location")!!
                            )
                            courseList.add(pd)
                        } catch (e: Exception) {
                        }
                    }
                    scheduleSetup()
                }
                .addOnFailureListener { e ->
                    Timber.d("Auth error: $e")
                }
        }
    }

    private fun scheduleSetup() {
        binding.apply {
            if (courseList.isNotEmpty()) {
                dayList.scheduleLayoutAnimation()
                dayList.adapter = PeriodAdapter(courseList, fragID)
                dayList.layoutManager = LinearLayoutManager(context)
                noPeriod.visibility = View.INVISIBLE
            } else {
                binding.quoteLine.text = try {
                    Quote.getLine(context)
                } catch (_: Exception) {
                    DEFAULT_QUOTE
                }
                noPeriod.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (days[fragID] == "saturday" && day != sharedPref.getString(
                UtilFunctions.getSatModeCode(),
                "saturday"
            ).toString()
        ) {
            getData()
        }
    }
}
