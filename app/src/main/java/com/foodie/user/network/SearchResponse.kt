package com.foodie.user.network

data class SearchResponse(
    val recipe: List<RecipeItem>? // Changed from 'recipes' to 'recipe'
)

data class RecipeItem(
    val name: String?,
    val ingredients: List<String>?,
)
