package com.example.login.interfaces

import com.example.login.models.ApiResponse
import com.example.login.models.FrameResponse
import com.example.login.models.ImagesResponse
import com.example.login.models.NameResponse
import com.example.login.models.RegisterData
import com.example.login.models.ShareStickerRequest
import com.example.login.models.SharedStickerInfo
import com.example.login.models.StickerInfo
import com.example.login.models.StickerListInfo
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

    @GET("/api/v1/member/sticker/personal")
    fun stickerFiles(): Call<ImagesResponse>

    @Multipart
    @POST("images/upload")
    fun uploadFile(@Part file: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST("/api/v1/member/gallery/save")
    fun uploadCapture(@Part file: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST("/api/v1/sticker/save")
    fun uploadSticker(@Part file: MultipartBody.Part): Call<ApiResponse<StickerInfo>>

    @GET("/api/v1/sticker/personal")
    fun getPersonalStickers(): Call<ApiResponse<StickerListInfo>>

    @GET("/api/v1/sticker/shared")
    fun getSharedStickers(): Call<ApiResponse<StickerListInfo>>

    @POST("/api/v1/sticker/share")
    fun shareSticker(@Body requestData: ShareStickerRequest): Call<ApiResponse<SharedStickerInfo>>

    @POST("/api/v1/sticker/delete")
    fun deleteSticker(@Body stickerId: Long): Call<ApiResponse<Void>>
}