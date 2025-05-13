package com.foodie.user.network

data class OrderItem(
    val foodId: Int,  // Only the food ID, no need for the whole FoodItem object
    var quantity: Int // Quantity of the food item
)
