package com.example.foodie.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodie.databinding.CartItemBinding

class CartAdapter(
    private val cartItems: MutableList<String>,
    private val cartItemPrices: MutableList<String>,
    private val cartImages: MutableList<Int>
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
            binding.apply {
                val quantity = itemQuantities[position]
                cartfoodname.text = cartItems[position]
                cartitemprice.text = cartItemPrices[position]
                cartimage.setImageResource(cartImages[position])
                cartItemquantity.text = quantity.toString()

                minusbutton.setOnClickListener {
                    decreaseQuantity(position)
                }

                plusbutton.setOnClickListener {
                    increaseQuantity(position)
                }

                deletebutton.setOnClickListener{
                    val itemPosition = adapterPosition
                    if( itemPosition!=RecyclerView.NO_POSITION)
                    {
                        deleteItem(position)
                    }
                }

            }
        }

        private  fun decreaseQuantity(position: Int) {
                    if (itemQuantities[position] > 1) {
                        itemQuantities[position]--;
                        binding.cartItemquantity.text = itemQuantities[position].toString()
                    }
                }

        private  fun increaseQuantity(position : Int) {
                        if (itemQuantities[position] < 10) {
                            itemQuantities[position]++;
                            binding.cartItemquantity.text = itemQuantities[position].toString()
                        }
                    }

        private fun deleteItem( position: Int){
            cartItems.removeAt(position)
            cartImages.removeAt(position)
            cartItemPrices.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position , cartItems.size)
        }

    }
}
