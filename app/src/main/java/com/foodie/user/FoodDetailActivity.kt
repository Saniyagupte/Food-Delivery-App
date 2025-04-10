package com.foodie.user

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the food ID from the Intent
        val foodIdString = intent.getStringExtra("id")
        val foodId = foodIdString?.toIntOrNull()

        if (foodId == null) {
            Toast.makeText(this, "Invalid food ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 🔄 Fetch food item from backend using Retrofit
        RetrofitClient.getApiService().getFoodItemById(foodId.toString())
            .enqueue(object : Callback<FoodItem> {
                override fun onResponse(call: Call<FoodItem>, response: Response<FoodItem>) {
                    if (response.isSuccessful && response.body() != null) {
                        val food = response.body()!!

                        // ✅ Set data to views
                        binding.foodName.text = food.name
                        binding.foodPrice.text = food.price
                        binding.foodDescription.text = food.description
                        binding.foodIngredients.text = food.ingredients

                        try {
                            val imageBytes = Base64.decode(food.imageBase64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            binding.foodImage.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        // 🛒 Add to Cart Button
                        binding.addToCartButton.setOnClickListener {
                            val request = CartRequest(userId = 1, foodId = food.id.toInt()) // Replace userId with actual login session
                            RetrofitClient.getApiService().addToCart(request)
                                .enqueue(object : Callback<ApiResponse> {
                                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                                        if (response.isSuccessful && response.body()?.success == true) {
                                            Toast.makeText(this@FoodDetailActivity, "${food.name} added to cart", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(this@FoodDetailActivity, "Failed to add to cart", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                                        Toast.makeText(this@FoodDetailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                                    }
                                })
                        }

                    } else {
                        Toast.makeText(this@FoodDetailActivity, "Food item not found", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onFailure(call: Call<FoodItem>, t: Throwable) {
                    Toast.makeText(this@FoodDetailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
