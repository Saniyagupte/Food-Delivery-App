package com.foodie.user.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodie.foodieapp.databinding.FragmentCartBinding
import com.foodie.user.adapter.CartAdapter
import com.foodie.user.model.FoodItem
import com.foodie.user.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartFragment : Fragment() {
    private lateinit var binding: FragmentCartBinding
    private lateinit var adapter: CartAdapter
    private val cartItems = mutableListOf<FoodItem>()

    private val userId: Int = 1 // TODO: Replace with actual logged-in user ID from shared preferences or session

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        setupRecyclerView()
        fetchCartItems()
        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = CartAdapter(cartItems)
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.cartRecyclerView.adapter = adapter
    }

    private fun fetchCartItems() {
        RetrofitClient.getApiService().getCartItems(userId)
            .enqueue(object : Callback<List<FoodItem>> {
                override fun onResponse(call: Call<List<FoodItem>>, response: Response<List<FoodItem>>) {
                    if (response.isSuccessful && response.body() != null) {
                        cartItems.clear()
                        cartItems.addAll(response.body()!!)
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<List<FoodItem>>, t: Throwable) {
                    t.printStackTrace()
                }
            })
    }
}
