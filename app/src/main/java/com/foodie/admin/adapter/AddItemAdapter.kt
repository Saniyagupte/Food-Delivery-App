package com.foodie.admin

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.foodie.admin.network.Fooditems
import com.foodie.foodieapp.databinding.ItemItemBinding

class AddItemAdapter(
    private val foodList: ArrayList<Fooditems>
) : RecyclerView.Adapter<AddItemAdapter.AddItemViewHolder>() {

    private val itemQuantities = mutableListOf<Int>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddItemViewHolder {
        val binding = ItemItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddItemViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = foodList.size

    inner class AddItemViewHolder(private val binding: ItemItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val foodItem = foodList[position]

            binding.foodnameTextView.text = foodItem.name
            binding.foodprice.text = foodItem.price
            binding.quantityTextView.text = itemQuantities[position].toString()

            // 🔥 Decode Base64 image
            try {
                val imageBytes = Base64.decode(foodItem.imageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                binding.foodImageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Quantity button handlers
            binding.minusButton.setOnClickListener { decreaseQuantity(position) }
            binding.addButton.setOnClickListener { increaseQuantity(position) }
            binding.deleteButton.setOnClickListener { deleteItem(position) }
        }

        private fun decreaseQuantity(position: Int) {
            if (itemQuantities[position] > 1) {
                itemQuantities[position]--
                binding.quantityTextView.text = itemQuantities[position].toString()
            }
        }

        private fun increaseQuantity(position: Int) {
            if (itemQuantities[position] < 10) {
                itemQuantities[position]++
                binding.quantityTextView.text = itemQuantities[position].toString()
            }
        }

        private fun deleteItem(position: Int) {
            foodList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, foodList.size)
        }

    }

    fun updateList(newList: List<Fooditems>) {
        foodList.clear()
        foodList.addAll(newList)

        itemQuantities.clear()
        itemQuantities.addAll(List(foodList.size) { 1 })

        notifyDataSetChanged()
    }
}
