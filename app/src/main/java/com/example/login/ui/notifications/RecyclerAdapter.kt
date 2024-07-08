package com.example.login.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.login.R

class RecyclerAdapter(
    private val items: MutableList<String>
) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    interface onItemClickListener {
        fun onItemClick(position: Int)
        fun onItemLongClick(position: Int): Boolean
    }

    private lateinit var itemClickListener: onItemClickListener

    fun setItemClickListener(itemClickListener: onItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_recyclerview, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(items[position], itemClickListener)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(item: String, clickListener: onItemClickListener) {
            val imageArea = itemView.findViewById<ImageView>(R.id.imageArea)

            Glide.with(itemView.context)
                .load(item)
                .into(imageArea)

            itemView.setOnClickListener {
                clickListener.onItemClick(adapterPosition)
            }

            itemView.setOnLongClickListener {
                clickListener.onItemLongClick(adapterPosition)
            }
        }
    }
}
