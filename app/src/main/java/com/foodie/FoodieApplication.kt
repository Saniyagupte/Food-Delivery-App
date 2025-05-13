package com.foodie

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.foodie.foodieapp.R

class FoodieApplication : Application() {
    companion object {
        private const val TAG = "FoodieApplication"
        private var initialized = false
    }
    
    override fun onCreate() {
        super.onCreate()
        initFirebase()
    }
    
    private fun initFirebase() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                Log.d(TAG, "Initializing Firebase manually")
                
                // Direct initialization using values from your google-services.json
                val options = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyAfxLhwouIV42urrHGih7jnlS6rhDjWgo8") // From your google-services.json
                    .setApplicationId("1:594941654682:android:4c34cae8c10c5126529123") // From your google-services.json
                    .setProjectId("fooddonation-2d9b8") // From your google-services.json
                    .build()
                
                FirebaseApp.initializeApp(this, options)
                
                initialized = true
                Log.d(TAG, "Firebase initialized successfully with manual configuration")
            } else {
                Log.d(TAG, "Firebase already initialized")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase", e)
        }
    }
} 