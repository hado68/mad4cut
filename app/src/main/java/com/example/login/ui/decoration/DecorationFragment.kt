package com.example.login.ui.decoration

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationSet
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.login.R
import com.example.login.RetrofitClient
import com.example.login.databinding.FragmentDecorationBinding
import com.example.login.interfaces.ApiService
import com.example.login.models.ImagesResponse
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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


class DecorationFragment : Fragment(){
    private var _binding: FragmentDecorationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val imageUrls: MutableList<String> = mutableListOf()
    private lateinit var adapter: RecyclerAdapter
    private lateinit var toggleButton: MaterialButton
    private var isRecyclerViewVisible = true
    private lateinit var recyclerView: RecyclerView
    private val apiService: ApiService by lazy {
        RetrofitClient.getClient(requireContext()).create(ApiService::class.java)
    }
    private var dX: Float = 0f
    private var dY: Float = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDecorationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getByteArray("image_bitmap")?.let { byteArray ->
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            binding.backgroundImage.setImageBitmap(bitmap)
        }
        recyclerView = binding.recyclerview
        toggleButton = binding.buttonToggle

        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        fetchImageUrls()
        initRecycler()
        recyclerView.visibility = View.VISIBLE
        isRecyclerViewVisible = true
        toggleButton.setIconResource(R.drawable.arrow_down2)
        toggleButton.setOnClickListener {
            if (isRecyclerViewVisible) {
                slideDown(recyclerView, toggleButton)
            } else {
                slideUp(recyclerView, toggleButton)
            }
            isRecyclerViewVisible = !isRecyclerViewVisible
        }

        binding.download.setOnClickListener{
            toggleButton.visibility = View.INVISIBLE
            recyclerView.visibility = View.INVISIBLE
            binding.download.visibility = View.INVISIBLE
            val bitmap = captureFragment(this)
            bitmap?.let {
                val file = bitmapToFile(it)
                uploadFile(file)
                findNavController().navigate(R.id.action_decoration_to_home)
            }
            toggleButton.visibility = View.VISIBLE
            recyclerView.visibility = View.VISIBLE
            binding.download.visibility = View.VISIBLE
        }

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

        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter.setItemClickListener(object : RecyclerAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                Log.d("click","button is clicked ${imageUrls[position]}")
                addDraggableImageView(imageUrls[position])


            }
        })

    }
    private fun fetchImageUrls() {
        val call = apiService.stickerFiles()
        call.enqueue(object : Callback<ImagesResponse> {
            override fun onResponse(call: Call<ImagesResponse>, response: Response<ImagesResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { imagesResponse ->
                        val urls = imagesResponse.data.images.map { "https://b0b1-223-39-176-107.ngrok-free.app${it.url}" }
                        Log.d("FetchImage", "$urls")
                        imageUrls.clear()
                        imageUrls.addAll(urls)
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
    private fun addDraggableImageView(imageUrl: String) {
        val imageView = ImageView(requireContext()).apply {
            id = View.generateViewId()
            layoutParams = ConstraintLayout.LayoutParams(80.dpToPx(), 80.dpToPx())
            loadImageFromUrl(imageUrl, this)
        }

        binding.rootLayout.addView(imageView)
        val set = ConstraintSet()
        set.clone(binding.rootLayout)
        set.connect(imageView.id, ConstraintSet.TOP, binding.rootLayout.id, ConstraintSet.TOP, 60)
        set.connect(imageView.id, ConstraintSet.START, binding.rootLayout.id, ConstraintSet.START, 60)
        set.applyTo(binding.rootLayout)
        imageView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX + dX
                    val newY = event.rawY + dY
                    v.animate()
                        .x(newX)
                        .y(newY)
                        .setDuration(0)
                        .start()
                    val params = v.layoutParams as ConstraintLayout.LayoutParams
                    params.leftMargin = newX.toInt()
                    params.topMargin = newY.toInt()
                    v.layoutParams = params
                }
                else -> return@setOnTouchListener false
            }
            true
        }
    }
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
    private fun slideUp(recyclerView: View, toggleButton: MaterialButton) {
        val displayMetrics = resources.displayMetrics
        val px50dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, displayMetrics)

        // 리사이클러뷰 애니메이션 설정
        val recyclerViewAnimator = ObjectAnimator.ofFloat(recyclerView, "translationY", recyclerView.height.toFloat(), 0f)
        // 버튼 애니메이션 설정
        val buttonAnimator = ObjectAnimator.ofFloat(toggleButton, "translationY", recyclerView.height.toFloat() - px50dp, 0f)

        // 애니메이션 세트로 동시에 실행
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(recyclerViewAnimator, buttonAnimator)
        animatorSet.duration = 500

        // 애니메이션 시작 전 가시성 설정
        recyclerView.visibility = View.VISIBLE

        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                toggleButton.setIconResource(R.drawable.arrow_down2)
            }
        })
        animatorSet.start()
    }

    private fun slideDown(recyclerView: View, toggleButton: MaterialButton) {
        val displayMetrics = resources.displayMetrics
        val px50dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, displayMetrics)

        // 리사이클러뷰 애니메이션 설정
        val recyclerViewAnimator = ObjectAnimator.ofFloat(recyclerView, "translationY", 0f, recyclerView.height.toFloat())
        // 버튼 애니메이션 설정
        val buttonAnimator = ObjectAnimator.ofFloat(toggleButton, "translationY", 0f, recyclerView.height.toFloat() - px50dp)

        // 애니메이션 세트로 동시에 실행
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(recyclerViewAnimator, buttonAnimator)
        animatorSet.duration = 500

        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                recyclerView.visibility = View.INVISIBLE
                toggleButton.setIconResource(R.drawable.arrow_up2)
            }
        })
        animatorSet.start()
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
    private fun uploadFile(file: File) {
        val requestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
        val body = MultipartBody.Part.createFormData("file", file.name, requestBody)
        val call = apiService.uploadCapture(body)
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
    private fun bitmapToFile(bitmap: Bitmap): File {
        // 이미지 파일이 저장될 디렉토리
        val filesDir = requireContext().filesDir
        // 파일 이름 및 경로 설정
        val imageFile = File(filesDir, "captured_image.png")

        // 파일 출력 스트림 생성 및 비트맵을 PNG 형식으로 저장
        val os: OutputStream
        try {
            os = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            os.flush()
            os.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return imageFile
    }
    private fun captureFragment(fragment: Fragment): Bitmap? {
        val view = fragment.view ?: return null
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}