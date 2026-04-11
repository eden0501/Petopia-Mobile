package com.example.petopia.ui.home

import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.recyclerview.widget.RecyclerView
import com.example.petopia.R
import com.example.petopia.databinding.RowFactItemBinding
import com.example.petopia.types.HomeItem

class FactViewHolder(
    private val binding: RowFactItemBinding,
    private val onRefreshFactClick: (() -> Unit)?
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.buttonRefreshFact.setOnClickListener { onRefreshFactClick?.invoke() }
    }

    fun bind(item: HomeItem.FactItem) {
        binding.textFactTitle.text = if (item.isDog) {
            binding.root.context.getString(R.string.fun_dog_fact)
        } else {
            binding.root.context.getString(R.string.fun_cat_fact)
        }
        binding.textFactContent.text = item.content
        
        if (item.isLoading) {
            if (binding.buttonRefreshFact.animation == null) {
                val rotate = RotateAnimation(
                    0f, 360f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                ).apply {
                    duration = 1000
                    repeatCount = Animation.INFINITE
                    interpolator = LinearInterpolator()
                }
                binding.buttonRefreshFact.startAnimation(rotate)
            }
            binding.buttonRefreshFact.isClickable = false
        } else {
            binding.buttonRefreshFact.clearAnimation()
            binding.buttonRefreshFact.isClickable = true
        }
    }
}
