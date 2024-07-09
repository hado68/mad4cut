package com.example.login.ui.decoration

import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.login.R
import com.example.login.models.ImagesResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
            LayoutInflater.from(parent.context).inflate(R.layout.item_recyclerview, parent, false)
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

    // 화면에 표시 될 뷰를 저장하는 역할
    // View들을 재활용 하기 위해 각 요소를 저장해두고 사용한다.
    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bindItems(items: String) {
            val imageArea = itemView.findViewById<ImageView>(R.id.imageArea)

            loadImageFromUrl(items, imageArea)

        }
    }
    private fun loadImageFromUrl(imageUrl: String, imageView: ImageView) {
        thread {
            try {
                val url = URL(imageUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.doInput = true
                conn.connect()

                val inputStream: InputStream = conn.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)

                // UI 작업은 메인 스레드에서 수행
                Handler(Looper.getMainLooper()).post {
                    Log.d("FetchImage", "$bitmap")
                    imageView.setImageBitmap(bitmap)
                }

            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}