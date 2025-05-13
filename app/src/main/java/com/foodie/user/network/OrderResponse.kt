package com.foodie.user.network

data class OrderResponse(
    val success: Boolean,  // A simple flag to indicate if the order was placed successfully
    val message: String,  // A message from the server (e.g., "Order placed successfully")
    val orderId: Int? = null // Optionally, the order ID if your backend sends it
)
