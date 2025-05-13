package com.foodie.donation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.foodie.foodieapp.databinding.ItemLeaderboardBinding

// Define the data class first
data class LeaderboardEntry(
    val name: String,
    val donationCount: Int,
    val rank: Int
)

class LeaderboardAdapter : ListAdapter<LeaderboardEntry, LeaderboardAdapter.ViewHolder>(LeaderboardDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderboardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemLeaderboardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: LeaderboardEntry) {
            binding.rankTextView.text = "#${entry.rank}"
            binding.nameTextView.text = entry.name
            binding.donationCountTextView.text = "${entry.donationCount} donations"
        }
    }

    // Make this an object instead of a class
    private object LeaderboardDiffCallback : DiffUtil.ItemCallback<LeaderboardEntry>() {
        override fun areItemsTheSame(oldItem: LeaderboardEntry, newItem: LeaderboardEntry): Boolean {
            return oldItem.name == newItem.name && oldItem.rank == newItem.rank
        }

        override fun areContentsTheSame(oldItem: LeaderboardEntry, newItem: LeaderboardEntry): Boolean {
            return oldItem == newItem
        }
    }
}
