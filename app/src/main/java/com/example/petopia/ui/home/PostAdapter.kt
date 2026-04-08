package com.example.petopia.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.petopia.databinding.RowPostItemBinding
import com.example.petopia.databinding.RowFactItemBinding
import com.example.petopia.types.HomeItem
import com.example.petopia.types.PostDisplayItem

class PostAdapter(
    private val onLikeClick: ((PostDisplayItem) -> Unit)? = null,
    private val onCommentClick: ((PostDisplayItem) -> Unit)? = null,
    private val onAddCommentClick: ((PostDisplayItem, String) -> Unit)? = null,
    private val onRefreshFactClick: (() -> Unit)? = null,
    private val onFactVisible: ((String) -> Unit)? = null
) : ListAdapter<HomeItem, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        private const val VIEW_TYPE_POST = 0
        private const val VIEW_TYPE_FACT = 1

        private val DiffCallback = object : DiffUtil.ItemCallback<HomeItem>() {
            override fun areItemsTheSame(old: HomeItem, new: HomeItem): Boolean {
                return if (old is HomeItem.PostItem && new is HomeItem.PostItem) {
                    old.displayItem.post.id == new.displayItem.post.id
                } else if (old is HomeItem.FactItem && new is HomeItem.FactItem) {
                    old.uniqueId == new.uniqueId
                } else {
                    false
                }
            }

            override fun areContentsTheSame(old: HomeItem, new: HomeItem): Boolean =
                old == new
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HomeItem.PostItem -> VIEW_TYPE_POST
            is HomeItem.FactItem -> VIEW_TYPE_FACT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_POST -> {
                val binding = RowPostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onLikeClick, onCommentClick, onAddCommentClick)
            }
            else -> {
                val binding = RowFactItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                FactViewHolder(binding, onRefreshFactClick)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is PostViewHolder && item is HomeItem.PostItem) {
            holder.bind(item.displayItem)
        } else if (holder is FactViewHolder && item is HomeItem.FactItem) {
            holder.bind(item)
            if (item.isLoading) {
                onFactVisible?.invoke(item.uniqueId)
            }
        }
    }
}
