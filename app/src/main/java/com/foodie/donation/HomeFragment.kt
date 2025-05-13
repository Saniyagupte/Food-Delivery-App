package com.foodie.donation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.foodie.foodieapp.R
import com.foodie.foodieapp.databinding.FragmentHomeDonationBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeDonationBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Binding is only valid between onCreateView and onDestroyView")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeDonationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
    }

    private fun setupViews() {
        binding.donateButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_donateFragment)
        }

        binding.myDonationsButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_myDonationsFragment)
        }

        binding.socialButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_socialFragment)
        }

        binding.mapButton.setOnClickListener {
            if (isAdded) {
                findNavController().navigate(R.id.action_homeFragment_to_mapFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 