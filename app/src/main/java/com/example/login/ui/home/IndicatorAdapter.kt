package com.example.login.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.login.R

class IndicatorAdapter(
    private var itemCount: Int,
    private val itemClickListener: (position: Int) -> Unit
) : RecyclerView.Adapter<IndicatorAdapter.IndicatorViewHolder>() {

    private var selectedPosition = 0

    class IndicatorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val circleView: View = itemView.findViewById(R.id.circle_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IndicatorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_indicator, parent, false)
        return IndicatorViewHolder(view)
    }

    override fun onBindViewHolder(holder: IndicatorViewHolder, position: Int) {
        holder.circleView.setBackgroundResource(
            if (position == selectedPosition) R.drawable.circle_selected
            else R.drawable.circle_unselected
        )
        holder.circleView.setOnClickListener {
            itemClickListener(position)
            setSelectedPosition(position)
        }
    }

    override fun getItemCount(): Int = itemCount

    fun setItemCount(newItemCount: Int) {
        itemCount = newItemCount
        notifyDataSetChanged()
    }

    private fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)
    }
}