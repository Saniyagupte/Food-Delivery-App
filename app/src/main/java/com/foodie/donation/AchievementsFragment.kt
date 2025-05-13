package com.foodie.donation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodie.foodieapp.R
import com.foodie.foodieapp.databinding.FragmentAchievementsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AchievementsFragment : Fragment() {
    private var _binding: FragmentAchievementsBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var achievementsAdapter: AchievementsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAchievementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadAchievements()
    }

    private fun setupRecyclerView() {
        achievementsAdapter = AchievementsAdapter()
        binding.achievementsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = achievementsAdapter
        }
    }

    private fun loadAchievements() {
        val userId = auth.currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE

        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val data = document.data ?: return@addOnSuccessListener
                val achievements = mutableListOf<Achievement>()

                // First Donation Achievement
                if ((data["donationCount"] as? Number)?.toInt() ?: 0 > 0) {
                    achievements.add(Achievement("First Donation", "Made your first food donation", true))
                } else {
                    achievements.add(Achievement("First Donation", "Make your first food donation", false))
                }

                // Regular Donor Achievement
                if ((data["donationCount"] as? Number)?.toInt() ?: 0 >= 5) {
                    achievements.add(Achievement("Regular Donor", "Made 5 or more donations", true))
                } else {
                    achievements.add(Achievement("Regular Donor", "Make 5 donations", false))
                }

                // Community Hero Achievement
                if ((data["donationCount"] as? Number)?.toInt() ?: 0 >= 10) {
                    achievements.add(Achievement("Community Hero", "Made 10 or more donations", true))
                } else {
                    achievements.add(Achievement("Community Hero", "Make 10 donations", false))
                }

                achievementsAdapter.submitList(achievements)
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                // Show error message
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 