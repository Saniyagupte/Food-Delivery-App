package com.foodie.donation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodie.foodieapp.R
import com.foodie.foodieapp.databinding.FragmentSocialBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import android.widget.Toast
import android.provider.ContactsContract
import android.app.Activity
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SocialFragment : Fragment() {
    private var _binding: FragmentSocialBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var achievementsAdapter: AchievementsAdapter

    private fun checkContactPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_CONTACTS),
                CONTACTS_PERMISSION_REQUEST
            )
        } else {
            inviteFriends()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CONTACTS_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    inviteFriends()
                } else {
                    Toast.makeText(requireContext(), "Permission required to access contacts", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSocialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerViews()
        setupShareButtons()
        loadLeaderboard()
        loadAchievements()
    }

    private fun setupRecyclerViews() {
        // Leaderboard setup
        leaderboardAdapter = LeaderboardAdapter()
        binding.leaderboardRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = leaderboardAdapter
        }

        // Achievements setup
        achievementsAdapter = AchievementsAdapter()
        binding.achievementsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = achievementsAdapter
        }
    }

    private fun setupShareButtons() {
        binding.shareDonationButton.setOnClickListener {
            shareDonation()
        }

        binding.inviteFriendsButton.setOnClickListener {
            checkContactPermission()
        }
    }

    private fun shareDonation() {
        val appDistributionLink = "https://appdistribution.firebase.google.com/testerapps/1:594941654682:android:4c34cae8c10c5126529123/releases/7rfd69ph2nc8g"

        val message = """
            🍽️ *Join me in making a difference with Foodie App!*
            
            I just made a food donation through Foodie App. Together, we can help reduce food waste and support those in need.
            
            📱 Download Foodie App (Test Version):
            $appDistributionLink
            
            Features:
            • Easy food donation process
            • Real-time tracking
            • Help reduce food waste
            • Make a positive impact
            
            Join me in creating a hunger-free community! 💚
            
            #FoodieApp #FoodDonation #ReduceWaste #HelpOthers
        """.trimIndent()

        try {
            // Create the sharing intent
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Join me in donating food!")
                putExtra(Intent.EXTRA_TEXT, message)
            }

            // Create a chooser with email apps preferred
            val chooser = Intent.createChooser(shareIntent, "Share via")

            // Add email apps filter
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(
                Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                }
            ))

            startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error sharing: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun inviteFriends() {
        try {
            // Try to open Google Contacts picker
            val contactIntent = Intent(Intent.ACTION_PICK).apply {
                type = ContactsContract.CommonDataKinds.Email.CONTENT_TYPE
            }

            if (contactIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivityForResult(contactIntent, PICK_CONTACT_REQUEST)
            } else {
                // Fallback to general sharing if contacts picker is not available
                shareInvitationMessage()
            }
        } catch (e: Exception) {
            shareInvitationMessage()
        }
    }

    private fun shareInvitationMessage() {
        val appDistributionLink = "https://appdistribution.firebase.google.com/testerapps/1:594941654682:android:4c34cae8c10c5126529123/releases/7rfd69ph2nc8g"

        val message = """
            Hey! Check out Foodie App - it helps reduce food waste while helping those in need.
            
            Download the app here (Test Version):
            $appDistributionLink
            
            Let's make a difference together! 💚
        """.trimIndent()

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Join Foodie App")
            putExtra(Intent.EXTRA_TEXT, message)
        }
        startActivity(Intent.createChooser(shareIntent, "Invite friends via"))
    }

    private fun loadLeaderboard() {
        db.collection("users")
            .orderBy("donationCount", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                val leaderboardEntries = documents.mapIndexed { index, doc ->
                    val data = doc.data
                    LeaderboardEntry(
                        name = data["name"] as? String ?: "Anonymous",
                        donationCount = (data["donationCount"] as? Number)?.toInt() ?: 0,
                        rank = index + 1
                    )
                }
                leaderboardAdapter.submitList(leaderboardEntries)

                // Update user's rank if they're in the top 10
                auth.currentUser?.uid?.let { userId ->
                    val userRank = leaderboardEntries.indexOfFirst {
                        it.name == (documents.find { doc -> doc.id == userId }?.get("name") as? String ?: "Anonymous")
                    } + 1
                    if (userRank > 0) {
                        binding.rankNumber.text = "#$userRank"
                    }
                }
            }
    }

    private fun loadAchievements() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val data = document.data ?: return@addOnSuccessListener
                val donationCount = (data["donationCount"] as? Number)?.toInt() ?: 0

                val achievements = mutableListOf<Achievement>()

                // First Donation Achievement
                achievements.add(Achievement(
                    "First Donation",
                    if (donationCount > 0) "Made your first food donation" else "Make your first food donation",
                    donationCount > 0
                ))

                // Regular Donor Achievement
                achievements.add(Achievement(
                    "Regular Donor",
                    if (donationCount >= 5) "Made 5 or more donations" else "Make 5 donations",
                    donationCount >= 5
                ))

                // Community Hero Achievement
                achievements.add(Achievement(
                    "Community Hero",
                    if (donationCount >= 10) "Made 10 or more donations" else "Make 10 donations",
                    donationCount >= 10
                ))

                achievementsAdapter.submitList(achievements)

                // Update stats
                binding.totalDonationsCount.text = donationCount.toString()
                binding.impactCount.text = (donationCount * 3).toString() // Assuming each donation helps 3 people
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK && intent != null) {
            try {
                val projection = arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS)

                requireActivity().contentResolver.query(
                    intent.data!!,
                    projection,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val emailIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                        val email = cursor.getString(emailIndex)

                        // Send email invitation
                        val appDistributionLink = "https://appdistribution.firebase.google.com/testerapps/1:594941654682:android:4c34cae8c10c5126529123/releases/7rfd69ph2nc8g"

                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$email")
                            putExtra(Intent.EXTRA_SUBJECT, "Join me on Foodie App!")
                            putExtra(Intent.EXTRA_TEXT, """
                                Hey! I'd like to invite you to try Foodie App.
                                
                                It's a great way to help reduce food waste and support those in need.
                                
                                Download the app here (Test Version):
                                $appDistributionLink
                                
                                Looking forward to seeing you there! 💚
                            """.trimIndent())
                        }

                        try {
                            startActivity(emailIntent)
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error accessing contact: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val PICK_CONTACT_REQUEST = 1
        private const val CONTACTS_PERMISSION_REQUEST = 2
    }
}
