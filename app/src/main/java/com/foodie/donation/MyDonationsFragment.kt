package com.foodie.donation

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodie.foodieapp.R
import com.foodie.foodieapp.databinding.FragmentMyDonationsBinding
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MyDonationsFragment : Fragment() {
    private var _binding: FragmentMyDonationsBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Binding is only valid between onCreateView and onDestroyView")
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: DonationAdapter
    private val donationsList = mutableListOf<Donation>()
    private var filteredDonations = mutableListOf<Donation>()
    private var currentSearchQuery = ""
    private var selectedStatuses = mutableSetOf<String>()
    private var currentSortOrder = SortOrder.DATE_DESC

    enum class SortOrder {
        DATE_ASC, DATE_DESC, STATUS_ASC, STATUS_DESC
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyDonationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        setupRecyclerView()
        setupSearchAndFilter()
        setupSortButton()
        loadDonations()
        
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadDonations()
        }
    }

    private fun setupRecyclerView() {
        adapter = DonationAdapter { donation ->
            showDonationDetails(donation)
        }
        binding.donationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MyDonationsFragment.adapter
            val animation = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down)
            layoutAnimation = animation
        }
    }

    private fun setupSearchAndFilter() {
        // Search functionality
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentSearchQuery = s.toString().trim()
                filterDonations()
            }
        })

        // Filter functionality
        binding.filterChipGroup.setOnCheckedChangeListener { group, checkedId ->
            selectedStatuses.clear()
            group.checkedChipIds.forEach { chipId ->
                val chip = group.findViewById<Chip>(chipId)
                when (chip?.text) {
                    "All" -> selectedStatuses.add("All")
                    "Pending" -> selectedStatuses.add("Pending")
                    "Accepted" -> selectedStatuses.add("Accepted")
                    "Completed" -> selectedStatuses.add("Completed")
                    "Cancelled" -> selectedStatuses.add("Cancelled")
                }
            }
            filterDonations()
        }
    }

    private fun setupSortButton() {
        binding.sortButton.setOnClickListener {
            showSortDialog()
        }
    }

    private fun showSortDialog() {
        val sortOptions = arrayOf(
            "Date (Newest First)",
            "Date (Oldest First)",
            "Status (A-Z)",
            "Status (Z-A)"
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sort By")
            .setItems(sortOptions) { _, which ->
                currentSortOrder = when (which) {
                    0 -> SortOrder.DATE_DESC
                    1 -> SortOrder.DATE_ASC
                    2 -> SortOrder.STATUS_ASC
                    3 -> SortOrder.STATUS_DESC
                    else -> SortOrder.DATE_DESC
                }
                sortDonations()
            }
            .show()
    }

    private fun filterDonations() {
        filteredDonations = donationsList.filter { donation ->
            val matchesSearch = currentSearchQuery.isEmpty() ||
                    donation.foodType.contains(currentSearchQuery, ignoreCase = true) ||
                    donation.description.contains(currentSearchQuery, ignoreCase = true)

            val matchesStatus = selectedStatuses.isEmpty() ||
                    selectedStatuses.contains("All") ||
                    selectedStatuses.contains(donation.status)

            matchesSearch && matchesStatus
        }.toMutableList()

        sortDonations()
    }

    private fun sortDonations() {
        filteredDonations.sortWith(compareBy { donation ->
            when (currentSortOrder) {
                SortOrder.DATE_ASC -> donation.timestamp
                SortOrder.DATE_DESC -> -donation.timestamp
                SortOrder.STATUS_ASC -> donation.status
                SortOrder.STATUS_DESC -> donation.status.reversed()
            }
        })

        adapter.submitList(filteredDonations.toList())
    }

    private fun loadDonations() {
        _binding?.let { safeBinding ->
            safeBinding.progressBar.visibility = View.VISIBLE
            
            db.collection("donations")
                .whereEqualTo("userId", auth.currentUser?.uid)
                .get()
                .addOnSuccessListener { documents ->
                    if (_binding != null) {
                        donationsList.clear()
                        for (document in documents) {
                            val donation = document.toObject(Donation::class.java)
                            donation.id = document.id
                            donationsList.add(donation)
                        }
                        filterDonations()
                        safeBinding.progressBar.visibility = View.GONE
                        safeBinding.swipeRefreshLayout.isRefreshing = false
                    }
                }
                .addOnFailureListener { exception ->
                    if (isAdded && _binding != null) {
                        Toast.makeText(requireContext(), "Error loading donations: ${exception.message}", Toast.LENGTH_SHORT).show()
                        safeBinding.progressBar.visibility = View.GONE
                        safeBinding.swipeRefreshLayout.isRefreshing = false
                    }
                }
        }
    }

    private fun showDonationDetails(donation: Donation) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_donation_details, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .setNegativeButton("Delete") { _, _ ->
                showDeleteConfirmation(donation)
            }
            .create()

        dialogView.findViewById<TextView>(R.id.foodTypeTextView).text = "Food Type: ${donation.foodType}"
        dialogView.findViewById<TextView>(R.id.quantityTextView).text = "Quantity: ${donation.quantity}"
        dialogView.findViewById<TextView>(R.id.pickupTimeTextView).text = "Pickup Time: ${donation.pickupTime}"
        dialogView.findViewById<TextView>(R.id.addressTextView).text = "Address: ${donation.address}"
        dialogView.findViewById<TextView>(R.id.descriptionTextView).text = "Description: ${donation.description}"
        dialogView.findViewById<TextView>(R.id.statusTextView).text = "Status: ${donation.status}"
        
        // Add click listener for the View on Map button
        dialogView.findViewById<Button>(R.id.viewOnMapButton).setOnClickListener {
            try {
                // Get the address from the donation
                val address = donation.address
                
                // Create an Intent to view the location on Google Maps
                val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                
                // Check if Google Maps is installed
                if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    Toast.makeText(requireContext(), "Google Maps app is not installed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error opening map: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun showDeleteConfirmation(donation: Donation) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Donation")
            .setMessage("Are you sure you want to delete this donation?")
            .setPositiveButton("Delete") { _, _ ->
                deleteDonation(donation)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteDonation(donation: Donation) {
        _binding?.let { safeBinding ->
            safeBinding.progressBar.visibility = View.VISIBLE
            
            db.collection("donations")
                .document(donation.id)
                .delete()
                .addOnSuccessListener {
                    if (isAdded && _binding != null) {
                        Toast.makeText(requireContext(), "Donation deleted successfully", Toast.LENGTH_SHORT).show()
                        loadDonations()
                    }
                }
                .addOnFailureListener { e ->
                    if (isAdded && _binding != null) {
                        Toast.makeText(requireContext(), "Error deleting donation: ${e.message}", Toast.LENGTH_SHORT).show()
                        safeBinding.progressBar.visibility = View.GONE
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class Donation(
    var id: String = "",
    val userId: String = "",
    val foodType: String = "",
    val quantity: String = "",
    val pickupTime: Date = Date(),
    val address: String = "",
    val description: String = "",
    val status: String = "",
    val timestamp: Long = 0
) 