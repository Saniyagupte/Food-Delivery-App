package com.foodie.admin

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.foodie.admin.network.AdminSignupRequest
import com.foodie.admin.network.AdminSignupResponse
import com.foodie.foodieapp.R
import com.foodie.foodieapp.databinding.ActivitySignupAdminBinding
import com.foodie.admin.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminSignupActivity : AppCompatActivity() {
    private val binding: ActivitySignupAdminBinding by lazy {
        ActivitySignupAdminBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 🔽 Set up location dropdown
        val locations = listOf("Pune", "Mumbai", "Delhi", "Hyderabad")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, locations)
        (binding.editTextTextName4.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        // 👉 Already have an account?
        binding.alreadyhavebutton.setOnClickListener {
            startActivity(Intent(this, AdminLoginActivity::class.java))
        }

        // 👉 Create Account
        binding.button3.setOnClickListener {
            val location = binding.editTextTextName4.editText?.text.toString().trim()
            val ownerName = binding.editTextTextName3.text.toString().trim()
            val restaurantName = binding.editTextTextName.text.toString().trim()
            val email = binding.editTextTextEmailAddress5.text.toString().trim()
            val password = binding.editTextTextPassword3.text.toString().trim()

            if (validateInputs(location, ownerName, restaurantName, email, password)) {
                signupOwner(location, ownerName, restaurantName, email, password)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun validateInputs(location: String, ownerName: String, restaurantName: String, email: String, password: String): Boolean {
        return when {
            location.isEmpty() -> {
                showToast("Please choose a location")
                false
            }
            ownerName.isEmpty() -> {
                showToast("Please enter owner's name")
                false
            }
            restaurantName.isEmpty() -> {
                showToast("Please enter restaurant name")
                false
            }
            email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showToast("Please enter a valid email")
                false
            }
            password.length < 6 -> {
                showToast("Password must be at least 6 characters long")
                false
            }
            else -> true
        }
    }

    private fun signupOwner(location: String, ownerName: String, restaurantName: String, email: String, password: String) {
        val apiService = RetrofitClient.getApiService()
        val request = AdminSignupRequest(
            owner_name = ownerName,
            restaurant_name = restaurantName,
            email = email,
            password = password,
            location = location
        )

        apiService.signupOwner(request).enqueue(object : Callback<AdminSignupResponse> {
            override fun onResponse(call: Call<AdminSignupResponse>, response: Response<AdminSignupResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminSignupActivity, "Account created successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@AdminSignupActivity, AdminMainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@AdminSignupActivity, "Signup failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AdminSignupResponse>, t: Throwable) {
                Toast.makeText(this@AdminSignupActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
