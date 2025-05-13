package com.foodie.user.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodie.foodieapp.databinding.FragmentSearchBinding
import com.foodie.user.adapter.SearchAdapter
import com.foodie.user.network.SearchRequest
import com.foodie.user.network.SearchResponse
import com.foodie.user.network.RetrofitClient
import com.foodie.user.network.SearchItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: SearchAdapter

    // Initialize the list with SearchItem for proper data binding
    private val displayedRecipes = mutableListOf<SearchItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        Log.d("SearchFragment", "onCreateView: Binding inflated")

        // Initialize the adapter with SearchItem
        adapter = SearchAdapter(displayedRecipes)
        Log.d("SearchFragment", "onCreateView: SearchAdapter initialized")

        // Set up RecyclerView with LinearLayoutManager and the adapter
        binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.menuRecyclerView.adapter = adapter
        Log.d("SearchFragment", "onCreateView: RecyclerView setup complete")

        // Set up the SearchView to listen to query text changes
        setupSearchView()
        Log.d("SearchFragment", "onCreateView: SearchView setup initiated")

        return binding.root
    }

    // Setup the SearchView to listen for text changes or submission of a query
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d("SearchFragment", "onQueryTextSubmit: Query submitted - $query")
                query?.let {
                    // Call function to fetch recipes from API based on query
                    fetchRecipesFromApi(it)
                }
                // Important: Return true to indicate that the query has been handled
                binding.searchView.clearFocus() // Optional: Hide the keyboard after submission
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Do nothing here, we only want to search on submit
                Log.d("SearchFragment", "onQueryTextChange: Text changed - $newText (ignoring)")
                return false // Return false to let the SearchView handle text changes internally
            }
        })
        Log.d("SearchFragment", "setupSearchView: OnQueryTextListener set on SearchView (submit-only)")
    }

    // Fetch recipes based on search query
    private fun fetchRecipesFromApi(query: String) {
        Log.d("SearchFragment", "fetchRecipesFromApi: Fetching recipes for query - $query")
        val request = SearchRequest(query)
        Log.d("SearchFragment", "fetchRecipesFromApi: SearchRequest created - $request")

        RetrofitClient.api.getRecipes(query).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                Log.d("SearchFragment", "onResponse: API call successful")
                Log.d("SearchFragment", "onResponse: Raw response - ${response}")
                if (response.isSuccessful) {
                    val searchResponse = response.body()
                    Log.d("SearchFragment", "onResponse: Response body - $searchResponse")

                    // Get the first recipe if available
                    val firstRecipe = searchResponse?.recipe?.firstOrNull() // Access 'recipe' array

                    if (firstRecipe != null) {
                        Log.d("SearchFragment", "onResponse: First recipe found - $firstRecipe")

                        // Display the name of the first recipe
                        binding.recipeNameTextView.text = firstRecipe.name
                        binding.recipeNameTextView.visibility = View.VISIBLE

                        // Clear the displayed recipes (we'll only show ingredients now)
                        displayedRecipes.clear()

                        // Convert each ingredient to a SearchItem
                        val ingredientItems = firstRecipe.ingredients?.map { ingredient ->
                            SearchItem(
                                name = ingredient,
                                description = "",
                                ingredients = "",
                                imageBase64 = "",
                                ingredientImageUrls = emptyList()
                            )
                        } ?: emptyList()
                        Log.d("SearchFragment", "onResponse: Converted ingredients to SearchItems - $ingredientItems")

                        displayedRecipes.addAll(ingredientItems)
                        Log.d("SearchFragment", "onResponse: Updated displayedRecipes with ingredients - $displayedRecipes")
                        adapter.notifyDataSetChanged()
                        Log.d("SearchFragment", "onResponse: Adapter notified of data change")

                    } else {
                        // Handle the case where no recipes are found
                        displayedRecipes.clear()
                        adapter.notifyDataSetChanged()
                        binding.recipeNameTextView.visibility = View.GONE
                        Toast.makeText(requireContext(), "No recipes found for this query", Toast.LENGTH_SHORT).show()
                        Log.d("SearchFragment", "onResponse: No recipes found for the query")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("SearchFragment", "onResponse: Failed to load recipes. Code: ${response.code()}, Error: $errorBody")
                    Toast.makeText(requireContext(), "Failed to load recipes", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                Log.e("SearchFragment", "onFailure: API call failed", t)
                Toast.makeText(requireContext(), "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
        Log.d("SearchFragment", "fetchRecipesFromApi: Enqueued Retrofit call")
    }

}