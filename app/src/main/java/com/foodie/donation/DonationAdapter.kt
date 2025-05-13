package com.foodie.donation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.foodie.foodieapp.databinding.ItemDonationBinding
import java.text.SimpleDateFormat
import java.util.*

class DonationAdapter(
    private val onItemClick: (Donation) -> Unit
) : ListAdapter<Donation, DonationAdapter.DonationViewHolder>(DonationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonationViewHolder {
        val binding = ItemDonationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DonationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DonationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DonationViewHolder(
        private val binding: ItemDonationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(donation: Donation) {
            binding.apply {
                foodTypeTextView.text = donation.foodType
                quantityTextView.text = "${donation.quantity} kg"
                timeTextView.text = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
                    .format(donation.pickupTime)
                statusTextView.text = donation.status.replaceFirstChar { it.uppercase() }
                addressTextView.text = donation.address
                descriptionTextView.text = donation.description
            }
        }
    }

    private class DonationDiffCallback : DiffUtil.ItemCallback<Donation>() {
        override fun areItemsTheSame(oldItem: Donation, newItem: Donation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Donation, newItem: Donation): Boolean {
            return oldItem == newItem
        }
    }
} 