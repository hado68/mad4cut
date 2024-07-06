package com.example.login.models

import com.google.gson.annotations.SerializedName

data class RegisterData(
    @SerializedName("token") val token: String,

)
