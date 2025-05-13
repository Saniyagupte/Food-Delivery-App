package com.foodie.user.Fragment

import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.foodie.foodieapp.R
import com.foodie.foodieapp.databinding.FragmentProfileBinding
import com.foodie.user.OrderPlacedBottomSheet
import com.foodie.user.network.ApiResponse
import com.foodie.user.network.CartItem
import com.foodie.user.network.OrderItem
import com.foodie.user.network.PlaceOrderRequest
import com.foodie.user.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {
    private var totalAmount: Int = 0
    private var userId: Int = -1
    private lateinit var binding: FragmentProfileBinding
    private var cartItems: List<CartItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            totalAmount = it.getInt("total_amount", 0)
            userId = getUserIdFromSession()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        cartItems = getCartFromLocal()

        val textToDisplay = "Payable: $ $totalAmount"
        val spannableString = SpannableString(textToDisplay)
        val redColorSpan = ForegroundColorSpan(
            ContextCompat.getColor(requireContext(), R.color.red)
        )
        spannableString.setSpan(redColorSpan, 0, textToDisplay.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.textViewAmount.text = spannableString

        // Initially disable the place order button
        binding.placeOrderButton.isEnabled = false
        binding.placeOrderButton.alpha = 0.5f // Optionally visually indicate it's disabled

        // Add TextWatchers to the input fields
        binding.editTextName.addTextChangedListener(textWatcher)
        binding.editTextAddress.addTextChangedListener(textWatcher)
        binding.editTextPhone.addTextChangedListener(textWatcher)

        // Set click listener for the "Place Order" button
        binding.placeOrderButton.setOnClickListener {
            placeOrder()
        }

        return binding.root
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val name = binding.editTextName.text.toString().trim()
            val address = binding.editTextAddress.text.toString().trim()
            val phone = binding.editTextPhone.text.toString().trim()

            // Enable the button only if all fields are not empty
            val isAllFieldsFilled = name.isNotEmpty() && address.isNotEmpty() && phone.isNotEmpty()
            binding.placeOrderButton.isEnabled = isAllFieldsFilled
            binding.placeOrderButton.alpha = if (isAllFieldsFilled) 1.0f else 0.5f
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    private fun getCartFromLocal(): List<CartItem> {
        val sharedPref = requireContext().getSharedPreferences("cart_data", MODE_PRIVATE)
        val gson = com.google.gson.Gson()
        val json = sharedPref.getString("cart_items", null)
        val type = object : com.google.gson.reflect.TypeToken<List<CartItem>>() {}.type
        return if (json != null) gson.fromJson(json, type) else emptyList()
    }

    private fun getUserIdFromSession(): Int {
        val sharedPref = requireContext().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
        return sharedPref.getInt("user_id", -1)
    }

    private fun placeOrder() {
        if (userId == -1) {
            Toast.makeText(requireContext(), "Invalid user. Please log in.", Toast.LENGTH_SHORT).show()
            return
        }

        if (cartItems.isEmpty()) {
            Toast.makeText(requireContext(), "Your cart is empty.", Toast.LENGTH_SHORT).show()
            return
        }

        val name = binding.editTextName.text.toString().trim()
        val address = binding.editTextAddress.text.toString().trim()
        val phone = binding.editTextPhone.text.toString().trim()

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all delivery details.", Toast.LENGTH_SHORT).show()
            return
        }

        val orderItems = cartItems.map {
            OrderItem(it.foodItem.id, it.quantity)
        }

        val orderRequest = PlaceOrderRequest(userId, orderItems) // Include delivery details

        RetrofitClient.getApiService().placeOrder(orderRequest)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    Log.d("PlaceOrder", "Response Code: ${response.code()}")
                    Log.d("PlaceOrder", "Is Successful: ${response.isSuccessful}")
                    Log.d("PlaceOrder", "Raw Response Body: ${response.body()}")

                    if (response.isSuccessful && response.body()?.success == true) {
                        val orderPlacedBottomSheet = OrderPlacedBottomSheet()
                        orderPlacedBottomSheet.show(requireActivity().supportFragmentManager, "orderPlacedBottomSheet")
                        // Optionally clear the cart locally after a successful order
                        clearLocalCart()
                    } else {
                        Log.e("PlaceOrder", "Error placing the order - Response Body: ${response.body()}")
                        Toast.makeText(requireContext(), "Error placing the order", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                    Log.e("SearchFragment", "Network error: ${t.message}", t)
                }
            })
    }

    private fun clearLocalCart() {
        val sharedPref = requireContext().getSharedPreferences("cart_data", MODE_PRIVATE)
        sharedPref.edit().remove("cart_items").apply()
        // Optionally, you might want to navigate the user back to the food listing screen
        // findNavController().navigate(R.id.action_profileFragment_to_someOtherFragment)
    }
}