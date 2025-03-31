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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 100  // Request code for Google Sign-In

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))  // OAuth Client ID from strings.xml
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // ✅ "Don't have an account?" → Navigate to Signup
        binding.donthavebutton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        // ✅ Login with email & password
        binding.loginbutton.setOnClickListener {
            val email = binding.editTextTextEmailAddress.text.toString().trim()
            val password = binding.editTextTextPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // ✅ Google Sign-In Button
        binding.googleLbutton.setOnClickListener {
            signInWithGoogle()
        }
    }

    // 🔹 Start Google Sign-In Process
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // 🔹 Handle Google Sign-In Result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    // 🔹 Process Google Sign-In Account
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.d("LoginActivity", "Google Sign-In success! Account: ${account?.email}")

            Toast.makeText(this, "Welcome ${account?.displayName}", Toast.LENGTH_SHORT).show()

            // ✅ Navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: ApiException) {
            Log.e("LoginActivity", "Google Sign-In failed: ${e.statusCode}", e)
            Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 🔹 Handle Email & Password Login
    private fun loginUser(email: String, password: String) {
        Log.d("LoginActivity", "Logging in with Email: $email, Password: $password")

        val apiService: ApiService = RetrofitClient.getApiService()
        val user = User(email, password)

        apiService.loginUser(user).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse != null) {
                        Log.d("LoginActivity", "Login success! User email: ${userResponse.email}")
                        Toast.makeText(this@LoginActivity, "Welcome ${userResponse.email}", Toast.LENGTH_LONG).show()

                        // ✅ Navigate to MainActivity
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
                Log.e("LoginActivity", "API call failed: ${t.message}", t)
                Toast.makeText(this@LoginActivity, "Login failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
