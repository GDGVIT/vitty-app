package com.dscvit.vitty.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.dscvit.vitty.R
import com.dscvit.vitty.databinding.CardPeriodBinding
import com.dscvit.vitty.model.PeriodDetails
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PeriodAdapter(private val dataSet: ArrayList<PeriodDetails>) :
    RecyclerView.Adapter<PeriodAdapter.ViewHolder>() {

    private var previousExpandedPosition = -1
    private var mExpandedPosition = -1

    class ViewHolder(private val binding: CardPeriodBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val arrow = binding.arrowMoreInfo
        val moreInfo = binding.moreInfo
        val expandedBackground = binding.expandedBackground
        val periodTime = binding.periodTime
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
        holder.bind(dataSet[position])

        val startTime: Date = dataSet[position].startTime.toDate() as Date
        val simpleDateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val sTime: String = simpleDateFormat.format(startTime).toUpperCase(Locale.ROOT)

        val endTime: Date = dataSet[position].endTime.toDate() as Date
        val eTime: String = simpleDateFormat.format(endTime).toUpperCase(Locale.ROOT)

        holder.periodTime.text = "$sTime - $eTime"

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
