package com.foodie.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodie.foodieapp.databinding.FragmentMenuBottomSheetBinding
import com.foodie.user.adapter.MenuAdapter
import com.foodie.user.model.FoodItem
import com.foodie.user.network.Fooditems
import com.foodie.user.network.RetrofitClient
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MenuBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentMenuBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuBottomSheetBinding.inflate(inflater, container, false)

        binding.buttonback.setOnClickListener {
            dismiss()
        }

        fetchMenuItems()
        return binding.root
    }

    private fun fetchMenuItems() {
        RetrofitClient.api.getAllFoodItems().enqueue(object : Callback<List<Fooditems>> {
            override fun onResponse(call: Call<List<Fooditems>>, response: Response<List<Fooditems>>) {
                if (response.isSuccessful && response.body() != null) {
                    val adminItems = response.body()!!

                    val userItems = adminItems.map { item ->
                        FoodItem(
                            id = item.id,  // Correctly use the ID from the API response
                            name = item.name,
                            price = item.price,
                            description = "No description available", // Fallback
                            ingredients = "Not listed", // Fallback
                            imageBase64 = item.imageBase64
                        )
                    }

                    val adapter = MenuAdapter(userItems)
                    binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                    binding.menuRecyclerView.adapter = adapter

                    // Handle item click to navigate to FoodDetailActivity
                    adapter.setOnItemClickListener { foodItem ->
                        val intent = Intent(requireContext(), FoodDetailActivity::class.java)
                        intent.putExtra("id", foodItem.id)  // Pass the ID to FoodDetailActivity
                        startActivity(intent)
                    }

                } else {
                    Toast.makeText(context, "Failed to load menu items", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Fooditems>>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
