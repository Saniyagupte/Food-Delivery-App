package com.foodie.user.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.foodie.foodieapp.databinding.CartItemBinding
import com.foodie.user.network.CartItem

class CartAdapter(
    private val cartItems: MutableList<CartItem>,
    private val listener: CartActionListener
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    interface CartActionListener {
        fun onIncrease(position: Int)
        fun onDecrease(position: Int)
        fun onDelete(position: Int)
    }

    // ... onCreateViewHolder, getItemCount ...

    inner class CartViewHolder(private val binding: CartItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val item = cartItems[position]
            val foodItem = item.foodItem

            binding.cartfoodname.text = foodItem.name
            binding.cartitemprice.text = "$ ${foodItem.price}"
            binding.cartItemquantity.text = item.quantity.toString()

            try {
                val imageBytes = Base64.decode(foodItem.imageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                binding.cartimage.setImageBitmap(bitmap)
            } catch (_: Exception) {}

            binding.minusbutton.setOnClickListener { listener.onDecrease(position) }
            binding.plusbutton.setOnClickListener { listener.onIncrease(position) }
            binding.deletebutton.setOnClickListener { listener.onDelete(position) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = cartItems.size
}
