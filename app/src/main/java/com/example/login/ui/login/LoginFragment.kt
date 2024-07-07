package com.example.login.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.login.LoginActivity
import com.example.login.MainActivity
import com.example.login.NaverLoginInitializer
import com.example.login.RetrofitClient
import com.example.login.databinding.FragmentLoginBinding
import com.example.login.interfaces.ApiService
import com.example.login.models.RegisterData
import com.example.login.models.TokenResponse
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val apiService: ApiService by lazy {
        RetrofitClient.getClient(requireContext()).create(ApiService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root

        if (isUserLoggedIn()) {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        NaverLoginInitializer.initialize(requireContext())
        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                val token = NaverIdLoginSDK.getAccessToken()
                if (token != null) {
                    val registerData = RegisterData(token)
                    apiService.sendToken(registerData).enqueue(object :
                        Callback<TokenResponse> {
                        override fun onResponse(
                            call: Call<TokenResponse>,
                            response: Response<TokenResponse>
                        ) {
                            if (response.isSuccessful) {
                                val responseWrapper = response.body()
                                if (responseWrapper != null) {
                                    val responseData = responseWrapper.data
                                    Log.d("LoginFragment", "token: ${responseData.token}")
                                    saveLoginToken(responseData.token){
                                        RetrofitClient.updateToken(requireContext(), responseData.token)
                                        val intent = Intent(requireContext(), MainActivity::class.java)
                                        startActivity(intent)
                                        requireActivity().finish()
                                    }


                                } else {
                                    Log.e("LoginFragment", "Response is null")
                                }
                            } else {
                                Log.e(
                                    "LoginFragment",
                                    "Request failed with status: ${response.code()}"
                                )
                            }
                        }

                        override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                            Log.e("LoginFragment", "Network error: $t")
                            Toast.makeText(requireContext(), "Network error: $t", Toast.LENGTH_LONG)
                                .show()
                        }
                    })
                }
            }

            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Toast.makeText(
                    requireContext(),
                    "errorCode:$errorCode, errorDesc:$errorDescription",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }

        binding.buttonOAuthLoginImg.setOnClickListener {
            NaverIdLoginSDK.authenticate(requireContext(), oauthLoginCallback)
        }

        binding.logoutbutton.setOnClickListener {
            NaverIdLoginSDK.logout()
            logout()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveLoginToken(token: String, onSaveCompleted: () -> Unit) {
        val sharedPreferences = requireContext().getSharedPreferences(
            "LoginSession",
            android.content.Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putString("TOKEN", token)
        if (editor.commit()) {
            onSaveCompleted()
        } else {
            Toast.makeText(requireContext(), "Failed to save token", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isUserLoggedIn(): Boolean {
        val sharedPreferences = requireContext().getSharedPreferences(
            "LoginSession",
            android.content.Context.MODE_PRIVATE
        )
        val token = sharedPreferences.getString("TOKEN", null)
        return token != null
    }

    private fun logout() {
        val sharedPreferences = requireContext().getSharedPreferences(
            "LoginSession",
            android.content.Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.remove("TOKEN")
        editor.apply()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}