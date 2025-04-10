package com.foodie.user.network

import com.foodie.user.network.RetrofitClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {  // Declared as an object for a singleton pattern
    private const val BASE_URL = "http://192.168.1.3:1234/" // IMPORTANT: Verify this IP and port!

    private val retrofit: Retrofit by lazy {  // Lazy initialization ensures it's only created once
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getApiService(): ApiService { // Function to access the ApiService
        return retrofit.create(ApiService::class.java)
    }


    val api: com.foodie.user.network.ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(RetrofitClient.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(com.foodie.user.network.ApiService::class.java)
    }
}