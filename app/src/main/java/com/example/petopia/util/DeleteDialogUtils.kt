package com.example.petopia.util

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import com.example.petopia.R
import com.example.petopia.databinding.DialogDeletePostBinding

fun Fragment.showDeletePostDialog(onConfirm: () -> Unit) {
    val dialog = Dialog(requireContext())
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    val dialogBinding = DialogDeletePostBinding.inflate(layoutInflater)
    dialog.setContentView(dialogBinding.root)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window?.setLayout(
        (resources.displayMetrics.widthPixels * 0.85).toInt(),
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    dialogBinding.close.setOnClickListener { dialog.dismiss() }
    dialogBinding.btnKeepPost.setOnClickListener { dialog.dismiss() }
    dialogBinding.confirmDelete.setOnClickListener {
        onConfirm()
        dialog.dismiss()
    }

    dialog.show()
}
