package com.example.petopia.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.petopia.R
import com.example.petopia.data.PostDisplayItem
import com.example.petopia.data.PostType
import com.example.petopia.databinding.RowPostItemBinding

class PostAdapter(
    private val onLikeClick: ((PostDisplayItem) -> Unit)? = null,
    private val onCommentClick: ((PostDisplayItem) -> Unit)? = null,
    private val onShareClick: ((PostDisplayItem) -> Unit)? = null
) : ListAdapter<PostDisplayItem, PostAdapter.PostViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = RowPostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PostViewHolder(private val binding: RowPostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val commentAdapter = CommentAdapter()

        fun bind(item: PostDisplayItem) {
            val post = item.post

            binding.textAuthorName.text = post.authorName
            binding.textTitle.text = post.title
            binding.textDescription.text = post.description
            binding.textHashtags.text = post.hashtags.joinToString(" ")
            binding.textTimestamp.text = "about 2 months ago"

            binding.textLikes.text = binding.root.context.getString(
                R.string.likes_format,
                item.likeCount
            )
            binding.textComments.text = binding.root.context.getString(
                R.string.comments_format,
                item.commentCount
            )

            binding.layoutPostTypeTag.setBackgroundResource(when (post.postType) {
                PostType.RESCUE -> R.drawable.bg_rescue_alert
                PostType.KNOWLEDGE -> R.drawable.bg_care_tip_chip
                PostType.SUPPLIES -> R.drawable.bg_equipment_chip
            })

            binding.textPostType.text = when (post.postType) {
                PostType.RESCUE -> binding.root.context.getString(R.string.rescue_alert)
                PostType.KNOWLEDGE -> binding.root.context.getString(R.string.care_tip)
                PostType.SUPPLIES -> binding.root.context.getString(R.string.equipment_donation)
            }

            binding.iconPostType.visibility = if (post.postType == PostType.RESCUE) {
                View.VISIBLE
            } else {
                View.GONE
            }

            binding.imagePost.visibility = View.VISIBLE
            binding.imagePost.setImageDrawable(null)
            binding.imagePost.setBackgroundResource(R.drawable.bg_stub_post_image)

            binding.imageAuthor.setImageDrawable(null)
            binding.imageAuthor.setBackgroundResource(R.drawable.bg_stub_avatar)

            binding.recyclerComments.layoutManager = LinearLayoutManager(binding.root.context)
            binding.recyclerComments.adapter = commentAdapter
            binding.recyclerComments.isNestedScrollingEnabled = false
            commentAdapter.submitList(item.previewComments)

            binding.recyclerComments.visibility = if (item.previewComments.isEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }

            binding.imageCurrentUser.setImageDrawable(null)
            binding.imageCurrentUser.setBackgroundResource(R.drawable.bg_stub_avatar)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PostDisplayItem>() {
        override fun areItemsTheSame(old: PostDisplayItem, new: PostDisplayItem): Boolean =
            old.post.id == new.post.id

        override fun areContentsTheSame(old: PostDisplayItem, new: PostDisplayItem): Boolean =
            old == new
    }
}
