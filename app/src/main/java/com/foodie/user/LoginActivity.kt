package com.foodie.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.foodie.foodieapp.R
import com.foodie.foodieapp.databinding.ActivityLoginBinding
import com.foodie.user.network.ApiService
import com.foodie.user.network.AuthResponse
import com.foodie.user.network.GoogleSignInRequest
import com.foodie.user.network.RetrofitClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.foodie.user.network.LoginRequest
import com.foodie.user.network.LoginResponse

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Google Sign-In config
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Navigate to Signup
        binding.donthavebutton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        // Login with email & password
        binding.loginbutton.setOnClickListener {
            val email = binding.editTextTextEmailAddress.text.toString().trim()
            val password = binding.editTextTextPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Google Sign-In
        binding.googleLbutton.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.d("LoginActivity", "Google Sign-In success! Account: ${account?.email}")

            val googleIdToken = account?.idToken
            if (googleIdToken != null) {
                sendGoogleIdTokenToServer(googleIdToken)
            } else {
                Toast.makeText(this, "Could not retrieve Google ID token.", Toast.LENGTH_SHORT).show()
            }

        } catch (e: ApiException) {
            Log.e("LoginActivity", "Google Sign-In failed: ${e.statusCode}", e)
            Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendGoogleIdTokenToServer(idToken: String) {
        Log.d("LoginActivity", "Sending Google ID token to server: $idToken")

        val apiService: ApiService = RetrofitClient.getApiService()
        val googleSignInRequest = GoogleSignInRequest(idToken)

        apiService.googleSignIn(googleSignInRequest).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse?.success == true && authResponse.sessionToken != null && authResponse.userId != null) {
                        // Save the session token and user ID
                        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("session_token", authResponse.sessionToken)
                            putInt("user_id", authResponse.userId)
                            apply()
                        }

                        Toast.makeText(this@LoginActivity, "Google Sign-In successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, UserMainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, authResponse?.message ?: "Google Sign-In failed on server.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("LoginActivity", "Google Sign-In failed on server. Code: ${response.code()} Message: ${response.message()}")
                    Toast.makeText(this@LoginActivity, "Google Sign-In failed on server.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Log.e("LoginActivity", "API call for Google Sign-In failed: ${t.message}", t)
                Toast.makeText(this@LoginActivity, "Network error during Google Sign-In.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loginUser(email: String, password: String) {
        Log.d("LoginActivity", "Logging in with Email: $email")

        val apiService: ApiService = RetrofitClient.getApiService()
        val loginRequest = LoginRequest(email, password)

        apiService.loginUser(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null && loginResponse.user != null) {
                        Log.d("LoginActivity", "Login success! User email: ${loginResponse.user.email}")
                        Toast.makeText(this@LoginActivity, "Welcome ${loginResponse.user.email}", Toast.LENGTH_LONG).show()

                        // Debug: Log the user_id received from the server
                        Log.d("LoginActivity", "Received user_id from server: ${loginResponse.user.id}")

                        // ✅ Save user_id into SharedPreferences
                        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putInt("user_id", loginResponse.user.id) // user.id must be Int
                            apply()
                        }

                        // Debug: Log the user_id stored in SharedPreferences
                        val savedUserId = sharedPref.getInt("user_id", -1)
                        Log.d("LoginActivity", "Stored user_id in SharedPreferences: $savedUserId")

                        // Go to main activity
                        val intent = Intent(this@LoginActivity, UserMainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, loginResponse?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("LoginActivity", "Login failed. Code: ${response.code()} Message: ${response.message()}")
                    Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("LoginActivity", "API call failed: ${t.message}", t)
                Toast.makeText(this@LoginActivity, "Login failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
