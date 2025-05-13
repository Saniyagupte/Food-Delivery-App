package com.foodie.user.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodie.foodieapp.R
import com.foodie.foodieapp.databinding.FragmentCartBinding
import com.foodie.user.adapter.CartAdapter
import com.foodie.user.network.CartActionResponse
import com.foodie.user.network.CartItem
import com.foodie.user.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartFragment : Fragment() {

    private lateinit var binding: FragmentCartBinding
    private lateinit var adapter: CartAdapter
    private val cartItems = mutableListOf<CartItem>()
    private var userId: Int = -1
    private var totalAmount: Int = 0
    private var deliveryFee: Int = 40 // Example delivery fee, you might fetch this dynamically

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)

        userId = getUserIdFromSession()
        setupRecyclerView()
        fetchCartItems()

        binding.proceedToPayButton.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("total_amount", totalAmount+deliveryFee)
                Log.d("CartFragment", "Preparing to navigate with totalAmount: $totalAmount")
            }
            try {
                findNavController().navigate(R.id.action_cartFragment_to_profileFragment, bundle)
            } catch (e: Exception) {
                Log.e("CartFragment", "Navigation error: ${e.message}", e)
            }
        }

        return binding.root
    }

    private fun saveCartToLocal() {
        if (isAdded) {
            val sharedPref = requireContext().getSharedPreferences("cart_data", MODE_PRIVATE)
            val editor = sharedPref.edit()
            val gson = com.google.gson.Gson()
            val json = gson.toJson(cartItems)
            editor.putString("cart_items", json)
            editor.apply()
        } else {
            Log.e("CartFragment", "Fragment is not attached to the context.")
        }
    }

    private fun getUserIdFromSession(): Int {
        val sharedPref = requireContext().getSharedPreferences("user_session", MODE_PRIVATE)
        return sharedPref.getInt("user_id", -1)
    }

    private fun setupRecyclerView() {
        adapter = CartAdapter(cartItems, object : CartAdapter.CartActionListener {
            override fun onIncrease(position: Int) = increaseQuantity(position)
            override fun onDecrease(position: Int) = decreaseQuantity(position)
            override fun onDelete(position: Int) = deleteItem(position)
        })
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.cartRecyclerView.adapter = adapter
    }

    private fun fetchCartItems() {
        RetrofitClient.getApiService().getCartItems(userId)
            .enqueue(object : Callback<List<CartItem>> {
                override fun onResponse(call: Call<List<CartItem>>, response: Response<List<CartItem>>) {
                    if (response.isSuccessful && response.body() != null) {
                        cartItems.clear()
                        cartItems.addAll(response.body()!!)
                        adapter.notifyDataSetChanged()
                        saveCartToLocal()

                        calculateTotal()
                        updateTotalDisplay()
                        toggleEmptyCartVisibility()
                    }
                }

                override fun onFailure(call: Call<List<CartItem>>, t: Throwable) {
                    t.printStackTrace()
                    toggleEmptyCartVisibility() // Ensure empty state is shown on failure if cart was empty
                }
            })
    }

    private fun increaseQuantity(position: Int) {
        val item = cartItems[position]
        val payload = mapOf("userId" to userId, "foodId" to item.foodItem.id)

        RetrofitClient.getApiService().increaseQuantity(payload)
            .enqueue(object : Callback<CartActionResponse> {
                override fun onResponse(call: Call<CartActionResponse>, response: Response<CartActionResponse>) {
                    if (response.isSuccessful) {
                        item.quantity++
                        adapter.notifyItemChanged(position)
                        calculateTotal()
                        updateTotalDisplay()
                    }
                }

                override fun onFailure(call: Call<CartActionResponse>, t: Throwable) {
                    Log.e("CartFragment", "Increase failed: ${t.message}")
                }
            })
    }

    private fun decreaseQuantity(position: Int) {
        val item = cartItems[position]
        if (item.quantity <= 1) return

        val payload = mapOf("userId" to userId, "foodId" to item.foodItem.id)

        RetrofitClient.getApiService().decreaseQuantity(payload)
            .enqueue(object : Callback<CartActionResponse> {
                override fun onResponse(call: Call<CartActionResponse>, response: Response<CartActionResponse>) {
                    if (response.isSuccessful) {
                        item.quantity--
                        adapter.notifyItemChanged(position)
                        calculateTotal()
                        updateTotalDisplay()
                    }
                }

                override fun onFailure(call: Call<CartActionResponse>, t: Throwable) {
                    Log.e("CartFragment", "Decrease failed: ${t.message}")
                }
            })
    }

    private fun deleteItem(position: Int) {
        val item = cartItems[position]
        val payload = mapOf("userId" to userId, "foodId" to item.foodItem.id)

        RetrofitClient.getApiService().deleteItem(payload)
            .enqueue(object : Callback<CartActionResponse> {
                override fun onResponse(call: Call<CartActionResponse>, response: Response<CartActionResponse>) {
                    if (response.isSuccessful) {
                        cartItems.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        adapter.notifyItemRangeChanged(position, cartItems.size)
                        calculateTotal()
                        updateTotalDisplay()
                        toggleEmptyCartVisibility()
                    }
                }

                override fun onFailure(call: Call<CartActionResponse>, t: Throwable) {
                    Log.e("CartFragment", "Delete failed: ${t.message}")
                }
            })
    }

    private fun calculateTotal() {
        totalAmount = cartItems.sumOf { it.foodItem.price * it.quantity }
    }

    private fun updateTotalDisplay() {
        binding.subtotalText.text = "$${totalAmount}"
        binding.deliveryFeeText.text = "$${deliveryFee}"
        binding.totalAmountText.text = "$${totalAmount + deliveryFee}"
    }

    private fun toggleEmptyCartVisibility() {
        if (cartItems.isEmpty()) {
            binding.emptyCartLayout.visibility = View.VISIBLE
            binding.cartContent.visibility = View.GONE
        } else {
            binding.emptyCartLayout.visibility = View.GONE
            binding.cartContent.visibility = View.VISIBLE
        }
    }
}