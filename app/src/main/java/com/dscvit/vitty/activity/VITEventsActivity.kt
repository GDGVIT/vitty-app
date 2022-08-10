package com.dscvit.vitty.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.dscvit.vitty.R
import com.dscvit.vitty.adapter.EventAdapter
import com.dscvit.vitty.databinding.ActivityViteventsBinding
import com.dscvit.vitty.model.Event
import com.dscvit.vitty.ui.events.VITEventsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VITEventsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViteventsBinding
    private lateinit var vitEventsViewModel: VITEventsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vitevents)
        vitEventsViewModel = ViewModelProvider(this)[VITEventsViewModel::class.java]
        vitEventsViewModel.events.observe(this) {
            binding.loadingSign.visibility = View.GONE
            if (it != null) {
                setRecycleViewList(it.Events)
            } else {
                binding.noEvents.apply {
                    text = "No internet available"
                    visibility = View.VISIBLE
                }
            }
        }
        binding.eventsToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.close -> {
                    onBackPressed()
                    true
                }
                else -> false
            }
        }
    }

    private fun setRecycleViewList(events: List<Event>) {
        if (events.isEmpty()) {
            binding.noEvents.visibility = View.VISIBLE
            return
        }
        binding.noEvents.visibility = View.GONE
        val sortedEvents: List<Event> = events.sortedBy {
            val formatter = SimpleDateFormat("hh:mm", Locale.getDefault())
            val date: Date = formatter.parse(it.Time) as Date
            date.time
        }
        val mAdapter = EventAdapter(sortedEvents)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        binding.eventsRecyclerView.apply {
            layoutManager = mLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = mAdapter
        }
    }
}
