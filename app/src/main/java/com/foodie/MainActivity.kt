package com.foodie

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.foodie.admin.AdminLoginActivity
import com.foodie.user.LoginActivity
import com.foodie.foodieapp.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnUser = findViewById<Button>(R.id.btnUser)
        val btnAdmin = findViewById<Button>(R.id.btnAdmin)

        btnUser.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnAdmin.setOnClickListener {
            val intent = Intent(this, AdminLoginActivity::class.java)
            startActivity(intent)
        }
    }
}
