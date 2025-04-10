package com.foodie.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.foodie.admin.network.AdminLoginRequest
import com.foodie.foodieapp.databinding.ActivityLoginAdminBinding
import com.foodie.admin.network.ApiService
import com.foodie.admin.network.AdminLoginResponse
import com.foodie.admin.network.RetrofitClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.foodie.foodieapp.R

class AdminLoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginAdminBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 100  // Request code for Google Sign-In

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))  // OAuth Client ID from strings.xml
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        binding.donthavebutton.setOnClickListener {
            val intent = Intent(this, AdminSignupActivity::class.java)
            startActivity(intent)
       }

        binding.loginbutton.setOnClickListener {
            val email = binding.editTextTextEmailAddress.text.toString().trim()
            val password = binding.editTextTextPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginOwner(email, password)  //
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }


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
            val intent = Intent(this, AdminMainActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: ApiException) {
            Log.e("LoginActivity", "Google Sign-In failed: ${e.statusCode}", e)
            Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 🔹 Handle Email & Password Login
    private fun loginOwner(email: String, password: String) {
        Log.d("LoginActivity", "Logging in with Email: $email, Password: $password")

        val apiService: ApiService = RetrofitClient.getApiService()
        val loginRequest = AdminLoginRequest(email, password)

        apiService.loginOwner(loginRequest).enqueue(object : Callback<AdminLoginResponse> {
            override fun onResponse(call: Call<AdminLoginResponse>, response: Response<AdminLoginResponse>) {
                if (response.isSuccessful) {
                    val ownerResponse = response.body()
                    if (ownerResponse != null) {
                        Log.d("LoginActivity", "Login success! User email: ${ownerResponse.owner?.email}")
                        Toast.makeText(this@AdminLoginActivity, "Welcome ${ownerResponse.owner?.email}", Toast.LENGTH_LONG).show()

                        //  Navigate to MainActivity
                        val intent = Intent(this@AdminLoginActivity, AdminMainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.d("LoginActivity", "Response body is null.")
                        Toast.makeText(this@AdminLoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("LoginActivity", "Login failed with response code: ${response.code()}")
                    Toast.makeText(this@AdminLoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AdminLoginResponse>, t: Throwable) {
                Log.e("LoginActivity", "API call failed: ${t.message}", t)
                Toast.makeText(this@AdminLoginActivity, "Login failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
