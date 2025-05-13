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

    @POST("/api/auth/google") // Adjust the endpoint URL as needed
    fun googleSignIn(@Body request: GoogleSignInRequest): Call<AuthResponse>

    @POST("/signup")
    fun signupUser(@Body signupRequest: SignupRequest): Call<SignupResponse>

    @GET("/getAllFoodItems")
    fun getAllFoodItems(): Call<List<Fooditems>>

    @GET("/getFoodItemById")
    fun getFoodItemById(@Query("id") id: String): Call<FoodItem>

    @POST("/cart/add")
    fun addToCart(@Body request: CartRequest): Call<ApiResponse>

    @GET("cart/{userId}")
    fun getCartItems(@Path("userId") userId: Int): Call<List<CartItem>>

    @POST("/cart/increase")
    fun increaseQuantity(@Body payload: Map<String, Int>): Call<CartActionResponse>

    @POST("/cart/decrease")
    fun decreaseQuantity(@Body payload: Map<String, Int>): Call<CartActionResponse>

    @POST("/cart/delete")
    fun deleteItem(@Body payload: Map<String, Int>): Call<CartActionResponse>

    @POST("/orders/place")
    fun placeOrder(@Body request: PlaceOrderRequest): Call<ApiResponse>

    @GET("/search-recipes")
    fun getRecipes(@Query("query") query: String): Call<SearchResponse>

    @POST("/generate-recipe")
    fun generateRecipe(@Body generateRequest: GenerateRequest): Call<GenerateResponse>
}
data class GenerateRequest(val query: String)

data class GenerateResponse(val recipe: String)