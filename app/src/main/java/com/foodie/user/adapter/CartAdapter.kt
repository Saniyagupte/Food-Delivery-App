package com.foodie.user.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.foodie.foodieapp.databinding.CartItemBinding
import com.foodie.user.model.FoodItem

class CartAdapter(
    private val cartItems: MutableList<FoodItem>
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val itemQuantities = MutableList(cartItems.size) { 1 }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = cartItems.size

    inner class CartViewHolder(private val binding: CartItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val item = cartItems[position]
            binding.cartfoodname.text = item.name
            binding.cartitemprice.text = item.price
            binding.cartItemquantity.text = itemQuantities[position].toString()

            try {
                val imageBytes = Base64.decode(item.imageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                binding.cartimage.setImageBitmap(bitmap)
            } catch (_: Exception) {}

            binding.minusbutton.setOnClickListener { decreaseQuantity(position) }
            binding.plusbutton.setOnClickListener { increaseQuantity(position) }
            binding.deletebutton.setOnClickListener { deleteItem(position) }
        }

        private fun decreaseQuantity(position: Int) {
            if (itemQuantities[position] > 1) {
                itemQuantities[position]--
                binding.cartItemquantity.text = itemQuantities[position].toString()
            }
        }

        private fun increaseQuantity(position: Int) {
            if (itemQuantities[position] < 10) {
                itemQuantities[position]++
                binding.cartItemquantity.text = itemQuantities[position].toString()
            }
        }

        private fun deleteItem(position: Int) {
            cartItems.removeAt(position)
            itemQuantities.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, cartItems.size)
        }
    }
}
