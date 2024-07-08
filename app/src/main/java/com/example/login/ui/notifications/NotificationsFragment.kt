package com.example.login.ui.notifications

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.login.R
import com.example.login.RetrofitClient
import com.example.login.databinding.FragmentNotificationsBinding
import com.example.login.interfaces.ApiService
import com.example.login.models.*
import com.example.login.ui.notifications.RecyclerAdapter
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
    private val imageUrls1: MutableList<String> = mutableListOf()
    private val imageUrls2: MutableList<String> = mutableListOf()
    private lateinit var adapter1: RecyclerAdapter
    private lateinit var adapter2: RecyclerAdapter
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

        fetchPersonalStickers()
        fetchSharedStickers()

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
        val call = apiService.uploadSticker(body)
        call.enqueue(object : Callback<ApiResponse<StickerInfo>> {
            override fun onResponse(call: Call<ApiResponse<StickerInfo>>, response: Response<ApiResponse<StickerInfo>>) {
                if (response.isSuccessful) {
                    Log.d("FileUpload", "File uploaded successfully")
                    fetchPersonalStickers() // Refresh personal stickers after upload
                } else {
                    Log.e("FileUpload", "File upload failed")
                }
            }

            override fun onFailure(call: Call<ApiResponse<StickerInfo>>, t: Throwable) {
                t.printStackTrace()
                Log.e("FileUpload", "File upload failed")
            }
        })
    }

    private fun fetchPersonalStickers() {
        val call = apiService.getPersonalStickers()
        call.enqueue(object : Callback<ApiResponse<StickerListInfo>> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ApiResponse<StickerListInfo>>, response: Response<ApiResponse<StickerListInfo>>) {
                if (response.isSuccessful) {
                    response.body()?.data?.stickers?.let { stickers ->
                        val urls = stickers.map { "https://b0b1-223-39-176-107.ngrok-free.app${it.url}" }
                        Log.d("FetchPersonalStickers", "$urls")
                        imageUrls1.clear()
                        imageUrls1.addAll(urls)
                        adapter1.notifyDataSetChanged()
                    }
                } else {
                    Log.e("FetchPersonalStickers", "Failed to fetch personal stickers")
                }
            }

            override fun onFailure(call: Call<ApiResponse<StickerListInfo>>, t: Throwable) {
                t.printStackTrace()
                Log.e("FetchPersonalStickers", "Failed to fetch personal stickers")
            }
        })
    }

    private fun fetchSharedStickers() {
        val call = apiService.getSharedStickers()
        call.enqueue(object : Callback<ApiResponse<StickerListInfo>> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ApiResponse<StickerListInfo>>, response: Response<ApiResponse<StickerListInfo>>) {
                if (response.isSuccessful) {
                    response.body()?.data?.stickers?.let { stickers ->
                        val urls = stickers.map { "https://b0b1-223-39-176-107.ngrok-free.app${it.url}" }
                        Log.d("FetchSharedStickers", "$urls")
                        imageUrls2.clear()
                        imageUrls2.addAll(urls)
                        adapter2.notifyDataSetChanged()
                    }
                } else {
                    Log.e("FetchSharedStickers", "Failed to fetch shared stickers")
                }
            }

            override fun onFailure(call: Call<ApiResponse<StickerListInfo>>, t: Throwable) {
                t.printStackTrace()
                Log.e("FetchSharedStickers", "Failed to fetch shared stickers")
            }
        })
    }

    private fun shareSticker(stickerId: Long) {
        val requestData = ShareStickerRequest(stickerId)
        val call = apiService.shareSticker(requestData)
        call.enqueue(object : Callback<ApiResponse<SharedStickerInfo>> {
            override fun onResponse(call: Call<ApiResponse<SharedStickerInfo>>, response: Response<ApiResponse<SharedStickerInfo>>) {
                if (response.isSuccessful) {
                    Log.d("ShareSticker", "Sticker shared successfully")
                    fetchSharedStickers() // Refresh shared stickers after sharing
                } else {
                    Log.e("ShareSticker", "Sticker sharing failed")
                }
            }

            override fun onFailure(call: Call<ApiResponse<SharedStickerInfo>>, t: Throwable) {
                t.printStackTrace()
                Log.e("ShareSticker", "Sticker sharing failed")
            }
        })
    }

    private fun deleteSticker(stickerId: Long) {
        val call = apiService.deleteSticker(stickerId)
        call.enqueue(object : Callback<ApiResponse<Void>> {
            override fun onResponse(call: Call<ApiResponse<Void>>, response: Response<ApiResponse<Void>>) {
                if (response.isSuccessful) {
                    Log.d("DeleteSticker", "Sticker deleted successfully")
                    fetchPersonalStickers() // Refresh personal stickers after deletion
                } else {
                    Log.e("DeleteSticker", "Sticker deletion failed")
                }
            }

            override fun onFailure(call: Call<ApiResponse<Void>>, t: Throwable) {
                t.printStackTrace()
                Log.e("DeleteSticker", "Sticker deletion failed")
            }
        })
    }

    private fun initRecycler() {
        adapter1 = RecyclerAdapter(imageUrls1)
        binding.recyclerview1.adapter = adapter1
        binding.recyclerview1.layoutManager = GridLayoutManager(requireContext(), 3)
        adapter1.setItemClickListener(object : RecyclerAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                // Handle item click for first RecyclerView
                // You can add specific actions here if needed
            }

            override fun onItemLongClick(position: Int): Boolean {
                showActionDialog(imageUrls1[position], true)
                return true
            }
        })

        adapter2 = RecyclerAdapter(imageUrls2)
        binding.recyclerview2.adapter = adapter2
        binding.recyclerview2.layoutManager = GridLayoutManager(requireContext(), 3)
        adapter2.setItemClickListener(object : RecyclerAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                // Handle item click for second RecyclerView
                // You can add specific actions here if needed
            }

            override fun onItemLongClick(position: Int): Boolean {
                showActionDialog(imageUrls2[position], false)
                return true
            }
        })
    }

    private fun showActionDialog(imageUrl: String, isPersonal: Boolean) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose an action")
        builder.setItems(arrayOf("Share", "Delete")) { dialog, which ->
            when (which) {
                0 -> {
                    // Share the sticker
                    val stickerId = extractStickerIdFromUrl(imageUrl)
                    if (stickerId != null) {
                        shareSticker(stickerId)
                    }
                }
                1 -> {
                    // Delete the sticker
                    val stickerId = extractStickerIdFromUrl(imageUrl)
                    if (stickerId != null) {
                        deleteSticker(stickerId)
                    }
                }
            }
        }
        builder.show()
    }

    private fun extractStickerIdFromUrl(url: String): Long? {
        // Logic to extract sticker ID from the URL
        // Assuming the ID is included in the URL path
        val regex = "/stickers/(\\d+)_".toRegex()
        val matchResult = regex.find(url)
        return matchResult?.groupValues?.get(1)?.toLongOrNull()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
