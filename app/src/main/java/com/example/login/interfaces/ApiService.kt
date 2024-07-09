package com.example.login.interfaces

import com.example.login.models.FrameResponse
import com.example.login.models.ImagesResponse
import com.example.login.models.NameResponse
import com.example.login.models.RegisterData
import com.example.login.models.StickerResponse
import com.example.login.models.TokenResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @POST("/api/v1/member/login")
    fun sendToken(@Body tokenRequest: RegisterData): Call<TokenResponse>


    @GET("/api/v1/member/gallery")
    fun getgalleryFiles(): Call<ImagesResponse>

    @GET("/api/v1/frames")
    fun frameFiles(): Call<FrameResponse>

    @GET("/api/v1/sticker/personal")
    fun stickerFiles(): Call<StickerResponse>

    @Multipart
    @POST("images/upload")
    fun uploadFile(@Part file: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST("/api/v1/member/gallery/save")
    fun uploadCapture(@Part file: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST("/api/v1/sticker/save")
    fun uploadSticker(@Part file: MultipartBody.Part): Call<ResponseBody>
}