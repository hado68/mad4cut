package com.example.login

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    @Volatile
    private var retrofit: Retrofit? = null

    fun getClient(context: Context): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: buildRetrofit(context).also { retrofit = it }
        }
    }

    fun updateToken(context: Context, newToken: String) {
        val sharedPreferences = context.getSharedPreferences("LoginSession", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("TOKEN", newToken)
        editor.apply()

        synchronized(this) {
            retrofit = buildRetrofit(context)
        }
    }

    private fun buildRetrofit(context: Context): Retrofit {
        val token = getSavedToken(context)
        val client = if (!token.isNullOrEmpty()) {
            OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(token))
                .build()
        } else {
            OkHttpClient.Builder().build()
        }

        return Retrofit.Builder()
            .baseUrl("https://b732-223-39-177-253.ngrok-free.app")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun getSavedToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("LoginSession", Context.MODE_PRIVATE)
        return sharedPreferences.getString("TOKEN", null)
    }
}
