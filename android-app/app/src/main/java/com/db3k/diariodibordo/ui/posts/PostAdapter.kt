package com.db3k.diariodibordo.ui.posts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.db3k.diariodibordo.R
import com.db3k.diariodibordo.data.model.Post
import com.db3k.diariodibordo.databinding.ItemPostBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Adapter for posts list
 */
class PostAdapter(
    private val onClick: (Post) -> Unit
) : ListAdapter<Post, PostAdapter.PostViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.title.text = post.title
            binding.excerpt.text = post.excerpt ?: ""
            binding.date.text = post.getFormattedDate()
            binding.category.text = post.category ?: ""
            binding.author.text = post.author ?: ""

            // Load cover image
            post.coverImage?.let { imageUrl ->
                binding.coverImage.visibility = View.VISIBLE
                Glide.with(binding.root)
                    .load(imageUrl)
                    .apply(RequestOptions()
                        .placeholder(R.color.ducati_medium_gray)
                        .error(R.color.ducati_medium_gray)
                        .transform(RoundedCorners(12))
                    )
                    .into(binding.coverImage)
            } ?: run {
                binding.coverImage.visibility = View.GONE
            }

            // Tags
            if (post.tags.isNotEmpty()) {
                binding.tagsContainer.visibility = View.VISIBLE
                binding.tagsContainer.removeAllViews()
                post.tags.take(3).forEach { tag ->
                    val chip = com.google.android.material.chip.Chip(binding.root.context).apply {
                        text = tag
                        isClickable = false
                        setTextAppearance(R.style.TextAppearance_DB3K_LabelSmall)
                    }
                    binding.tagsContainer.addView(chip)
                }
            } else {
                binding.tagsContainer.visibility = View.GONE
            }

            // Read indicator
            binding.readIndicator.visibility = if (post.isRead) View.VISIBLE else View.GONE

            // Click listener
            binding.root.setOnClickListener { onClick(post) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.slug == newItem.slug
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}