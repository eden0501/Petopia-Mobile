package com.example.petopia.ui.home

import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.example.petopia.R
import com.example.petopia.databinding.RowPostItemBinding
import com.example.petopia.databinding.PopupPostActionsBinding
import com.example.petopia.types.PostDisplayItem
import com.example.petopia.types.PostType

class PostViewHolder(
    private val binding: RowPostItemBinding,
    private val onLikeClick: ((PostDisplayItem) -> Unit)?,
    private val onCommentClick: ((PostDisplayItem) -> Unit)?,
    private val onAddCommentClick: ((PostDisplayItem, String) -> Unit)?,
    private val onEditClick: ((PostDisplayItem) -> Unit)?,
    private val onDeleteClick: ((PostDisplayItem) -> Unit)?,
    private val currentUserId: String?
) : RecyclerView.ViewHolder(binding.root) {

    private val commentAdapter = CommentAdapter()
    private var currentItem: PostDisplayItem? = null
    private val context get() = binding.root.context

    init {
        binding.recyclerComments.layoutManager = LinearLayoutManager(context)
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

        binding.btnPostMenu.setOnClickListener { anchor ->
            currentItem?.let { item ->
                val popupView = LayoutInflater.from(context).inflate(R.layout.popup_post_actions, null)
                val popupWindow = PopupWindow(
                    popupView,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    true
                )
                popupWindow.elevation = 16f
                popupWindow.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

                val popupBinding = PopupPostActionsBinding.bind(popupView)

                popupBinding.actionEdit.setOnClickListener {
                    popupWindow.dismiss()
                    onEditClick?.invoke(item)
                }
                popupBinding.actionDelete.setOnClickListener {
                    popupWindow.dismiss()
                    onDeleteClick?.invoke(item)
                }

                popupView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                val xOff = -(popupView.measuredWidth - anchor.width)
                popupWindow.showAsDropDown(anchor, xOff, 0)
            }
        }
    }

    fun bind(item: PostDisplayItem) {
        currentItem = item
        val post = item.post

        val isOwner = currentUserId != null && post.authorId == currentUserId
        binding.btnPostMenu.visibility = if (isOwner) View.VISIBLE else View.GONE

        binding.textAuthorName.text = item.authorName
        binding.textTitle.text = post.title
        binding.textDescription.text = post.content
        binding.textHashtags.text = post.hashtags.joinToString(" ") { if (it.startsWith("#")) it else "#$it" }
        binding.textTimestamp.text = android.text.format.DateUtils.getRelativeTimeSpanString(
            post.createdAt,
            System.currentTimeMillis(),
            android.text.format.DateUtils.MINUTE_IN_MILLIS
        ).toString()

        binding.textLikes.text = context.getString(R.string.likes_format, post.likeCount)
        binding.textComments.text = context.getString(R.string.comments_format, item.commentCount)

        val petopiaOrange = ContextCompat.getColor(context, R.color.petopia_orange)
        val defaultColor = ContextCompat.getColor(context, R.color.black)

        when (post.postType) {
            PostType.RESCUE -> {
                binding.layoutPostTypeTag.setBackgroundResource(R.drawable.bg_tag_rescue)
                binding.textPostType.text = context.getString(R.string.rescue_alert)
                binding.textPostType.setTextColor(ContextCompat.getColor(context, R.color.tag_rescue_text))
                binding.iconPostType.setImageResource(R.drawable.ic_circle_alert)
                binding.iconPostType.setColorFilter(ContextCompat.getColor(context, R.color.tag_rescue_text))
            }
            PostType.KNOWLEDGE -> {
                binding.layoutPostTypeTag.setBackgroundResource(R.drawable.bg_tag_care)
                binding.textPostType.text = context.getString(R.string.care_tip)
                binding.textPostType.setTextColor(ContextCompat.getColor(context, R.color.tag_care_text))
                binding.iconPostType.setImageResource(R.drawable.ic_lightbulb)
                binding.iconPostType.setColorFilter(ContextCompat.getColor(context, R.color.tag_care_text))
            }
            PostType.SUPPLIES -> {
                binding.layoutPostTypeTag.setBackgroundResource(R.drawable.bg_tag_equipment)
                binding.textPostType.text = context.getString(R.string.equipment_donation)
                binding.textPostType.setTextColor(ContextCompat.getColor(context, R.color.tag_supplies_text))
                binding.iconPostType.setImageResource(R.drawable.ic_package)
                binding.iconPostType.setColorFilter(ContextCompat.getColor(context, R.color.tag_supplies_text))
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

        val likeRed = ContextCompat.getColor(context, R.color.rescue_red)
        if (item.isLiked) {
            binding.iconLike.setImageResource(R.drawable.ic_heart_filled)
            binding.iconLike.setColorFilter(likeRed)
            binding.textLikes.setTextColor(likeRed)
        } else {
            binding.iconLike.setImageResource(R.drawable.ic_heart_outline)
            binding.iconLike.setColorFilter(defaultColor)
            binding.textLikes.setTextColor(defaultColor)
        }
    }
}
