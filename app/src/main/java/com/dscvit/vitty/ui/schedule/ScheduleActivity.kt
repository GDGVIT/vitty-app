package com.dscvit.vitty.ui.schedule

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.dscvit.vitty.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ScheduleActivity : FragmentActivity() {

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = findViewById(R.id.pager)
        tabLayout = findViewById(R.id.tabs)

        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = DayAdapter(this)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(
                tabLayout, viewPager
        ) { tab, position -> tab.text = days[position] }.attach()
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }


}