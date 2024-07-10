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
        val backgroundResource = when (position) {
            0 -> R.drawable.red_circle_selected
            1 -> R.drawable.green_circle_selected
            2 -> R.drawable.khaki_circle_selected
            3 -> R.drawable.blue_circle_selected
            4 -> R.drawable.pink_circle_selected
            5 -> R.drawable.white_circle_selected
            6 -> R.drawable.black_circle_selected
            else -> R.drawable.white_circle_selected
        }
        val unbackgroundResource = when (position) {
            0 -> R.drawable.red_circle_unselected
            1 -> R.drawable.green_circle_unselected
            2 -> R.drawable.khaki_circle_unselected
            3 -> R.drawable.blue_circle_unselected
            4 -> R.drawable.pink_circle_unselected
            5 -> R.drawable.white_circle_unselected
            6 -> R.drawable.black_circle_unselected
            else -> R.drawable.white_circle_unselected
        }
        holder.circleView.setBackgroundResource(
            if (position == selectedPosition) backgroundResource
            else unbackgroundResource
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