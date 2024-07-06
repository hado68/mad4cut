package com.example.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.login.databinding.ActivityLoginBinding
import com.example.login.ui.login.LoginFragment

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // LoginFragment를 Activity에 추가
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }
}