package com.foodie.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.foodie.foodieapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.foodie.foodieapp.databinding.OrderPlacedBottomSheetBinding
import com.foodie.user.Fragment.HomeFragment

class OrderPlacedBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: OrderPlacedBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = OrderPlacedBottomSheetBinding.inflate(inflater, container, false)
        val view = binding.root

        // Set the background color to green (or whatever color you prefer)
        view.setBackgroundColor(resources.getColor(R.color.green)) // Adjust this to your green color

        // Set tick image (you can update this as per your icon resource)
        binding.imageViewTick.setImageResource(R.drawable.ic_big_circle)

        // Set the "Go to Home" button action
        binding.btnGoHome.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
            dismiss() // Close the bottom sheet first

            // Then navigate to HomeFragment using Navigation Component
            requireActivity().supportFragmentManager.popBackStack(R.id.homeFragment, 0)
        }


        return view
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Additional setup if required
    }
}
