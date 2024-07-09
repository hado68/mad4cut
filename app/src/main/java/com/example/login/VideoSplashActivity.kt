package com.example.login

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Surface
import android.view.TextureView
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class VideoSplashActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var textureView: TextureView
    private var isVideoReadyToPlay = false
    private var isSurfaceReady = false
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_splash)

        textureView = findViewById(R.id.textureView)
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {
                isSurfaceReady = true
                tryToStartVideo(surface)
            }

            override fun onSurfaceTextureSizeChanged(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureUpdated(surface: android.graphics.SurfaceTexture) {}
            override fun onSurfaceTextureDestroyed(surface: android.graphics.SurfaceTexture): Boolean {
                mediaPlayer.release()
                return true
            }
        }

        initializeMediaPlayer()
    }

    private fun initializeMediaPlayer() {
        val videoUri: Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.splash3)
        mediaPlayer = MediaPlayer().apply {
            setDataSource(applicationContext, videoUri)
            isLooping = false
            setOnPreparedListener {
                isVideoReadyToPlay = true
                tryToStartVideo(textureView.surfaceTexture)
            }
            setOnCompletionListener {
                // 비디오가 완료된 후 LoginActivity로 이동
                startActivity(Intent(this@VideoSplashActivity, LoginActivity::class.java))
                overridePendingTransition(0, 0) // 전환 애니메이션 제거
                finish()
            }
            prepareAsync()
        }
    }

    private fun tryToStartVideo(surface: android.graphics.SurfaceTexture?) {
        if (isSurfaceReady && isVideoReadyToPlay && surface != null) {
            mediaPlayer.setSurface(Surface(surface))
            // 추가적인 지연 없이 Surface와 MediaPlayer가 모두 준비된 후 바로 재생
            mediaPlayer.start()
        }
    }
}
