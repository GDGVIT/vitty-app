package com.dscvit.vitty.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.dscvit.vitty.R
import com.dscvit.vitty.databinding.CardPeriodBinding
import com.dscvit.vitty.model.PeriodDetails
import com.dscvit.vitty.util.VITMap
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PeriodAdapter(private val dataSet: ArrayList<PeriodDetails>, private val day: Int) :
    RecyclerView.Adapter<PeriodAdapter.ViewHolder>() {

    private var previousExpandedPosition = -1
    private var mExpandedPosition = -1
    private var active = -1

    class ViewHolder(private val binding: CardPeriodBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val arrow = binding.arrowMoreInfo
        val moreInfo = binding.moreInfo
        val expandedBackground = binding.expandedBackground
        val activePeriod = binding.activePeriod
        val periodTime = binding.periodTime
        val classNav = binding.classNav
        fun bind(data: PeriodDetails) {
            binding.periodDetails = data
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.card_period,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataSet[position]
        holder.bind(item)

        val startTime: Date = item.startTime.toDate()
        val simpleDateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val sTime: String = simpleDateFormat.format(startTime).uppercase(Locale.ROOT)

        val endTime: Date = item.endTime.toDate()
        val eTime: String = simpleDateFormat.format(endTime).uppercase(Locale.ROOT)

        val now = Calendar.getInstance()
        val s = Calendar.getInstance()
        s.time = startTime
        val start = Calendar.getInstance()
        start[Calendar.HOUR_OF_DAY] = s[Calendar.HOUR_OF_DAY]
        start[Calendar.MINUTE] = s[Calendar.MINUTE]
        val e = Calendar.getInstance()
        e.time = endTime
        val end = Calendar.getInstance()
        end[Calendar.HOUR_OF_DAY] = e[Calendar.HOUR_OF_DAY]
        end[Calendar.MINUTE] = e[Calendar.MINUTE]

        holder.apply {
            periodTime.text = "$sTime - $eTime"
            activePeriod.visibility = View.INVISIBLE
            classNav.setOnClickListener {
                VITMap.openClassMap(classNav.context, item.roomNo)
            }
        }

        if ((((day + 1) % 7) + 1) == now[Calendar.DAY_OF_WEEK]) {
            if ((start.before(now) && end.after(now)) || start.equals(now) || (start.after(now) && active == -1) || active == position) {
                holder.activePeriod.visibility = View.VISIBLE
                active = position
            }
        }

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

    override fun getItemCount() = dataSet.size
}
