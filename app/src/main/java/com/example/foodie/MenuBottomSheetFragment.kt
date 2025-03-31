package com.example.foodie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodie.adapter.MenuAdapter
import com.example.foodie.databinding.FragmentMenuBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class MenuBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding:FragmentMenuBottomSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMenuBottomSheetBinding.inflate(inflater, container, false)

        binding.buttonback.setOnClickListener{
            dismiss()
        }

        val menuFoodName = listOf("Pancakes", "Sandwiches", "Momos", "Burgers" , "Pancakes", "Sandwiches", "Momos", "Burgers")
        val menuItemPrice = listOf("$5", "$8", "$9", "$10" , "$5", "$8", "$9", "$10")
        val menuImage = listOf(
            R.drawable.menu1,
            R.drawable.menu2,
            R.drawable.menu3,
            R.drawable.menu4,
            R.drawable.menu1,
            R.drawable.menu2,
            R.drawable.menu3,
            R.drawable.menu4
        )
        val adapter = MenuAdapter(ArrayList(menuFoodName) , ArrayList(menuItemPrice) , ArrayList(menuImage)        )

        binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.menuRecyclerView.adapter = adapter

        return binding.root
    }

    companion object{

    }

}