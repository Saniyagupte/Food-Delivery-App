package com.foodie.user

import com.foodie.foodieapp.R
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.foodie.foodieapp.databinding.ActivityChooseLocationBinding

class ChooseLocationActivity : AppCompatActivity() {
    private val binding: ActivityChooseLocationBinding by lazy {
        ActivityChooseLocationBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        val locationList = arrayOf("Jaipur", "Pune", "Delhi", "Mumbai")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, locationList)
        val autocompletetextview = binding.listOfLocation
        autocompletetextview.setAdapter(adapter)

        // Proceed button click listener
        binding.proceedButton.setOnClickListener {
            val selectedLocation = autocompletetextview.text.toString()

            if (selectedLocation.isNotEmpty()) {
                val intent = Intent(this, UserMainActivity::class.java)
                intent.putExtra("selected_location", selectedLocation) // Passing location
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
