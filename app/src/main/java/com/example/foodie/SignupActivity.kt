package com.example.foodie

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foodie.databinding.ActivitySignupBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class SignupActivity : AppCompatActivity() {
    private val binding: ActivitySignupBinding by lazy {
        ActivitySignupBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Handle "Already have an account?" button
        binding.alreadyhavebutton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Handle "Create Account" button click
        binding.button6.setOnClickListener {
            val name = binding.editTextTextName.text.toString().trim()
            val email = binding.editTextTextEmailAddress5.text.toString().trim()
            val password = binding.editTextTextPassword3.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                registerUser(name, email, password) // Call function to send data
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Function to send signup data to backend
    private fun registerUser(name: String, email: String, password: String) {
        val client = OkHttpClient()
        val url = "http://192.168.0.101:1234/signup" // Use 10.0.2.2 for localhost on emulator

        // Create JSON body
        val json = JSONObject().apply {
            put("name", name)
            put("email", email)
            put("password", password)
        }

        val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())

        // Build request
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        // Execute HTTP request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@SignupActivity, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@SignupActivity, "Account Created Successfully!", Toast.LENGTH_SHORT).show()

                        // Redirect user to MainActivity after successful signup
                        val intent = Intent(this@SignupActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@SignupActivity, "Signup Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
