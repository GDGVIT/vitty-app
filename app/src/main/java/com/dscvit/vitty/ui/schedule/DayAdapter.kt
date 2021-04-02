package com.dscvit.vitty.ui.schedule

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter


/**
 * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
 * sequence.
 */
class DayAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    private val numPages = 7

    override fun getItemCount(): Int = numPages

    override fun createFragment(position: Int): Fragment {
        val bundle = Bundle()
        bundle.putString("frag_id", position.toString())
        val fragment = DayFragment()
        fragment.arguments = bundle
        return fragment
    }
}