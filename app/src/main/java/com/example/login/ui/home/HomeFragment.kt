package com.example.login.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.login.MainActivity
import com.example.login.RetrofitClient
import com.example.login.databinding.FragmentHomeBinding
import com.example.login.interfaces.LoginApiService
import com.example.login.models.NameResponse
import com.example.login.models.RegisterData
import com.example.login.models.TokenResponse
import com.example.login.models.UserName
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.jar.Attributes.Name

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val loginApiService: LoginApiService by lazy {
        RetrofitClient.getClient(requireContext()).create(LoginApiService::class.java)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tempButton.setOnClickListener{
            fetchUserProfile()

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun fetchUserProfile() {

        loginApiService.getUserProfile().enqueue(object : Callback<NameResponse> {
            override fun onResponse(call: Call<NameResponse>, response: Response<NameResponse>) {
                if (response.isSuccessful) {
                    val userProfile = response.body()
                    if (userProfile != null) {
                        Log.d("AnotherFragment", "User Profile: ${userProfile.data}")
                        binding.text.text = "${userProfile.data.name}"
                    }
                } else {
                    Log.e("AnotherFragment", "Request failed with status: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<NameResponse>, t: Throwable) {
                Log.e("AnotherFragment", "Network error: $t")
                Toast.makeText(requireContext(), "Network error: $t", Toast.LENGTH_LONG).show()
            }
        })
    }
}