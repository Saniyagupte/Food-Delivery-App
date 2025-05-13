package com.foodie.user.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.foodie.foodieapp.R
import com.foodie.user.network.SearchItem

// SearchAdapter.kt
class SearchAdapter(private val recipeList: MutableList<SearchItem>) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ingredientNameTextView: TextView = itemView.findViewById(R.id.foodnameTextView)
        val decreaseButton: ImageButton = itemView.findViewById(R.id.minusButton)
        val ingredientQuantityTextView: TextView = itemView.findViewById(R.id.quantityTextView)
        val increaseButton: ImageButton = itemView.findViewById(R.id.addButton)
        // You might need a way to track the quantity of each ingredient.
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.ingredient_item, parent, false) // Use ingredient_cart_item_layout
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = recipeList[position]
        holder.ingredientNameTextView.text = currentItem.name
        holder.ingredientQuantityTextView.text = "0" // Initialize quantity

        holder.increaseButton.setOnClickListener {
            var quantity = holder.ingredientQuantityTextView.text.toString().toInt()
            quantity++
            holder.ingredientQuantityTextView.text = quantity.toString()
            // Implement logic to update cart for this ingredient
        }

        holder.decreaseButton.setOnClickListener {
            var quantity = holder.ingredientQuantityTextView.text.toString().toInt()
            if (quantity > 0) {
                quantity--
                holder.ingredientQuantityTextView.text = quantity.toString()
                // Implement logic to update cart for this ingredient
            }
        }
    }

    override fun getItemCount() = recipeList.size
}