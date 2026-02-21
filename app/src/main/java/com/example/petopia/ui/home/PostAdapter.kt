package com.example.petopia.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.petopia.data.Post
import com.example.petopia.data.PostType
import com.example.petopia.databinding.RowPostItemBinding

class PostAdapter : ListAdapter<Post, PostAdapter.PostViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RowPostItemBinding.inflate(inflater, parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PostViewHolder(
        private val binding: RowPostItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.textAuthorName.text = post.authorName
            binding.textTitle.text = post.title
            binding.textDescription.text = post.description
            binding.textHashtags.text = post.hashtags.joinToString(" ")
            binding.textTimestamp.text = "" // TODO: format createdAt

            binding.textPostType.text = when (post.postType) {
                PostType.RESCUE -> "Rescue"
                PostType.KNOWLEDGE -> "Care Tip"
                PostType.SUPPLIES -> "Donation"
            }

            if (post.imageUrl.isNullOrEmpty()) {
                binding.imagePost.visibility = View.GONE
            } else {
                binding.imagePost.visibility = View.VISIBLE
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
    }
}

