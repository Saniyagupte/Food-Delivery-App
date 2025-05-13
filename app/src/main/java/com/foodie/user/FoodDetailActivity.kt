package com.foodie.user

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
// Import Button if needed, though usually not with ViewBinding directly
// import android.widget.Button
import com.foodie.foodieapp.databinding.ActivityFoodDetailBinding
import com.foodie.user.model.FoodItem
import com.foodie.user.network.ApiResponse
import com.foodie.user.network.CartRequest
import com.foodie.user.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FoodDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFoodDetailBinding

    // Keep this function as is
    fun getUserIdFromSession(): Int {
        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        // Make sure "user_id" is the EXACT key you used in LoginActivity
        return sharedPref.getInt("user_id", -1)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Get the food ID passed to the activity
        val foodIdFromIntent = intent.getIntExtra("id", -1) // Use a different name to avoid confusion later

        if (foodIdFromIntent == -1) {
            Toast.makeText(this, "Invalid food ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("FoodDetailDebug", "Fetching details for foodId: $foodIdFromIntent")

        // 2. Fetch food item details from backend
        RetrofitClient.getApiService().getFoodItemById(foodIdFromIntent.toString())
            .enqueue(object : Callback<FoodItem> {
                override fun onResponse(call: Call<FoodItem>, response: Response<FoodItem>) {
                    if (response.isSuccessful && response.body() != null) {
                        val food = response.body()!! // The fetched FoodItem

                        // 3. ✅ Populate the UI with food details
                        binding.foodName.text = food.name
                        binding.foodPrice.text = String.format("$ %.2f", food.price.toDouble())
                        binding.foodDescription.text = food.description
                        binding.foodIngredients.text = food.ingredients // Assuming ingredients is String

                        // Decode and set image
                        try {
                            // Ensure imageBase64 is not null or empty before decoding
                            if (!food.imageBase64.isNullOrEmpty()) {
                                val imageBytes = Base64.decode(food.imageBase64, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                binding.foodImage.setImageBitmap(bitmap)
                            } else {
                                Log.w("FoodDetailDebug", "Image base64 string is null or empty for food ID: ${food.id}")
                                // Optionally set a placeholder image here
                                // binding.foodImage.setImageResource(R.drawable.placeholder_image)
                            }
                        } catch (e: IllegalArgumentException) {
                            Log.e("FoodDetailDebug", "Base64 decoding failed for food ID: ${food.id}", e)
                            // Optionally set a placeholder image on error
                        } catch (e: Exception) {
                            Log.e("FoodDetailDebug", "Error setting image for food ID: ${food.id}", e)
                            // Optionally set a placeholder image on error
                        }

                        // 4. Set up the "Add to Cart" Button Click Listener
                        //    THIS IS THE KEY CHANGE: Move the cart logic inside here
                        binding.addToCartButton.setOnClickListener { // <-- Use the actual ID of your button
                            Log.d("AddToCartDebug", "Add to Cart button clicked.")

                            val userId = getUserIdFromSession()
                            // Use the ID from the 'food' object we just fetched, it's more reliable
                            val confirmedFoodId = food.id

                            Log.d("AddToCartDebug", "Attempting to add to cart: userId=$userId, foodId=$confirmedFoodId")


                            // Check if we have a valid user logged in
                            if (userId == -1) {
                                Toast.makeText(this@FoodDetailActivity, "Please log in to add items to cart", Toast.LENGTH_LONG).show()
                                // Optional: Redirect to LoginActivity
                                // val intent = Intent(this@FoodDetailActivity, LoginActivity::class.java)
                                // startActivity(intent)
                                return@setOnClickListener // Stop processing if user is not logged in
                            }

                            // Check if foodId is valid (should always be valid here if fetch succeeded, but good practice)
                            if (confirmedFoodId <= 0) { // Assuming ID must be positive
                                Toast.makeText(this@FoodDetailActivity,"Invalid Food ID, cannot add to cart.", Toast.LENGTH_SHORT).show()
                                Log.e("AddToCartError", "Invalid foodId ($confirmedFoodId) retrieved from fetched food item.")
                                return@setOnClickListener // Stop if food ID seems invalid
                            }


                            // 5. ✅ Create the request and make the API call ONLY on click
                            val request = CartRequest(userId = userId, foodId = confirmedFoodId)

                            Log.d("AddToCartDebug", "Sending request: $request")

                            RetrofitClient.getApiService().addToCart(request)
                                .enqueue(object : Callback<ApiResponse> {
                                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                                        if (response.isSuccessful && response.body()?.success == true) {
                                            Log.d("AddToCartSuccess", "Successfully added ${food.name} to cart.")
                                            Toast.makeText(this@FoodDetailActivity, "${food.name} added to cart!", Toast.LENGTH_SHORT).show()
                                            // Maybe update a cart icon count, or navigate away, or just show toast
                                        } else {
                                            val errorBody = response.errorBody()?.string() ?: response.message() ?: "Unknown error"
                                            Log.e("AddToCartError", "Failed to add to cart. Code: ${response.code()}, Body: $errorBody")
                                            Toast.makeText(this@FoodDetailActivity, "Failed to add item: $errorBody", Toast.LENGTH_LONG).show()
                                        }
                                    }

                                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                                        Log.e("AddToCartFailure", "API call failed: ${t.message}", t)
                                        Toast.makeText(this@FoodDetailActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                                    }
                                })
                        } // End of setOnClickListener

                    } else {
                        // Handle error in fetching food details
                        val errorBody = response.errorBody()?.string() ?: "Food item not found or error"
                        Log.e("FoodDetailError", "Failed to fetch food details. Code: ${response.code()}, Message: $errorBody")
                        Toast.makeText(this@FoodDetailActivity, "Error fetching food: $errorBody", Toast.LENGTH_LONG).show()
                        finish() // Close activity if food can't be loaded
                    }
                }

                override fun onFailure(call: Call<FoodItem>, t: Throwable) {
                    // Handle failure in fetching food details (network error, etc.)
                    Log.e("FoodDetailFailure", "API call failed: ${t.message}", t)
                    Toast.makeText(this@FoodDetailActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    finish() // Close activity if food can't be loaded
                }
            })
    }
}