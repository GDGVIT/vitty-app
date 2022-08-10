package com.dscvit.vitty.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dscvit.vitty.R
import com.dscvit.vitty.model.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventAdapter(private val dataSet: List<Event>) :
    RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventName: TextView = view.findViewById(R.id.event_name)
        val eventTime: TextView = view.findViewById(R.id.event_time)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.card_event, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val date: Date = SimpleDateFormat("hh:mm z", Locale.getDefault()).parse("${dataSet[position].Time} IST") as Date
        val time: String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(date).uppercase(Locale.ROOT)
        viewHolder.eventName.text = dataSet[position].EventName
        viewHolder.eventTime.text = time
    }

    override fun getItemCount() = dataSet.size
}
