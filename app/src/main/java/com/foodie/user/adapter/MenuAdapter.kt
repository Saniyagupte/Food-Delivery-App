package com.foodie.user.adapter

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.foodie.user.FoodDetailActivity
import com.foodie.user.model.FoodItem
import com.foodie.foodieapp.databinding.MenuItemBinding

class MenuAdapter(private val menuItems: List<FoodItem>) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    private var onItemClickListener: ((FoodItem) -> Unit)? = null

    // Set the item click listener from the fragment/activity
    fun setOnItemClickListener(listener: (FoodItem) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = MenuItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(menuItems[position])
    }

    override fun getItemCount(): Int = menuItems.size

    inner class MenuViewHolder(private val binding: MenuItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FoodItem) {
            binding.menuFoodName.text = item.name
            binding.menuPrice.text = item.price

            try {
                val imageBytes = Base64.decode(item.imageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                binding.menuImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Trigger the click event
            binding.viewItemButton.setOnClickListener {
                onItemClickListener?.invoke(item)  // Invoke the click listener and pass the item
            }
        }
    }
}
