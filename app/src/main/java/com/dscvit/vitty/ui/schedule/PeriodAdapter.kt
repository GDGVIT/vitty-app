package com.dscvit.vitty.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dscvit.vitty.R

class PeriodAdapter(private val dataset: ArrayList<String>) :
    RecyclerView.Adapter<PeriodAdapter.ViewHolder>() {

    private var previousExpandedPosition = -1
    private var mExpandedPosition = -1

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val timePeriod: TextView = view.findViewById(R.id.period_time)
        val courseName: TextView = view.findViewById(R.id.course_name)
        val arrow: ImageView = view.findViewById(R.id.arrow_faq)
        val moreInfo: LinearLayout = view.findViewById(R.id.more_info)
        val expandedBackground: ImageView = view.findViewById(R.id.expanded_background)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_period, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.courseName.text = dataset[position]

        val isExpanded = position == mExpandedPosition
        if (isExpanded) {
            holder.expandedBackground.visibility = View.VISIBLE
            holder.moreInfo.visibility = View.VISIBLE
            holder.arrow.rotation = 180F
        } else {
            holder.moreInfo.visibility = View.GONE
            holder.expandedBackground.visibility = View.GONE
            holder.arrow.rotation = 0F
        }
        holder.itemView.isActivated = isExpanded

        if (isExpanded) previousExpandedPosition = position

        holder.itemView.setOnClickListener {
            mExpandedPosition = if (isExpanded) -1 else position
            notifyItemChanged(previousExpandedPosition)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = dataset.size
}
