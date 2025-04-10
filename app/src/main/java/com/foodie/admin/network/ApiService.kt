package com.foodie.admin.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("/loginOwner")
    fun loginOwner(@Body request: AdminLoginRequest): Call<AdminLoginResponse>

    @GET("/owner/{id}")
    fun getOwner(@Path("id") id: Int): Call<Owner>

    @POST("/signupOwner")
    fun signupOwner(@Body signupRequest: AdminSignupRequest): Call<AdminSignupResponse>

    @POST("addItem")
    fun addItem(@Body request: FoodItem): Call<AddFoodItemResponse>

    @GET("/getAllFoodItems")
    fun getAllFoodItems(): Call<List<Fooditems>>
}
