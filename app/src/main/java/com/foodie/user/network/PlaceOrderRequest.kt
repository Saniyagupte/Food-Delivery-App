package com.foodie.user.network

data class PlaceOrderRequest(
    val userId: Int,
    val orderItems: List<OrderItem>
)
