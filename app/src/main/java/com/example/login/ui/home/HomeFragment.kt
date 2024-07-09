package com.example.login.ui.home

import android.Manifest
import android.content.Context.CAMERA_SERVICE
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.login.R
import com.example.login.RetrofitClient
import com.example.login.databinding.FragmentHomeBinding
import com.example.login.interfaces.ApiService
import com.example.login.models.ImagesResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var handler: Handler

    private lateinit var textureView1: TextureView
    private lateinit var textureView2: TextureView
    private lateinit var textureView3: TextureView
    private lateinit var textureView4: TextureView

    private lateinit var capturedImage1: ImageView
    private lateinit var capturedImage2: ImageView
    private lateinit var capturedImage3: ImageView
    private lateinit var capturedImage4: ImageView

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null

    private val REQUEST_PERMISSIONS = 100

    private val textureViewQueue = LinkedBlockingQueue<Pair<TextureView, ImageView>>()
    private var frontCameraId: String? = null
    private lateinit var cameraManager: CameraManager  // 카메라 매니저 멤버 변수로 선언

    private val imageUrls: MutableList<String> = mutableListOf()
    private var isFirstClick = true
    private val apiService: ApiService by lazy {
        RetrofitClient.getClient(requireContext()).create(ApiService::class.java)
    }
    private lateinit var timerTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler()

        cameraManager = requireActivity().getSystemService(CAMERA_SERVICE) as CameraManager
        frontCameraId = getFrontFacingCameraId(cameraManager)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        handler = Handler()
        fetchImageUrls()
        initRecycler()
        textureView1 = binding.textureView1
        textureView2 = binding.textureView2
        textureView3 = binding.textureView3
        textureView4 = binding.textureView4
        capturedImage1 = binding.capturedImage1
        capturedImage2 = binding.capturedImage2
        capturedImage3 = binding.capturedImage3
        capturedImage4 = binding.capturedImage4

        setupTextureListeners()

        timerTextView = binding.timerTextView

        binding.startButton.setOnClickListener {
            binding.nextButton.visibility = View.INVISIBLE
            binding.startButton.visibility = View.INVISIBLE
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), REQUEST_PERMISSIONS)
            } else {
                resetCameraSequence()
                startCameraSequence()
            }
        }



        binding.nextButton.setOnClickListener{
            if (isFirstClick){
                binding.indicatorRecyclerView.visibility = View.VISIBLE
            }else{
                binding.indicatorRecyclerView.visibility = View.INVISIBLE
                binding.nextButton.visibility = View.INVISIBLE
                binding.startButton.visibility = View.INVISIBLE
                val bitmap = captureFragment(this@HomeFragment)
                bitmap?.let {
                    val stream = ByteArrayOutputStream()
                    it.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val byteArray = stream.toByteArray()

                    val bundle = Bundle().apply {
                        putByteArray("image_bitmap", byteArray)
                    }

                    findNavController().navigate(R.id.action_home_to_decoration, bundle)
                }
            }
            isFirstClick = !isFirstClick

        }

    }

    private fun getFrontFacingCameraId(cameraManager: CameraManager): String? {
        try {
            for (cameraId in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    return cameraId
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return null
    }

    private fun setupTextureListeners() {

        textureView1.surfaceTextureListener = createSurfaceTextureListener(textureView1, capturedImage1)
        textureView2.surfaceTextureListener = createSurfaceTextureListener(textureView2, capturedImage2)
        textureView3.surfaceTextureListener = createSurfaceTextureListener(textureView3, capturedImage3)
        textureView4.surfaceTextureListener = createSurfaceTextureListener(textureView4, capturedImage4)

    }

    private fun createSurfaceTextureListener(textureView: TextureView, capturedImage: ImageView): TextureView.SurfaceTextureListener {
        return object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                // TextureView가 사용 가능해지면 큐에 추가
                textureViewQueue.offer(Pair(textureView, capturedImage))

            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return true
            }
        }
    }

    private fun startCameraSequence() {
        if (textureViewQueue.isNotEmpty()) {
            startNextCameraPreview()
        }
    }

    private fun startNextCameraPreview() {
        if (textureViewQueue.isNotEmpty()) {
            val (textureView, capturedImage) = textureViewQueue.poll()

            // TextureView를 나타내고 타이머 시작
            textureView.visibility = View.VISIBLE
            openCamera(textureView, capturedImage)
        } else {
            handler.post {
                binding.nextButton.visibility = View.VISIBLE
            }
        }
    }

    private fun startCountdown(onCountdownFinished: () -> Unit) {
        timerTextView.visibility = View.VISIBLE
        val countdownTime = 7
        val countdownHandler = Handler(Looper.getMainLooper())

        for (i in countdownTime downTo 1) {
            countdownHandler.postDelayed({
                timerTextView.text = (i - 2).toString()
                if (i == 1) {
                    onCountdownFinished()
                    timerTextView.visibility = View.GONE
                }
            }, ((countdownTime - i) * 1000).toLong())
        }
    }

    private fun openCamera(textureView: TextureView, capturedImage: ImageView) {
        val cameraManager = requireActivity().getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = frontCameraId ?: return
            val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
            val map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val previewSize = map!!.getOutputSizes(SurfaceTexture::class.java)[0]

            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    try {
                        val texture = textureView.surfaceTexture ?: return
                        texture.setDefaultBufferSize(previewSize.width, previewSize.height)
                        val surface = Surface(texture)
                        val captureRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                        captureRequestBuilder.addTarget(surface)
                        camera.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(session: CameraCaptureSession) {
                                if (cameraDevice == null) return  // 추가된 null 체크
                                captureSession = session
                                try {
                                    session.setRepeatingRequest(captureRequestBuilder.build(), null, handler)
                                    startCountdown { takePicture(textureView, capturedImage) }
                                } catch (e: CameraAccessException) {
                                    e.printStackTrace()
                                    closeCamera()
                                }
                            }

                            override fun onConfigureFailed(session: CameraCaptureSession) {
                                closeCamera()
                            }
                        }, handler)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                        closeCamera()
                    }
                }

                override fun onDisconnected(camera: CameraDevice) {
                    cameraDevice?.close()
                    cameraDevice = null  // cameraDevice를 null로 설정
                    Log.e("Camera", "Camera device disconnected")
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    cameraDevice = null  // cameraDevice를 null로 설정
                    Log.e("Camera", "Error opening camera: $error")
                    if (error == ERROR_CAMERA_DEVICE || error == ERROR_CAMERA_SERVICE) {
                        closeCamera()
                        openCamera(textureView, capturedImage)
                    }
                }
            }, handler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun closeCamera() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
    }
    override fun onPause() {
        super.onPause()
        closeCamera()
    }
    override fun onResume() {
        super.onResume()
        if (textureView1.isAvailable) {
            setupTextureListeners()
        }
        resetCameraSequence()
        binding.startButton.visibility = View.VISIBLE
        binding.nextButton.visibility = View.INVISIBLE
    }

    // 카메라 시퀀스 초기화 메서드 추가
    private fun resetCameraSequence() {
        textureViewQueue.clear()
        textureViewQueue.offer(Pair(textureView1, capturedImage1))
        textureViewQueue.offer(Pair(textureView2, capturedImage2))
        textureViewQueue.offer(Pair(textureView3, capturedImage3))
        textureViewQueue.offer(Pair(textureView4, capturedImage4))

        capturedImage1.visibility = View.GONE
        capturedImage2.visibility = View.GONE
        capturedImage3.visibility = View.GONE
        capturedImage4.visibility = View.GONE

        textureView1.visibility = View.VISIBLE
        textureView2.visibility = View.VISIBLE
        textureView3.visibility = View.VISIBLE
        textureView4.visibility = View.VISIBLE
    }

    private fun takePicture(textureView: TextureView, capturedImage: ImageView) {
        if (cameraDevice == null || captureSession == null) return

        val cameraManager = requireActivity().getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraDevice!!.id)
            val jpegSizes = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.getOutputSizes(ImageFormat.JPEG)
            val width = jpegSizes[0].width
            val height = jpegSizes[0].height

            val reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
            val outputSurfaces = ArrayList<Surface>(2)
            outputSurfaces.add(reader.surface)
            outputSurfaces.add(Surface(textureView.surfaceTexture))

            val captureBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(reader.surface)
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

            cameraDevice!!.createCaptureSession(outputSurfaces, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    try {
                        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "pic.jpg")
                        val readerListener = ImageReader.OnImageAvailableListener { reader ->
                            var image: Image? = null
                            try {
                                image = reader.acquireLatestImage()
                                val buffer = image.planes[0].buffer
                                val bytes = ByteArray(buffer.capacity())
                                buffer.get(bytes)
                                save(bytes, file)
                                var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                                // 이미지를 좌우 반전
                                val matrix = Matrix()
                                matrix.postRotate(270f)
                                matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
                                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                                // UI 스레드에서 실행되도록 핸들러 사용
                                handler.post {
                                    Log.d("Debug", "Handler post is running")
                                    capturedImage.setImageBitmap(bitmap)
                                    capturedImage.visibility = View.VISIBLE

                                    // TextureView 숨기기
                                    textureView.visibility = View.GONE
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                            } finally {
                                image?.close()
                            }
                        }

                        reader.setOnImageAvailableListener(readerListener, handler)
                        val captureListener = object : CameraCaptureSession.CaptureCallback() {
                            override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                                super.onCaptureCompleted(session, request, result)
                                cameraDevice?.close()
                                captureSession?.close()

                                startNextCameraPreview()
                            }
                        }

                        session.capture(captureBuilder.build(), captureListener, handler)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Toast.makeText(requireContext(), "Failed to configure camera", Toast.LENGTH_SHORT).show()
                }
            }, handler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun save(bytes: ByteArray, file: File) {
        var output: OutputStream? = null
        try {
            output = FileOutputStream(file)
            output.write(bytes)
        } finally {
            output?.close()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startCameraSequence()
            } else {
                Toast.makeText(requireContext(), "Permissions denied", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun fetchImageUrls() {
        val call = apiService.frameFiles()
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



    private fun initRecycler() {
        imageUrls.add("https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyNDA2MDRfMTEg%2FMDAxNzE3NDY3OTU0NjQ3.manwzdw7qWGewwwO_hFRmbTUh19i5bw8LyreA2zoYk4g.VsLhsUk4osPUU0IxhF2z1xIXoWOyGJkyFl7C0Okkrb8g.PNG%2Fv2.PNG&type=a340")
        imageUrls.add("https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyNDAzMDVfMjUw%2FMDAxNzA5NjM0NDc5NjM3.ZTlyfczxfjQL_VKukur1s8AWQd4DeuO8qMnAp6mdmaMg.clBh_0nDh-Ox3uX7-zug3ezWMATU66QBQc4gRxzSpZ4g.JPEG%2FIMG_3512.jpg&type=a340")
        imageUrls.add("https://search.pstatic.net/sunny/?src=https%3A%2F%2Fw7.pngwing.com%2Fpngs%2F885%2F946%2Fpng-transparent-groudon-darkrai-pokedex-pokemon-giratina-pokemon-seafood-fictional-character-crab.png&type=a340")
        imageUrls.add("https://search.pstatic.net/sunny/?src=https%3A%2F%2Fi.namu.wiki%2Fi%2F1Ogotf36_OmhfkNY4gR7Mm_PqdDX8BOEU2qUKhL1SgAnYDsRBbzdS57G4SMqMxypVYDQsP0GSnOoEKD7n3JXhQ.webp&type=a340")
        imageUrls.add("https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyNDA0MDlfMTk1%2FMDAxNzEyNjUwMDE1NDk3.vtwekxMeNXbVniVziotwGp-OnxBi1u2LdFSpFgCKiE8g.dP2UWN2IVNTZHmSy4GYROBDgE9YbyhUsw6m7tWIOt0wg.JPEG%2Fbandicam_2024-04-09_17-06-24-112.jpg&type=a340")
        imageUrls.add("https://search.pstatic.net/sunny/?src=https%3A%2F%2Fw7.pngwing.com%2Fpngs%2F866%2F884%2Fpng-transparent-pokemon-x-and-y-pokemon-heartgold-and-soulsilver-pokemon-sun-and-moon-pokemon-crystal-lugia-lugia-pokemon-mammal-vertebrate-cartoon.png&type=a340")

        val indicatorAdapter = IndicatorAdapter(imageUrls.size) { position ->
            loadImageFromUrl(imageUrls[position], binding.backgroundImage)
        }
        binding.indicatorRecyclerView.adapter = indicatorAdapter
        binding.indicatorRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
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
    private fun captureFragment(fragment: Fragment): Bitmap? {
        val view = fragment.view ?: return null
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}