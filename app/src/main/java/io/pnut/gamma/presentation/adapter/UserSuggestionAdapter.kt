package io.pnut.gamma.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.pnut.gamma.databinding.ItemUserSuggestionBinding
import io.pnut.gamma.domain.model.UserSuggestion
import io.pnut.gamma.presentation.util.BindingUtil

class UserSuggestionAdapter(private val onUserClicked: (UserSuggestion) -> Unit) :
    ListAdapter<UserSuggestion, UserSuggestionAdapter.ViewHolder>(UserSuggestionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserSuggestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserSuggestionDiffCallback : DiffUtil.ItemCallback<UserSuggestion>() {
        override fun areItemsTheSame(oldItem: UserSuggestion, newItem: UserSuggestion): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserSuggestion, newItem: UserSuggestion): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(private val binding: ItemUserSuggestionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        @SuppressLint("SetTextI18n")
        fun bind(user: UserSuggestion) {
            binding.usernameTextView.text = "@${user.username}"
            binding.nameTextView.text = user.name
            BindingUtil.glideAvatarSrc(binding.avatarImageView, user.avatarUrl)
            binding.root.setOnClickListener { onUserClicked(user) }
        }
    }
}
