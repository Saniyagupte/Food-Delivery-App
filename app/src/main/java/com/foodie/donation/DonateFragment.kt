package com.foodie.donation

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.foodie.foodieapp.R
import com.foodie.foodieapp.databinding.FragmentDonateBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class DonateFragment : Fragment() {
    private var _binding: FragmentDonateBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDonateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        setupViews()
    }

    private fun setupViews() {
        binding.submitButton.setOnClickListener {
            submitDonation()
        }

        binding.pickupTimeEditText.setOnClickListener {
            showDateTimePicker()
        }

        // Add text change listeners for real-time validation
        binding.foodTypeEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateFoodType()
        }

        binding.quantityEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateQuantity()
        }

        binding.addressEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateAddress()
        }

        binding.descriptionEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateDescription()
        }
    }

    private fun showDateTimePicker() {
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, day)
                showTimePicker()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        datePicker.show()
    }

    private fun showTimePicker() {
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                selectedDate.set(Calendar.HOUR_OF_DAY, hour)
                selectedDate.set(Calendar.MINUTE, minute)
                updatePickupTimeText()
            },
            selectedDate.get(Calendar.HOUR_OF_DAY),
            selectedDate.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updatePickupTimeText() {
        val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        binding.pickupTimeEditText.setText(sdf.format(selectedDate.time))
    }

    private fun validateInputs(): Boolean {
        return validateFoodType() &&
                validateQuantity() &&
                validatePickupTime() &&
                validateAddress() &&
                validateDescription()
    }

    private fun validateFoodType(): Boolean {
        val foodType = binding.foodTypeEditText.text.toString().trim()
        return if (foodType.isEmpty()) {
            binding.foodTypeEditText.error = "Food type is required"
            false
        } else {
            binding.foodTypeEditText.error = null
            true
        }
    }

    private fun validateQuantity(): Boolean {
        val quantity = binding.quantityEditText.text.toString().trim()
        return if (quantity.isEmpty()) {
            binding.quantityEditText.error = "Quantity is required"
            false
        } else if (quantity.toDoubleOrNull() == null || quantity.toDouble() <= 0) {
            binding.quantityEditText.error = "Please enter a valid quantity"
            false
        } else {
            binding.quantityEditText.error = null
            true
        }
    }

    private fun validatePickupTime(): Boolean {
        val pickupTime = binding.pickupTimeEditText.text.toString().trim()
        return if (pickupTime.isEmpty()) {
            binding.pickupTimeEditText.error = "Pickup time is required"
            false
        } else {
            binding.pickupTimeEditText.error = null
            true
        }
    }

    private fun validateAddress(): Boolean {
        val address = binding.addressEditText.text.toString().trim()
        return if (address.isEmpty()) {
            binding.addressEditText.error = "Address is required"
            false
        } else {
            binding.addressEditText.error = null
            true
        }
    }

    private fun validateDescription(): Boolean {
        val description = binding.descriptionEditText.text.toString().trim()
        return if (description.isEmpty()) {
            binding.descriptionEditText.error = "Description is required"
            false
        } else {
            binding.descriptionEditText.error = null
            true
        }
    }

    private fun submitDonation() {
        if (!validateInputs()) return

        binding.progressBar.visibility = View.VISIBLE
        binding.submitButton.isEnabled = false

        val userId = auth.currentUser?.uid ?: return
        val foodType = binding.foodTypeEditText.text.toString().trim()
        val quantity = binding.quantityEditText.text.toString().trim()
        val pickupTime = binding.pickupTimeEditText.text.toString().trim()
        val address = binding.addressEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()

        val donation = hashMapOf(
            "userId" to userId,
            "foodType" to foodType,
            "quantity" to quantity,
            "pickupTime" to selectedDate.time,
            "address" to address,
            "description" to description,
            "status" to "pending",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("donations")
            .add(donation)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Donation submitted successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_donateFragment_to_myDonationsFragment)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error submitting donation: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.submitButton.isEnabled = true
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 