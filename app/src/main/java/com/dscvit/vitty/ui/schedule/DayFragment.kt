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
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

class DayFragment : Fragment() {

    private lateinit var binding: FragmentDayBinding
    private val courseList: ArrayList<PeriodDetails> = ArrayList()
    private var fragID = -1
    private lateinit var sharedPref: SharedPreferences
    private val db = FirebaseFirestore.getInstance()
    private val days =
        listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")

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
        sharedPref = activity?.getSharedPreferences("login_info", Context.MODE_PRIVATE)!!
        val uid = sharedPref.getString("uid", "")
        if (uid != null) {
            db.collection("users")
                .document(uid)
                .collection("timetable")
                .document(days[fragID])
                .collection("periods")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        try {
                            val pd = PeriodDetails(
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
            } else {
                noPeriod.visibility = View.VISIBLE
            }
        }
    }
}
