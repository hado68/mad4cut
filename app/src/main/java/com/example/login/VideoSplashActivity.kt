package com.example.login

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class VideoSplashActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var textureView: TextureView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_splash)

        textureView = findViewById(R.id.textureView)
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {
                val videoUri: Uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.splash3)
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(applicationContext, videoUri)
                    setSurface(Surface(surface))
                    isLooping = false
                    setOnPreparedListener { mp ->
                        mp.start()
                    }
                    setOnCompletionListener {
                        startActivity(Intent(this@VideoSplashActivity, LoginActivity::class.java))
                        overridePendingTransition(0, 0) // 전환 애니메이션 제거
                        finish()
                    }
                    prepareAsync()
                }
            }

            override fun onSurfaceTextureSizeChanged(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureDestroyed(surface: android.graphics.SurfaceTexture): Boolean {
                mediaPlayer.release()
                return true
            }
            override fun onSurfaceTextureUpdated(surface: android.graphics.SurfaceTexture) {}
        }
    }
}
