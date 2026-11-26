package com.db3k.diariodibordo.ui.notifications

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.db3k.diariodibordo.R
import com.db3k.diariodibordo.data.model.Notification
import com.db3k.diariodibordo.databinding.ItemNotificationBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Adapter for notifications list
 */
class NotificationAdapter(
    private val onClick: (Notification) -> Unit,
    private val onLongClick: (Notification) -> Unit
) : ListAdapter<Notification, NotificationAdapter.NotificationViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificationViewHolder(private val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(notification: Notification) {
            binding.title.text = notification.title
            binding.body.text = notification.body
            binding.time.text = notification.getFormattedTime()
            binding.icon.setImageResource(notification.getIconRes())

            // Accent color based on type
            val accentColor = notification.getAccentColor()
            binding.iconContainer.setBackgroundColor(
                androidx.core.content.ContextCompat.getColor(binding.root.context, accentColor)
            )

            // Read/unread styling
            if (notification.isRead) {
                binding.title.setTextColor(Color.parseColor("#B0B0B0"))
                binding.body.setTextColor(Color.parseColor("#808080"))
                binding.readIndicator.visibility = View.GONE
                binding.root.setBackgroundColor(Color.TRANSPARENT)
            } else {
                binding.title.setTextColor(Color.WHITE)
                binding.body.setTextColor(Color.parseColor("#E0E0E0"))
                binding.readIndicator.visibility = View.VISIBLE
                binding.root.setBackgroundColor(Color.parseColor("#1AE31837"))
            }

            // Click listeners
            binding.root.setOnClickListener { onClick(notification) }
            binding.root.setOnLongClickListener { onLongClick(notification); true }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem == newItem
        }
    }
}