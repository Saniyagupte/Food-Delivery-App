package com.foodie.admin

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.foodie.admin.network.AddFoodItemResponse
import com.foodie.admin.network.FoodItem
import com.foodie.admin.network.RetrofitClient
import com.foodie.foodieapp.databinding.ActivityAddAdminBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream



class AddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAdminBinding
    private var imageUri: Uri? = null  // ✅ Declare this to track the selected image

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.selectImage.setOnClickListener {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.addItemButton.setOnClickListener {
            uploadDataToServer()
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            imageUri = uri  // ✅ Store URI
            binding.selectedImage.setImageURI(uri)
        }
    }

    private fun encodeImage(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)

        // Resize to max 800px width (maintain aspect ratio)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 800, (bitmap.height * 800) / bitmap.width, true)

        val outputStream = ByteArrayOutputStream()
        // Compress to JPEG with 50% quality
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)

        val compressedBytes = outputStream.toByteArray()
        return Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
    }

    private fun uploadDataToServer() {
        val name = binding.editText1.text.toString()
        val price = binding.editText3.text.toString()
        val desc = binding.editText4.text.toString()
        val ingredients = binding.editText5.text.toString()

        if (name.isBlank() || price.isBlank() || desc.isBlank() || ingredients.isBlank() || imageUri == null) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
            return
        }

        val encodedImage = encodeImage(imageUri!!)
        val foodItem = FoodItem(
            item_name = name,
            item_price = price,
            item_description = desc,
            item_ingredients = ingredients,
            item_imageBase64 = encodedImage
        )

        val api = RetrofitClient.getApiService()

        api.addItem(foodItem).enqueue(object : Callback<AddFoodItemResponse> {
            override fun onResponse(call: Call<AddFoodItemResponse>, response: Response<AddFoodItemResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@AddActivity, "✅ Item added successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AddActivity, " Server Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AddFoodItemResponse>, t: Throwable) {
                Toast.makeText(this@AddActivity, " Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
