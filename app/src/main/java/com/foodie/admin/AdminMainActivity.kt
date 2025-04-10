package com.foodie.admin

import android.content.Intent
import android.os.Bundle
import com.foodie.foodieapp.R
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.foodie.foodieapp.databinding.ActivityMainAdminBinding

class AdminMainActivity : AppCompatActivity() {

    private val binding : ActivityMainAdminBinding by lazy{
        ActivityMainAdminBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.addMenu.setOnClickListener{
            val intent = Intent( this , AddActivity::class.java)
            startActivity(intent)
        }

        binding.allItemMenu.setOnClickListener{
            val intent = Intent( this , AllItemActivity::class.java)
            startActivity(intent)
        }
    }
}