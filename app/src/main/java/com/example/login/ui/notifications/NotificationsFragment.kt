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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class NotificationsFragment : Fragment(), StickerAdapter.OnItemClickListener {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private val PICK_IMAGE_REQUEST = 1
    private val personalStickers: MutableList<String> = mutableListOf()
    private val sharedStickers: MutableList<String> = mutableListOf()
    private val combinedStickers: MutableList<String> = mutableListOf()
    private var adapter: StickerAdapter? = null
    private val spanCount = 3
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
        registerForContextMenu(binding.recyclerview)
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
            override fun onResponse(call: Call<ApiResponse<StickerListInfo>>, response: Response<ApiResponse<StickerListInfo>>) {
                if (response.isSuccessful) {
                    response.body()?.data?.stickers?.let { stickers ->
                        personalStickers.clear()
                        personalStickers.addAll(stickers.map { "https://b0b1-223-39-176-107.ngrok-free.app${it.url}" })
                        combineStickers()
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
            override fun onResponse(call: Call<ApiResponse<StickerListInfo>>, response: Response<ApiResponse<StickerListInfo>>) {
                if (response.isSuccessful) {
                    response.body()?.data?.stickers?.let { stickers ->
                        sharedStickers.clear()
                        sharedStickers.addAll(stickers.map { "https://705a-223-39-176-104.ngrok-free.app${it.url}" })
                        combineStickers()
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

    private fun combineStickers() {
        combinedStickers.clear()
        combinedStickers.addAll(personalStickers)

        val emptyCount = spanCount - (personalStickers.size % spanCount)
        if (emptyCount != spanCount) {
            for (i in 0 until emptyCount) {
                combinedStickers.add("") // Add empty stickers
            }
        }

        combinedStickers.addAll(sharedStickers)
        adapter?.notifyDataSetChanged()
    }

    private fun shareSticker(url: String) {
        val stickerId = extractStickerIdFromUrl(url)
        if (stickerId != null) {
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
    }

    private fun deleteSticker(url: String) {
        val stickerId = extractStickerIdFromUrl(url)
        if (stickerId != null) {
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
    }

    private fun initRecycler() {
        // Add dummy data
        sharedStickers.add("https://postfiles.pstatic.net/20141106_49/bsh040817_1415273770472wqcn7_PNG/PGL_Mega-Bisaflor.png?type=w2")
        personalStickers.add("https://postfiles.pstatic.net/20141106_49/bsh040817_1415273770472wqcn7_PNG/PGL_Mega-Bisaflor.png?type=w2")

        combineStickers()

        adapter = StickerAdapter(combinedStickers, spanCount)
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = GridLayoutManager(requireContext(), spanCount)
        adapter?.setItemClickListener(this)
    }

    override fun onItemClick(position: Int) {
        // Handle item click if needed
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        activity?.menuInflater?.inflate(R.menu.context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val position = item.groupId
        val url = combinedStickers[position]
        if (url.isEmpty()) return super.onContextItemSelected(item) // Ignore empty stickers

        return when (item.itemId) {
            R.id.context_menu_share -> {
                shareSticker(url)
                true
            }
            R.id.context_menu_delete -> {
                deleteSticker(url)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun extractStickerIdFromUrl(url: String): Long? {
        // Logic to extract sticker ID from the URL
        val regex = "/images/(\\d+)_".toRegex()
        val matchResult = regex.find(url)
        return matchResult?.groupValues?.get(1)?.toLongOrNull()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
