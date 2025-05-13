package com.foodie

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.foodie.admin.Splash_screen as AdminSplashScreen
import com.foodie.user.Splash_screen as UserSplashScreen
import com.foodie.donation.SplashActivity as DonationSplashScreen
import com.foodie.foodieapp.R.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        val btnUser = findViewById<Button>(id.btnUser)
        val btnAdmin = findViewById<Button>(id.btnAdmin)
        val btnDonation = findViewById<Button>(id.foodDonationButton)

        btnUser.setOnClickListener {
            // Navigate to User Splash Screen
            val intent = Intent(this, UserSplashScreen::class.java)
            startActivity(intent)
            // Remove finish() to keep MainActivity in back stack
        }

        btnAdmin.setOnClickListener {
            // Navigate to Admin Splash Screen
            val intent = Intent(this, AdminSplashScreen::class.java)
            startActivity(intent)
            // Remove finish() to keep MainActivity in back stack
        }

        btnDonation.setOnClickListener {
            // Navigate to Donation Splash Screen
            val intent = Intent(this, DonationSplashScreen::class.java)
            startActivity(intent)
            // Remove finish() to keep MainActivity in back stack
        }
    }
}



