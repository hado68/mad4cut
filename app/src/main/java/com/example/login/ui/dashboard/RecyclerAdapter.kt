package com.example.login.ui.dashboard

import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.login.R
import retrofit2.http.Url
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import kotlin.concurrent.thread

class RecyclerAdapter(val items: MutableList<String>) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
    interface onItemClickListener {
        fun onItemClick(position: Int)
    }
    private lateinit var itemClickListener: onItemClickListener
    fun setItemClickListener(itemClickListener: onItemClickListener) {
        this.itemClickListener = itemClickListener
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerAdapter.ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_gallery, parent, false)
        return ViewHolder(v)
    }

    // 생성된 View Holder에 데이터를 바인딩 해주는 메서드
    override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(position)
        }
        holder.bindItems(items[position])
    }

    // 데이터의 개수를 반환하는 메서드
    override fun getItemCount(): Int {
        return items.count()
    }
    fun updateData(newImageUrls: List<String>) {
        items.clear()
        items.addAll(newImageUrls)
        notifyDataSetChanged()
    }
    // 화면에 표시 될 뷰를 저장하는 역할
    // View들을 재활용 하기 위해 각 요소를 저장해두고 사용한다.
    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bindItems(items: String) {
            val imageArea = itemView.findViewById<ImageView>(R.id.imageArea)

            Glide.with(itemView.context)
                .load(items)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageArea)

        }
    }

}