package com.example.login.ui.home

import android.Manifest
import android.content.Context.CAMERA_SERVICE
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ExifInterface
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.login.databinding.FragmentHomeBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

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

        textureView1 = binding.textureView1
        textureView2 = binding.textureView2
        textureView3 = binding.textureView3
        textureView4 = binding.textureView4
        capturedImage1 = binding.capturedImage1
        capturedImage2 = binding.capturedImage2
        capturedImage3 = binding.capturedImage3
        capturedImage4 = binding.capturedImage4

        setupTextureListeners()

        binding.startButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), REQUEST_PERMISSIONS)
            } else {
                startCameraSequence()

            }
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
            openCamera(textureView, capturedImage)
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
                    val texture = textureView.surfaceTexture!!
                    texture.setDefaultBufferSize(previewSize.width, previewSize.height)
                    val surface = Surface(texture)
                    val captureRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    captureRequestBuilder.addTarget(surface)
                    camera.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            captureSession = session
                            session.setRepeatingRequest(captureRequestBuilder.build(), null, handler)
                            handler.postDelayed({ takePicture(textureView, capturedImage) }, 5000)
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {}
                    }, handler)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                }
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
