package com.foodie.user.network

import com.foodie.user.model.FoodItem

data class CartItem(
    val cartId: Int,
    val userId: Int,
    val foodItem: FoodItem
)
