package com.example.foodie.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodie.R
import com.example.foodie.adapter.MenuAdapter
import com.example.foodie.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: MenuAdapter

    private val originalMenuFoodName = listOf("Pancakes", "Sandwiches", "Momos", "Burgers", "Pancakes", "Sandwiches", "Momos", "Burgers")
    private val originalMenuItemPrice = listOf("$5", "$8", "$9", "$10", "$5", "$8", "$9", "$10")
    private val originalMenuImage = listOf(
        R.drawable.menu1,
        R.drawable.menu2,
        R.drawable.menu3,
        R.drawable.menu4,
        R.drawable.menu1,
        R.drawable.menu2,
        R.drawable.menu3,
        R.drawable.menu4
    )

    private val filterMenuFoodName = mutableListOf<String>()
    private val filterMenuItemPrice = mutableListOf<String>()
    private val filterMenuItemImage = mutableListOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        adapter = MenuAdapter(filterMenuFoodName, filterMenuItemPrice, filterMenuItemImage)
        binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.menuRecyclerView.adapter = adapter

        // Setup search functionality
        setupSearchView()

        // Show all menu items initially
        showAllMenu()

        return binding.root
    }

    private fun showAllMenu() {
        filterMenuFoodName.clear()
        filterMenuItemPrice.clear()
        filterMenuItemImage.clear()

        filterMenuFoodName.addAll(originalMenuFoodName)
        filterMenuItemPrice.addAll(originalMenuItemPrice)
        filterMenuItemImage.addAll(originalMenuImage) //

        adapter.notifyDataSetChanged()
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

    private fun filterMenuItems(query: String?) {
        if (query.isNullOrEmpty()) {
            showAllMenu()
            return
        }

        filterMenuFoodName.clear()
        filterMenuItemPrice.clear()
        filterMenuItemImage.clear()

        originalMenuFoodName.forEachIndexed { index, foodName ->
            if (foodName.contains(query, ignoreCase = true)) {
                filterMenuFoodName.add(foodName)
                filterMenuItemPrice.add(originalMenuItemPrice[index])
                filterMenuItemImage.add(originalMenuImage[index]) //
            }
        }

        adapter.notifyDataSetChanged()
    }
}
