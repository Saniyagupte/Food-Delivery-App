package com.example.foodie

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodie.databinding.ActivityLoginBinding
import com.example.foodie.network.ApiService
import com.example.foodie.network.RetrofitClient
import com.example.foodie.network.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // If the user clicks on "Don't have an account?"
        binding.donthavebutton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        // Login button click listener
        binding.loginbutton.setOnClickListener {
            val email = binding.editTextTextEmailAddress.text.toString().trim()
            val password = binding.editTextTextPassword.text.toString().trim()

            // Log email and password input for debugging
            Log.d("LoginActivity", "Entered email: $email")
            Log.d("LoginActivity", "Entered password: $password")

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        // Log the values of email and password before making the API call
        Log.d("LoginActivity", "Logging in with Email: $email, Password: $password")

        val apiService: ApiService = RetrofitClient.getApiService()
        val user = User(email, password)

        // Make API call to loginUser
        apiService.loginUser(user).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                // Log the response details for debugging
                if (response.isSuccessful) {
                    val userResponse = response.body()

                    if (userResponse != null) {
                        Log.d("LoginActivity", "Login success! User email: ${userResponse.email}")
                        Toast.makeText(this@LoginActivity, "Welcome ${userResponse.email}", Toast.LENGTH_LONG).show()

                        // Navigate to MainActivity after login
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.d("LoginActivity", "Response body is null.")
                        Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("LoginActivity", "Login failed with response code: ${response.code()}")
                    Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                // Log the full stack trace to understand the exact error
                Log.e("LoginActivity", "API call failed: ${t.message}", t)
                t.printStackTrace()  // This will log the full stack trace

                // Display a toast with the error message
                Toast.makeText(this@LoginActivity, "Login failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }
}