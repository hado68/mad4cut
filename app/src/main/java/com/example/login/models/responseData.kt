package com.example.login.models

import com.google.gson.annotations.SerializedName

data class ResponseData(
    @SerializedName("token") val token: String,
)