package com.foodie.admin

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodie.admin.network.Fooditems
import com.foodie.admin.network.RetrofitClient
import com.foodie.foodieapp.databinding.ActivityAllItemAdminBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AllItemActivity : AppCompatActivity() {

    private val binding: ActivityAllItemAdminBinding by lazy {
        ActivityAllItemAdminBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: AddItemAdapter
    private val foodList = ArrayList<Fooditems>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        adapter = AddItemAdapter(foodList)
        binding.MenuRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.MenuRecyclerView.adapter = adapter

        fetchDataFromServer()
    }

    private fun fetchDataFromServer() {
        RetrofitClient.api.getAllFoodItems().enqueue(object : Callback<List<Fooditems>> {
            override fun onResponse(call: Call<List<Fooditems>>, response: Response<List<Fooditems>>) {
                if (response.isSuccessful && response.body() != null) {
                    val items = response.body()!!
                    Log.d("API", "Fetched ${items.size} food items")

                    for (item in items) {
                        Log.d("API_ITEM", "Name: ${item.name}, Price: ${item.price}")
                    }

                    adapter.updateList(response.body()!!)

                } else {
                    Log.e("API", "Response failed: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<Fooditems>>, t: Throwable) {
                Log.e("API", "API call failed: ${t.message}")
            }
        })
    }

}
