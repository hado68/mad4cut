package com.example.login.ui.notifications

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.login.R
import com.example.login.RetrofitClient

import com.example.login.databinding.FragmentNotificationsBinding
import com.example.login.interfaces.ApiService
import com.example.login.models.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private val PICK_IMAGE_REQUEST = 1
    private val imageUrls: MutableList<String> = mutableListOf()
    private lateinit var adapter: StickerAdapter
    private val apiService: ApiService by lazy {
        RetrofitClient.getClient(requireContext()).create(ApiService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 어댑터 초기화
        adapter = StickerAdapter(imageUrls)
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = GridLayoutManager(requireContext(), 3)

        fetchImageUrls()

        binding.imagesendbutton.setOnClickListener {
            openImageChooser()
        }

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
        val call = apiService.uploadSticker(body)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("FileUpload", "File uploaded successfully")
                    // 업로드 후 이미지 목록을 다시 불러옴
                    fetchImageUrls()
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
        val call = apiService.stickerFiles()
        call.enqueue(object : Callback<StickerResponse> {
            override fun onResponse(
                call: Call<StickerResponse>,
                response: Response<StickerResponse>
            ) {
                if (response.isSuccessful) {
                    val baseUrl = context?.getString(R.string.base_url)

                    response.body()?.let { imagesResponse ->
                        val urls = imagesResponse.data.stickers.map { "$baseUrl${it.url}" }
                        Log.d("FetchImage", "$urls")
                        // 어댑터에 새 데이터 설정
                        adapter.updateData(urls)
                    }
                } else {
                    Log.e("ImageList", "Failed to fetch image URLs")
                }
            }

            override fun onFailure(call: Call<StickerResponse>, t: Throwable) {
                t.printStackTrace()
                Log.e("ImageList", "Failed to fetch image URLs")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}