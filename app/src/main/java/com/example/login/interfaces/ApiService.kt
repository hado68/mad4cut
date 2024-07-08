package com.example.login.interfaces

import com.example.login.models.ImagesResponse
import com.example.login.models.NameResponse
import com.example.login.models.RegisterData
import com.example.login.models.TokenResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @POST("auth/naver")
    fun sendToken(@Body tokenRequest: RegisterData): Call<TokenResponse>


    @GET("test")
    fun getUserProfile(): Call<NameResponse>

    @GET("images/getimage")
    fun listFiles(): Call<ImagesResponse>

    @Multipart
    @POST("images/upload")
    fun uploadFile(@Part file: MultipartBody.Part): Call<ResponseBody>
}