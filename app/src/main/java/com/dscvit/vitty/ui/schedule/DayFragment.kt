package com.dscvit.vitty.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dscvit.vitty.R

class DayFragment : Fragment() {

    private val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_day, container, false)
        val fragID = arguments!!.getString("frag_id")?.toInt()
        val textView = root.findViewById<TextView>(R.id.textView)
        textView.text = days[fragID!!]
        return root
    }
}