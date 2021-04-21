package com.dscvit.vitty.ui.schedule

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
import kotlin.random.Random

class DayFragment : Fragment() {

    private lateinit var binding: FragmentDayBinding
    private val courseList: ArrayList<PeriodDetails> = ArrayList()
    private var fragID = -1

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
        fragID = arguments!!.getString("frag_id")?.toInt()!!
        generateDummyTimetable(fragID)
        scheduleSetup()
        return binding.root
    }

    private fun generateDummyTimetable(fragID: Int) {
        if (fragID != -1) {
            for (i in (6 - fragID) downTo 2) {
                val c = getRandomString(Random.nextInt(8) + 5)
                val s = "A1 + TA1"
                val t = "6:00 AM - 9:00 PM"
                val r = "ACB 10"
                courseList.add(PeriodDetails(c, t, s, r))
            }
        }
    }

    private fun scheduleSetup() {
        binding.apply {
            if (courseList.isNotEmpty()) {
                dayList.adapter = PeriodAdapter(courseList)
                dayList.layoutManager = LinearLayoutManager(context)
            } else {
                noPeriod.visibility = View.VISIBLE
            }
        }
    }

    private fun getRandomString(length: Int): String {
        val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        return List(length) { charset.random() }
            .joinToString("")
    }
}
