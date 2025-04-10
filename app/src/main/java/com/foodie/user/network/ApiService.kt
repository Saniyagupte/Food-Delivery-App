package com.foodie.user.network

import com.foodie.user.model.FoodItem
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("/login")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @GET("/users/{id}")
    fun getUser(@Path("id") id: Int): Call<User>

    @POST("/signup")
    fun signupUser(@Body signupRequest: SignupRequest): Call<SignupResponse>

    @GET("/getAllFoodItems")
    fun getAllFoodItems(): Call<List<Fooditems>>

    @GET("/getFoodItemById")
    fun getFoodItemById(@Query("id") id: String): Call<FoodItem>

    @POST("/cart/add")
    fun addToCart(@Body request: CartRequest): Call<ApiResponse>

    @GET("/cart/{userId}")
    fun getCartItems(@Path("userId") userId: Int): Call<List<FoodItem>>
}
