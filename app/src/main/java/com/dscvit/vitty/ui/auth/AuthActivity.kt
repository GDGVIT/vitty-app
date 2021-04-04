package com.dscvit.vitty.ui.auth

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.dscvit.vitty.R
import com.dscvit.vitty.ui.instructions.InstructionsActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class AuthActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var nextButton: Button
    private lateinit var loginButton: ImageButton
    private val pages = listOf("○", "○", "○")
    private var loginClick = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        viewPager = findViewById(R.id.intro_pager)
        tabLayout = findViewById(R.id.intro_tabs)
        nextButton = findViewById(R.id.next_button)
        loginButton = findViewById(R.id.google_login)

        val pagerAdapter = IntroAdapter(this)
        viewPager.adapter = pagerAdapter

        val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == 0 || position == 1) {
                    loginButton.visibility = View.GONE
                    nextButton.visibility = View.VISIBLE
                } else {
                    nextButton.visibility = View.GONE
                    loginButton.visibility = View.VISIBLE
                }
            }
        }

        viewPager.registerOnPageChangeCallback(pageChangeCallback)

        TabLayoutMediator(
            tabLayout, viewPager
        ) { tab, position -> tab.text = pages[position] }.attach()

        nextButton.setOnClickListener {
            if (viewPager.currentItem != pages.size - 1)
                viewPager.currentItem++
        }

        loginButton.setOnClickListener {
            loginClick = true
            viewPager.isUserInputEnabled = false
            viewPager.unregisterOnPageChangeCallback(pageChangeCallback)

            val message = "Yajat Malhotra"
            val intent = Intent(this, InstructionsActivity::class.java).apply {
                putExtra(EXTRA_MESSAGE, message)
            }
            startActivity(intent)
            finish()
        }

    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0 || loginClick) {
            super.onBackPressed()
        } else {
            viewPager.currentItem--
        }
    }
}
