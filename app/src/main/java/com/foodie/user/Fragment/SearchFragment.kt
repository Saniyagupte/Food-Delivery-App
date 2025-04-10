package com.foodie.user.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodie.foodieapp.databinding.FragmentSearchBinding
import com.foodie.user.adapter.MenuAdapter
import com.foodie.user.model.FoodItem
import com.foodie.user.network.Fooditems
import com.foodie.user.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: MenuAdapter

    private val originalMenuItems = mutableListOf<FoodItem>()
    private val filteredMenuItems = mutableListOf<FoodItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        adapter = MenuAdapter(filteredMenuItems)
        binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.menuRecyclerView.adapter = adapter

        setupSearchView()
        fetchMenuItemsFromApi()

        return binding.root
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterMenuItems(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterMenuItems(newText)
                return true
            }
        })
    }

    private fun fetchMenuItemsFromApi() {
        RetrofitClient.api.getAllFoodItems().enqueue(object : Callback<List<Fooditems>> {
            override fun onResponse(call: Call<List<Fooditems>>, response: Response<List<Fooditems>>) {
                if (response.isSuccessful && response.body() != null) {
                    val adminList = response.body()!!

                    originalMenuItems.clear()
                    originalMenuItems.addAll(adminList.mapIndexed { index, item ->
                        FoodItem(
                            id = index,
                            name = item.name,
                            price = item.price,
                            description = "No description available",
                            ingredients = "Not listed",
                            imageBase64 = item.imageBase64
                        )
                    })

                    showAllMenu()
                } else {
                    Toast.makeText(requireContext(), "Failed to load items", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Fooditems>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAllMenu() {
        filteredMenuItems.clear()
        filteredMenuItems.addAll(originalMenuItems)
        adapter.notifyDataSetChanged()
    }

    private fun filterMenuItems(query: String?) {
        if (query.isNullOrEmpty()) {
            showAllMenu()
            return
        }

        filteredMenuItems.clear()
        filteredMenuItems.addAll(originalMenuItems.filter {
            it.name.contains(query, ignoreCase = true)
        })
        adapter.notifyDataSetChanged()
    }
}
