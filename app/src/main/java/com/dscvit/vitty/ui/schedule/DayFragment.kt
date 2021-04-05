package com.dscvit.vitty.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dscvit.vitty.R
import kotlin.random.Random

class DayFragment : Fragment() {

//    private val days =
//        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    private val courseList: ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_day, container, false)
        val fragID = arguments!!.getString("frag_id")?.toInt()

        if (fragID != null) {
            for (i in (6 - fragID) downTo 2) {
                courseList.add(getRandomString(Random.nextInt(8) + 5))
            }
        }

        if (courseList.isNotEmpty()) {
            val recyclerView: RecyclerView = root.findViewById(R.id.day_list)
            recyclerView.adapter = PeriodAdapter(courseList)
            recyclerView.layoutManager = LinearLayoutManager(context)
        } else {
            val noPeriod = root.findViewById<RelativeLayout>(R.id.no_period)
            noPeriod.visibility = View.VISIBLE
        }

        return root
    }

    private fun getRandomString(length: Int): String {
        val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        return List(length) { charset.random() }
            .joinToString("")
    }
}
