package com.example.petopia.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.petopia.data.CommentPreview
import com.example.petopia.databinding.RowCommentItemBinding

class CommentAdapter : ListAdapter<CommentPreview, CommentAdapter.CommentViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = RowCommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CommentViewHolder(private val binding: RowCommentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: CommentPreview) {
            binding.textCommentAuthor.text = comment.authorName
            binding.textComment.text = comment.text
            binding.textCommentTime.text = comment.timeAgo
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<CommentPreview>() {
        override fun areItemsTheSame(old: CommentPreview, new: CommentPreview): Boolean =
            old.authorName == new.authorName && old.text == new.text

        override fun areContentsTheSame(old: CommentPreview, new: CommentPreview): Boolean =
            old == new
    }
}
