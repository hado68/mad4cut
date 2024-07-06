package com.example.login.interfaces

import com.example.login.models.NameResponse
import com.example.login.models.RegisterData
import com.example.login.models.TokenResponse
import com.example.login.models.UserName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface LoginApiService {
    @POST("auth/naver")
    fun sendToken(@Body tokenRequest: RegisterData): Call<TokenResponse>


    @GET("test")
    fun getUserProfile(): Call<NameResponse>
}