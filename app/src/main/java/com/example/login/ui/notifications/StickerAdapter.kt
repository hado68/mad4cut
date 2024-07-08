package com.example.login.ui.dashboard

import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.login.R
import com.example.login.models.Sticker

class StickerAdapter(
    private val items: MutableList<Sticker>,
    private val spanCount: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private lateinit var itemClickListener: OnItemClickListener

    fun setItemClickListener(itemClickListener: OnItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].id == -1L) {
            VIEW_TYPE_EMPTY
        } else {
            VIEW_TYPE_STICKER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_STICKER) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_recyclerview, parent, false)
            StickerViewHolder(v)
        } else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_empty, parent, false)
            EmptyViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is StickerViewHolder) {
            holder.bindItems(items[position], itemClickListener)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class StickerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {
        private val imageArea: ImageView = itemView.findViewById(R.id.imageArea)

        init {
            itemView.setOnCreateContextMenuListener(this)
        }

        fun bindItems(item: Sticker, clickListener: OnItemClickListener) {
            Glide.with(itemView.context)
                .load("https://b0b1-223-39-176-107.ngrok-free.app${item.url}")
                .into(imageArea)

            itemView.setOnClickListener {
                clickListener.onItemClick(adapterPosition)
            }
        }

        override fun onCreateContextMenu(menu: ContextMenu, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            menu.add(adapterPosition, R.id.context_menu_share, 0, "Share")
            menu.add(adapterPosition, R.id.context_menu_delete, 1, "Delete")
        }
    }

    inner class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        private const val VIEW_TYPE_STICKER = 0
        private const val VIEW_TYPE_EMPTY = 1
    }
}
