package com.foodie.donation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.foodie.foodieapp.databinding.ItemAchievementBinding

// Data class for achievements
data class Achievement(
    val title: String,
    val description: String,
    val isUnlocked: Boolean
)

class AchievementsAdapter : ListAdapter<Achievement, AchievementsAdapter.ViewHolder>(AchievementDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAchievementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemAchievementBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(achievement: Achievement) {
            binding.titleTextView.text = achievement.title
            binding.descriptionTextView.text = achievement.description
            // You might want to update the UI based on isUnlocked status
            binding.root.alpha = if (achievement.isUnlocked) 1.0f else 0.5f
        }
    }

    private class AchievementDiffCallback : DiffUtil.ItemCallback<Achievement>() {
        override fun areItemsTheSame(oldItem: Achievement, newItem: Achievement): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: Achievement, newItem: Achievement): Boolean {
            return oldItem == newItem
        }
    }
}
