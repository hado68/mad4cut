package com.example.login

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(private val token: String) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(newRequest)
    }
}