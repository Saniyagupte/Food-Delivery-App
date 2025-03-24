package com.example.foodie.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("/login")
    fun loginUser(@Body user: User): Call<User>

    @GET("/users/{id}")
    fun getUser(@Path("id") id: Int): Call<User>
}
