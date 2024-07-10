package com.example.login.ui.home

import android.Manifest
import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.graphics.drawable.BitmapDrawable
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.login.ImageUtil
import com.example.login.R
import com.example.login.RetrofitClient
import com.example.login.databinding.FragmentHomeBinding
import com.example.login.interfaces.ApiService
import com.example.login.models.FrameResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import kotlin.concurrent.thread

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var handler: Handler

    private lateinit var textureView: TextureView

    private lateinit var capturedImage1: ImageView
    private lateinit var capturedImage2: ImageView
    private lateinit var capturedImage3: ImageView
    private lateinit var capturedImage4: ImageView

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null

    private val REQUEST_PERMISSIONS = 100

    private var currentCaptureIndex = 0
    private var frontCameraId: String? = null
    private lateinit var cameraManager: CameraManager

    private val imageUrls: MutableList<String> = mutableListOf()
    private val capturedBitmaps: MutableList<Bitmap> = mutableListOf()
    private var isFirstClick = true
    private val apiService: ApiService by lazy {
        RetrofitClient.getClient(requireContext()).create(ApiService::class.java)
    }
    private lateinit var timerTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler(Looper.getMainLooper())

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
        fetchImageUrls()
        handler = Handler()

        initRecycler()
        textureView = binding.textureView1
        capturedImage1 = binding.capturedImage1
        capturedImage2 = binding.capturedImage2
        capturedImage3 = binding.capturedImage3
        capturedImage4 = binding.capturedImage4

        setupTextureListener()

        timerTextView = binding.timerTextView

        binding.startButton.setOnClickListener {
            val imageLoader = ImageLoader.Builder(requireContext())
                .componentRegistry { add(SvgDecoder(requireContext()))
                }
                .build()

            val imageRequest = ImageRequest.Builder(requireContext())
                .data("https://b732-223-39-177-253.ngrok-free.app/frames/2.svg")
                .target(
                    onSuccess = { result ->
                        val bitmap = (result as BitmapDrawable).bitmap
                        binding.backgroundImage.setImageBitmap(bitmap)
                    },
                )
                .build()
            imageLoader.enqueue(imageRequest)
            Log.d("HomeFragment", "Start button clicked")
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
                openCamera()
            }
        }

        binding.nextButton.setOnClickListener {
            Log.d("HomeFragment", "Next button clicked")
            if (isFirstClick) {
                Log.d("HomeFragment", "Next button clicked")

                binding.indicatorRecyclerView.visibility = View.VISIBLE
            } else {
                binding.indicatorRecyclerView.visibility = View.INVISIBLE
                binding.nextButton.visibility = View.INVISIBLE
                binding.startButton.visibility = View.INVISIBLE
                val bitmap = captureFragment(this@HomeFragment)
                if (bitmap != null) {
                    saveImageAndPath(requireContext(), bitmap)
                }
                findNavController().navigate(R.id.action_home_to_decoration)
            }
            isFirstClick = !isFirstClick
        }
    }
    companion object {
        private const val PREFS_NAME = "my_prefs"
        private const val IMAGE_PATH_KEY = "image_path"
    }

    private fun saveImageAndPath(context: Context, bitmap: Bitmap) {
        val imagePath = ImageUtil.saveImageToInternalStorage(context, bitmap, "my_image.png")

        if (imagePath != null) {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(IMAGE_PATH_KEY, imagePath)
            editor.apply()
        }
    }
    private fun getFrontFacingCameraId(cameraManager: CameraManager): String? {
        try {
            for (cameraId in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    Log.d("HomeFragment", "Front facing camera found: $cameraId")
                    return cameraId
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        Log.d("HomeFragment", "No front facing camera found")
        return null
    }

    private fun setupTextureListener() {
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                Log.d("HomeFragment", "SurfaceTexture available")
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return true
            }
        }
    }

    private fun startCameraSequence() {
        Log.d("HomeFragment", "Starting camera sequence")
        currentCaptureIndex = 0
        capturedBitmaps.clear()
        startNextCapture()
    }

    private fun startNextCapture() {
        Log.d("HomeFragment", "Starting next capture. Index: $currentCaptureIndex")
        if (currentCaptureIndex < 4) {
            startCountdown {
                takePicture()
            }
        } else {
            handler.post {
                Log.d("HomeFragment", "All captures completed, showing next button")
                binding.nextButton.visibility = View.VISIBLE
                Log.d("HomeFragment", "Next button visibility: ${binding.nextButton.visibility}")
                closeCamera()
            }
        }
    }

    private fun startCountdown(onCountdownFinished: () -> Unit) {
        Log.d("HomeFragment", "Starting countdown")
        timerTextView.visibility = View.VISIBLE
        val countdownTime = 7
        val countdownHandler = Handler(Looper.getMainLooper())

        for (i in countdownTime downTo 1) {
            countdownHandler.postDelayed({
                timerTextView.text = (i - 2).toString()
                if (i == 1) {
                    Log.d("HomeFragment", "Countdown finished")
                    onCountdownFinished()
                    timerTextView.visibility = View.GONE
                }
            }, ((countdownTime - i) * 1000).toLong())
        }
    }

    private fun openCamera() {
        try {
            val cameraId = frontCameraId ?: return
            val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
            val map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val previewSize = map!!.getOutputSizes(SurfaceTexture::class.java)[0]

            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            // Initialize ImageReader
            imageReader = ImageReader.newInstance(previewSize.width, previewSize.height, ImageFormat.JPEG, 4)

            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    Log.d("HomeFragment", "Camera opened")
                    cameraDevice = camera
                    createCameraPreviewSession()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    Log.d("HomeFragment", "Camera disconnected")
                    cameraDevice?.close()
                    cameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.d("HomeFragment", "Camera error: $error")
                    camera.close()
                    cameraDevice = null
                }
            }, handler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun createCameraPreviewSession() {
        try {
            val texture = textureView.surfaceTexture ?: return
            val surface = Surface(texture)

            val previewRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder?.addTarget(surface)

            cameraDevice?.createCaptureSession(listOf(surface, imageReader?.surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    try {
                        previewRequestBuilder?.build()?.let {
                            captureSession?.setRepeatingRequest(it, null, handler)
                        }
                        Log.d("HomeFragment", "Camera preview session started")
                        startCameraSequence()
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.d("HomeFragment", "Failed to configure camera")
                    Toast.makeText(context, "Failed to configure camera", Toast.LENGTH_SHORT).show()
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
        imageReader?.close()
        imageReader = null
        // TextureView를 숨기도록 추가
        handler.post {
            Log.d("HomeFragment", "Closing camera")
            textureView.visibility = View.GONE
            // 캡처된 이미지가 보이도록 설정
            _binding?.let { binding ->
                if (capturedBitmaps.size > 0) binding.capturedImage1.setImageBitmap(capturedBitmaps[0])
                if (capturedBitmaps.size > 1) binding.capturedImage2.setImageBitmap(capturedBitmaps[1])
                if (capturedBitmaps.size > 2) binding.capturedImage3.setImageBitmap(capturedBitmaps[2])
                if (capturedBitmaps.size > 3) binding.capturedImage4.setImageBitmap(capturedBitmaps[3])

                binding.capturedImage1.visibility = View.VISIBLE
                binding.capturedImage2.visibility = View.VISIBLE
                binding.capturedImage3.visibility = View.VISIBLE
                binding.capturedImage4.visibility = View.VISIBLE
            }
        }
    }


    override fun onPause() {
        super.onPause()
        closeCamera()
    }

    override fun onResume() {
        super.onResume()
        if (textureView.isAvailable) {
            // 버튼 클릭 시 카메라를 열도록 설정되었으므로, onResume에서는 아무 작업도 하지 않음
        }
        resetCameraSequence()
        binding.startButton.visibility = View.VISIBLE
        binding.nextButton.visibility = View.INVISIBLE
    }

    private fun resetCameraSequence() {
        Log.d("HomeFragment", "Resetting camera sequence")
        currentCaptureIndex = 0
        capturedImage1.visibility = View.GONE
        capturedImage2.visibility = View.GONE
        capturedImage3.visibility = View.GONE
        capturedImage4.visibility = View.GONE

        textureView.visibility = View.VISIBLE
    }

    private fun takePicture() {
        if (cameraDevice == null || captureSession == null) return

        try {
            val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraDevice!!.id)
            val jpegSizes = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.getOutputSizes(ImageFormat.JPEG)
            val width = jpegSizes[0].width
            val height = jpegSizes[0].height

            val reader = imageReader ?: return
            val captureBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(reader.surface)
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

            reader.setOnImageAvailableListener({ reader ->
                var image: Image? = null
                try {
                    image = reader.acquireLatestImage()
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.capacity())
                    buffer.get(bytes)
                    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "pic_${currentCaptureIndex}.jpg")
                    Log.d("HomeFragment", "Saving image to: ${file.absolutePath}")
                    save(bytes, file)
                    var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    val matrix = Matrix()
                    matrix.postRotate(270f)
                    matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                    capturedBitmaps.add(bitmap)

                    handler.post {
                        currentCaptureIndex++
                        Log.d("HomeFragment", "Picture taken. Current capture index: $currentCaptureIndex")
                        if (currentCaptureIndex < 4) {
                            startNextCapture()
                        } else {
                            binding.nextButton.visibility = View.VISIBLE
                            closeCamera()
                        }
                    }
                } catch (e: IOException) {
                    Log.e("HomeFragment", "Error saving image", e)
                } finally {
                    image?.close()
                }
            }, handler)

            captureSession!!.capture(captureBuilder.build(), object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                    super.onCaptureCompleted(session, request, result)
                    Log.d("HomeFragment", "Capture completed")
                }
            }, handler)
        } catch (e: CameraAccessException) {
            Log.e("HomeFragment", "Camera access exception", e)
        }
    }

    private fun save(bytes: ByteArray, file: File) {
        var output: OutputStream? = null
        try {
            output = FileOutputStream(file)
            output.write(bytes)
            Log.d("HomeFragment", "Image saved: ${file.absolutePath}")
        } catch (e: IOException) {
            Log.e("HomeFragment", "Error writing to file", e)
        } finally {
            try {
                output?.close()
            } catch (e: IOException) {
                Log.e("HomeFragment", "Error closing output stream", e)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Permissions denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fetchImageUrls() {
        val call = apiService.frameFiles()
        call.enqueue(object : Callback<FrameResponse> {
            override fun onResponse(call: Call<FrameResponse>, response: Response<FrameResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { imagesResponse ->
                        val baseUrl = context?.getString(R.string.base_url)
                        val urls = imagesResponse.data.frames.map { "${baseUrl}${it.url}" }
                        Log.d("FetchImage", "$urls")
                        imageUrls.clear()
                        imageUrls.addAll(urls)
                        initRecycler()  // RecyclerView 초기화 호출
                    }
                } else {
                    Log.e("ImageList", "Failed to fetch image URLs")
                }
            }

            override fun onFailure(call: Call<FrameResponse>, t: Throwable) {
                t.printStackTrace()
                Log.e("ImageList", "Failed to fetch image URLs")
            }
        })
    }

    private fun initRecycler() {
        val indicatorAdapter = IndicatorAdapter(imageUrls.size) { position ->
            val imageLoader = coil.ImageLoader.Builder(requireContext())
                .componentRegistry { add(SvgDecoder(requireContext())) }
                .build()

            val imageRequest = ImageRequest.Builder(requireContext())
                .data(imageUrls[position])
                .target(
                    onSuccess = { result ->
                        val bitmap = (result as BitmapDrawable).bitmap
                        binding.backgroundImage.setImageBitmap(bitmap)
                    },
                )
                .build()
            imageLoader.enqueue(imageRequest)
        }
        binding.indicatorRecyclerView.adapter = indicatorAdapter
        binding.indicatorRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.indicatorRecyclerView.visibility = View.INVISIBLE  // RecyclerView를 보이지 않도록 설정
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
