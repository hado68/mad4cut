package com.example.login.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.login.R
import com.example.login.RetrofitClient
import com.example.login.databinding.FragmentDashboardBinding
import com.example.login.interfaces.ApiService
import com.example.login.models.ImagesResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import kotlin.concurrent.thread

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val PICK_IMAGE_REQUEST = 1
    private val imageUrls: MutableList<String> = mutableListOf()
    private lateinit var adapter: RecyclerAdapter
    private val apiService: ApiService by lazy {
        RetrofitClient.getClient(requireContext()).create(ApiService::class.java)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        fetchImageUrls()

        val uploadButton = binding.imagesendbutton

        uploadButton.setOnClickListener {
            openImageChooser()
        }
        initRecycler()
        return root
    }
    private fun openImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val uri: Uri? = data.data
            uri?.let {
                val file = File(requireContext().cacheDir, getFileName(uri))
                requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                    file.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                uploadFile(file)
            }
        }
    }
    private fun getFileName(uri: Uri): String {
        val result: String? = if (uri.scheme == "content") {
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (it != null && it.moveToFirst()) {
                    it.getString(it.getColumnIndexOrThrow("_display_name"))
                } else {
                    null
                }
            }
        } else {
            uri.path?.let {
                val cut = it.lastIndexOf('/')
                if (cut != -1) it.substring(cut + 1) else it
            }
        }
        return result ?: "unknown_file"
    }
    private fun uploadFile(file: File) {
        val requestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
        val body = MultipartBody.Part.createFormData("file", file.name, requestBody)
        val call = apiService.uploadFile(body)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("FileUpload", "File uploaded successfully")
                } else {
                    Log.e("FileUpload", "File upload failed")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace()
                Log.e("FileUpload", "File upload failed")
            }
        })
    }

    private fun fetchImageUrls() {
        val call = apiService.listFiles()
        call.enqueue(object : Callback<ImagesResponse> {
            override fun onResponse(call: Call<ImagesResponse>, response: Response<ImagesResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { imagesResponse ->
                        val urls = imagesResponse.data.images.map { "https://b0b1-223-39-176-107.ngrok-free.app${it.url}" }
                        Log.d("FetchImage", "$urls")
                        imageUrls.clear()
                        imageUrls.addAll(urls)
                        adapter.notifyDataSetChanged()
                        initRecycler()
                    }
                } else {
                    Log.e("ImageList", "Failed to fetch image URLs")
                }
            }

            override fun onFailure(call: Call<ImagesResponse>, t: Throwable) {
                t.printStackTrace()
                Log.e("ImageList", "Failed to fetch image URLs")
            }
        })
    }

    private fun initRecycler() {
        imageUrls.add("https://search.pstatic.net/sunny/?src=https%3A%2F%2Fwww.shutterstock.com%2Fimage-vector%2Fthis-minimalist-logo-design-your-260nw-2397842879.jpg&type=a340")
        imageUrls.add("https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyNDA2MDRfMTEg%2FMDAxNzE3NDY3OTU0NjQ3.manwzdw7qWGewwwO_hFRmbTUh19i5bw8LyreA2zoYk4g.VsLhsUk4osPUU0IxhF2z1xIXoWOyGJkyFl7C0Okkrb8g.PNG%2Fv2.PNG&type=a340")
        imageUrls.add("https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyNDAzMDVfMjUw%2FMDAxNzA5NjM0NDc5NjM3.ZTlyfczxfjQL_VKukur1s8AWQd4DeuO8qMnAp6mdmaMg.clBh_0nDh-Ox3uX7-zug3ezWMATU66QBQc4gRxzSpZ4g.JPEG%2FIMG_3512.jpg&type=a340")
        imageUrls.add("https://search.pstatic.net/sunny/?src=https%3A%2F%2Fw7.pngwing.com%2Fpngs%2F885%2F946%2Fpng-transparent-groudon-darkrai-pokedex-pokemon-giratina-pokemon-seafood-fictional-character-crab.png&type=a340")
        imageUrls.add("https://search.pstatic.net/sunny/?src=https%3A%2F%2Fi.namu.wiki%2Fi%2F1Ogotf36_OmhfkNY4gR7Mm_PqdDX8BOEU2qUKhL1SgAnYDsRBbzdS57G4SMqMxypVYDQsP0GSnOoEKD7n3JXhQ.webp&type=a340")
        imageUrls.add("https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyNDA0MDlfMTk1%2FMDAxNzEyNjUwMDE1NDk3.vtwekxMeNXbVniVziotwGp-OnxBi1u2LdFSpFgCKiE8g.dP2UWN2IVNTZHmSy4GYROBDgE9YbyhUsw6m7tWIOt0wg.JPEG%2Fbandicam_2024-04-09_17-06-24-112.jpg&type=a340")
        imageUrls.add("https://search.pstatic.net/sunny/?src=https%3A%2F%2Fw7.pngwing.com%2Fpngs%2F866%2F884%2Fpng-transparent-pokemon-x-and-y-pokemon-heartgold-and-soulsilver-pokemon-sun-and-moon-pokemon-crystal-lugia-lugia-pokemon-mammal-vertebrate-cartoon.png&type=a340")
        adapter = RecyclerAdapter(imageUrls)
        binding.recyclerview.adapter = adapter

        binding.recyclerview.layoutManager = GridLayoutManager(requireContext(),3)
        adapter.setItemClickListener(object : RecyclerAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                //여기서 이미지 클릭시 하게될 행동 쓰기
            }
        })

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}