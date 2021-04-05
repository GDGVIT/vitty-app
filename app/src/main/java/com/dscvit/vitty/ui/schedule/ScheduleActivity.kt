package com.dscvit.vitty.ui.schedule

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.dscvit.vitty.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ScheduleActivity : FragmentActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        viewPager = findViewById(R.id.pager)
        tabLayout = findViewById(R.id.tabs)

        val pagerAdapter = DayAdapter(this)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(
            tabLayout, viewPager
        ) { tab, position -> tab.text = days[position] }.attach()
    }

//    override fun onBackPressed() {
//        if (viewPager.currentItem == 0) {
//            super.onBackPressed()
//        } else {
//            viewPager.currentItem = viewPager.currentItem - 1
//        }
//    }
}
