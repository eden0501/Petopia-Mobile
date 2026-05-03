package com.example.petopia.util

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import com.example.petopia.R

fun Fragment.showDeletePostDialog(onConfirm: () -> Unit) {
    val dialog = Dialog(requireContext())
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(R.layout.dialog_delete_post)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window?.setLayout(
        (resources.displayMetrics.widthPixels * 0.85).toInt(),
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    dialog.findViewById<View>(R.id.btnClose).setOnClickListener { dialog.dismiss() }
    dialog.findViewById<View>(R.id.btnKeepPost).setOnClickListener { dialog.dismiss() }
    dialog.findViewById<View>(R.id.btnConfirmDelete).setOnClickListener {
        onConfirm()
        dialog.dismiss()
    }

    dialog.show()
}
