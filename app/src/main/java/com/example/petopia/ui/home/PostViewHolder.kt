package com.example.petopia.ui.home

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.example.petopia.R
import com.example.petopia.databinding.RowPostItemBinding
import com.example.petopia.types.PostDisplayItem
import com.example.petopia.types.PostType

class PostViewHolder(
    private val binding: RowPostItemBinding,
    private val onLikeClick: ((PostDisplayItem) -> Unit)?,
    private val onCommentClick: ((PostDisplayItem) -> Unit)?,
    private val onAddCommentClick: ((PostDisplayItem, String) -> Unit)?
) : RecyclerView.ViewHolder(binding.root) {

    private val commentAdapter = CommentAdapter()
    private var currentItem: PostDisplayItem? = null

    init {
        binding.recyclerComments.layoutManager = LinearLayoutManager(binding.root.context)
        binding.recyclerComments.adapter = commentAdapter
        binding.recyclerComments.isNestedScrollingEnabled = false

        binding.layoutLikes.setOnClickListener { currentItem?.let { onLikeClick?.invoke(it) } }
        binding.layoutComments.setOnClickListener { currentItem?.let { onCommentClick?.invoke(it) } }

        binding.buttonPostComment.setOnClickListener {
            val text = binding.editComment.text.toString()
            if (text.isNotBlank()) {
                currentItem?.let { onAddCommentClick?.invoke(it, text) }
                binding.editComment.text.clear()
            }
        }
    }

    fun bind(item: PostDisplayItem) {
        currentItem = item
        val post = item.post

        binding.textAuthorName.text = item.authorName
        binding.textTitle.text = post.title
        binding.textDescription.text = post.content
        binding.textHashtags.text = post.hashtags.joinToString(" ") { if (it.startsWith("#")) it else "#$it" }
        binding.textTimestamp.text = android.text.format.DateUtils.getRelativeTimeSpanString(
            post.createdAt,
            System.currentTimeMillis(),
            android.text.format.DateUtils.MINUTE_IN_MILLIS
        ).toString()

        binding.textLikes.text = binding.root.context.getString(
            R.string.likes_format,
            post.likeCount
        )
        binding.textComments.text = binding.root.context.getString(
            R.string.comments_format,
            item.commentCount
        )

        val petopiaOrange = androidx.core.content.ContextCompat.getColor(binding.root.context, R.color.petopia_orange)
        val defaultColor = android.graphics.Color.BLACK
        
        when (post.postType) {
            PostType.RESCUE -> {
                binding.layoutPostTypeTag.setBackgroundResource(R.drawable.bg_tag_rescue)
                binding.textPostType.text = binding.root.context.getString(R.string.rescue_alert)
                binding.textPostType.setTextColor(android.graphics.Color.parseColor("#C62828"))
                binding.iconPostType.setImageResource(R.drawable.ic_circle_alert)
                binding.iconPostType.setColorFilter(android.graphics.Color.parseColor("#C62828"))
            }
            PostType.KNOWLEDGE -> {
                binding.layoutPostTypeTag.setBackgroundResource(R.drawable.bg_tag_care)
                binding.textPostType.text = binding.root.context.getString(R.string.care_tip)
                binding.textPostType.setTextColor(android.graphics.Color.parseColor("#1565C0"))
                binding.iconPostType.setImageResource(R.drawable.ic_lightbulb)
                binding.iconPostType.setColorFilter(android.graphics.Color.parseColor("#1565C0"))
            }
            PostType.SUPPLIES -> {
                binding.layoutPostTypeTag.setBackgroundResource(R.drawable.bg_tag_equipment)
                binding.textPostType.text = binding.root.context.getString(R.string.equipment_donation)
                binding.textPostType.setTextColor(android.graphics.Color.parseColor("#00695C"))
                binding.iconPostType.setImageResource(R.drawable.ic_package)
                binding.iconPostType.setColorFilter(android.graphics.Color.parseColor("#00695C"))
            }
        }
        binding.iconPostType.visibility = View.VISIBLE

        if (post.imageUrl != null && post.imageUrl.isNotEmpty()) {
            binding.cardPostImage.visibility = View.VISIBLE
            Picasso.get()
                .load(post.imageUrl)
                .placeholder(R.drawable.bg_stub_post_image)
                .error(R.drawable.bg_stub_post_image)
                .into(binding.imagePost)
        } else {
            binding.cardPostImage.visibility = View.GONE
        }
        
        binding.imageAuthor.setImageResource(R.drawable.bg_stub_avatar)
        if (!item.authorProfileImageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(item.authorProfileImageUrl)
                .placeholder(R.drawable.bg_stub_avatar)
                .error(R.drawable.bg_stub_avatar)
                .into(binding.imageAuthor)
        }
        binding.imageCurrentUser.setImageResource(R.drawable.bg_stub_avatar)

        commentAdapter.submitList(item.previewComments)

        binding.layoutCommentSection.visibility = if (item.isCommentsVisible) View.VISIBLE else View.GONE
        binding.iconComment.setColorFilter(if (item.isCommentsVisible) petopiaOrange else defaultColor)
        binding.textComments.setTextColor(if (item.isCommentsVisible) petopiaOrange else defaultColor)

        if (item.isLiked) {
            binding.iconLike.setImageResource(R.drawable.ic_heart_filled)
            binding.iconLike.setColorFilter(android.graphics.Color.RED)
            binding.textLikes.setTextColor(android.graphics.Color.RED)
        } else {
            binding.iconLike.setImageResource(R.drawable.ic_heart_outline)
            binding.iconLike.setColorFilter(defaultColor)
            binding.textLikes.setTextColor(defaultColor)
        }
    }
}
