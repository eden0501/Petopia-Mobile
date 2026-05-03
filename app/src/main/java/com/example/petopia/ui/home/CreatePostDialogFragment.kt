package com.example.petopia.ui.home

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.graphics.ImageDecoder
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels

import com.example.petopia.R
import com.example.petopia.base.Constants
import com.example.petopia.types.PostType
import com.example.petopia.databinding.FragmentCreatePostBinding
import com.squareup.picasso.Picasso

class CreatePostDialogFragment : DialogFragment() {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreatePostViewModel by viewModels {
        CreatePostViewModelFactory(requireContext())
    }

    companion object {
        private const val ARG_POST_ID = "arg_post_id"
        private const val ARG_POST_TITLE = "arg_post_title"
        private const val ARG_POST_CONTENT = "arg_post_content"
        private const val ARG_POST_IMAGE_URL = "arg_post_image_url"
        private const val ARG_POST_TYPE = "arg_post_type"
        private const val ARG_POST_HASHTAGS = "arg_post_hashtags"
        private const val ARG_POST_AUTHOR_ID = "arg_post_author_id"
        private const val ARG_POST_CREATED_AT = "arg_post_created_at"
        private const val ARG_POST_LIKES = "arg_post_likes"

        fun newEditInstance(
            postId: String,
            title: String,
            content: String,
            imageUrl: String?,
            postType: String,
            hashtags: ArrayList<String>,
            authorId: String,
            createdAt: Long,
            likes: ArrayList<String>
        ): CreatePostDialogFragment {
            return CreatePostDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_POST_ID, postId)
                    putString(ARG_POST_TITLE, title)
                    putString(ARG_POST_CONTENT, content)
                    putString(ARG_POST_IMAGE_URL, imageUrl)
                    putString(ARG_POST_TYPE, postType)
                    putStringArrayList(ARG_POST_HASHTAGS, hashtags)
                    putString(ARG_POST_AUTHOR_ID, authorId)
                    putLong(ARG_POST_CREATED_AT, createdAt)
                    putStringArrayList(ARG_POST_LIKES, likes)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setGravity(Gravity.CENTER)
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, it)
            } else {
                val source = ImageDecoder.createSource(requireContext().contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            }
            viewModel.setImageBitmap(bitmap)
            binding.imagePreview.setImageBitmap(bitmap)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initEditMode()
        setupListeners()
        observeViewModel()
    }

    private fun initEditMode() {
        val postId = arguments?.getString(ARG_POST_ID) ?: return

        val post = com.example.petopia.data.model.Post(
            id = postId,
            title = arguments?.getString(ARG_POST_TITLE) ?: "",
            content = arguments?.getString(ARG_POST_CONTENT) ?: "",
            imageUrl = arguments?.getString(ARG_POST_IMAGE_URL),
            postType = PostType.valueOf(arguments?.getString(ARG_POST_TYPE) ?: PostType.RESCUE.name),
            hashtags = arguments?.getStringArrayList(ARG_POST_HASHTAGS) ?: emptyList(),
            authorId = arguments?.getString(ARG_POST_AUTHOR_ID) ?: "",
            createdAt = arguments?.getLong(ARG_POST_CREATED_AT) ?: 0L,
            likes = arguments?.getStringArrayList(ARG_POST_LIKES) ?: emptyList()
        )

        viewModel.initEditMode(post)

        binding.textTitle.text = getString(R.string.edit_post)
        binding.textSubtitle.text = getString(R.string.edit_post_subtitle)
        binding.btnCreatePost.text = getString(R.string.update_post)

        binding.editTitle.setText(post.title)
        binding.editDescription.setText(post.content)

        if (!post.imageUrl.isNullOrEmpty()) {
            binding.layoutUpload.visibility = View.GONE
            binding.imagePreview.visibility = View.VISIBLE
            Picasso.get()
                .load(post.imageUrl)
                .placeholder(R.drawable.bg_stub_post_image)
                .into(binding.imagePreview)
        }

        post.hashtags.forEach { addHashtagChip(it) }
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }

        binding.tabRescue.setOnClickListener { viewModel.setPostType(PostType.RESCUE) }
        binding.tabCareTips.setOnClickListener { viewModel.setPostType(PostType.KNOWLEDGE) }
        binding.tabSupplies.setOnClickListener { viewModel.setPostType(PostType.SUPPLIES) }

        binding.editTitle.addTextChangedListener { viewModel.setTitle(it?.toString() ?: "") }
        binding.editDescription.addTextChangedListener { viewModel.setContent(it?.toString() ?: "") }

        binding.editHashtags.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: return
                if (text.endsWith(" ") || text.endsWith("\n")) {
                    val rawWord = text.trim()
                    if (rawWord.isNotEmpty()) {
                        addHashtagChip(rawWord)
                    }
                    binding.editHashtags.setText("")
                }
            }
        })

        binding.editHashtags.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val text = binding.editHashtags.text.toString().trim()
                if (text.isNotEmpty()) {
                    addHashtagChip(text)
                    binding.editHashtags.setText("")
                }
                true
            } else {
                false
            }
        }

        binding.btnCreatePost.setOnClickListener {
            val pending = binding.editHashtags.text.toString().trim()
            if (pending.isNotEmpty()) {
                addHashtagChip(pending)
                binding.editHashtags.setText("")
            }
            viewModel.submitPost(requireContext())
        }

        binding.layoutUpload.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.imagePreview.setOnClickListener {
            pickImage.launch("image/*")
        }
    }

    private fun observeViewModel() {
        viewModel.postType.observe(viewLifecycleOwner) { type ->
            updateTabSelection(type)
        }

        viewModel.imageBitmap.observe(viewLifecycleOwner) { bitmap ->
            if (bitmap != null) {
                binding.layoutUpload.visibility = View.GONE
                binding.imagePreview.visibility = View.VISIBLE
            }
        }

        viewModel.isUploading.observe(viewLifecycleOwner) { isUploading ->
            binding.uploadingOverlay.visibility = if (isUploading) View.VISIBLE else View.GONE
            binding.btnCreatePost.isEnabled = !isUploading
            binding.btnCancel.isEnabled = !isUploading
            binding.btnClose.isEnabled = !isUploading
            dialog?.setCancelable(!isUploading)
            if (viewModel.isEditMode) {
                binding.btnCreatePost.text = if (isUploading) getString(R.string.updating) else getString(R.string.update_post)
            } else {
                binding.btnCreatePost.text = if (isUploading) getString(R.string.uploading) else getString(R.string.create_post_button)
            }
        }

        viewModel.postCreated.observe(viewLifecycleOwner) { created ->
            if (created) {
                val bundle = Bundle()
                bundle.putBoolean(Constants.ResultKeys.SUCCESS, true)
                parentFragmentManager.setFragmentResult(Constants.ResultKeys.CREATE_POST_RESULT, bundle)
                dismiss()
            }
        }

        viewModel.titleError.observe(viewLifecycleOwner) { errorResId ->
            val error = errorResId?.let { getString(it) }
            binding.editTitle.setError(error, if (error != null) getTintedErrorIcon() else null)
        }

        viewModel.contentError.observe(viewLifecycleOwner) { errorResId ->
            val error = errorResId?.let { getString(it) }
            binding.editDescription.setError(error, if (error != null) getTintedErrorIcon() else null)
        }
    }

    private fun getTintedErrorIcon(): android.graphics.drawable.Drawable? {
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_circle_alert)
        drawable?.let {
            val size = (18 * resources.displayMetrics.density).toInt()
            it.setBounds(0, 0, size, size)
            androidx.core.graphics.drawable.DrawableCompat.setTint(it, ContextCompat.getColor(requireContext(), R.color.error))
        }
        return drawable
    }

    private fun updateTabSelection(selectedType: PostType) {
        val activeBg = ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_selected)
        val inactiveBg = null
        val textColorActive = ContextCompat.getColor(requireContext(), R.color.gray_900)
        val textColorInactive = ContextCompat.getColor(requireContext(), R.color.gray_400)

        binding.tabRescue.background = inactiveBg
        binding.tabCareTips.background = inactiveBg
        binding.tabSupplies.background = inactiveBg
        binding.textRescueTab.setTextColor(textColorInactive)
        binding.textCareTab.setTextColor(textColorInactive)
        binding.textSuppliesTab.setTextColor(textColorInactive)

        when (selectedType) {
            PostType.RESCUE -> {
                binding.tabRescue.background = activeBg
                binding.textRescueTab.setTextColor(textColorActive)
                binding.cardAlertBox.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.rescue_bg))
                binding.cardAlertBox.strokeColor = ContextCompat.getColor(requireContext(), R.color.rescue_border)
                binding.textAlertMessage.text = getString(R.string.rescue_alert_message)
                binding.textAlertMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.rescue_red))
            }
            PostType.KNOWLEDGE -> {
                binding.tabCareTips.background = activeBg
                binding.textCareTab.setTextColor(textColorActive)
                binding.cardAlertBox.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.care_bg))
                binding.cardAlertBox.strokeColor = ContextCompat.getColor(requireContext(), R.color.care_border)
                binding.textAlertMessage.text = getString(R.string.care_tip_alert_message)
                binding.textAlertMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.care_blue))
            }
            PostType.SUPPLIES -> {
                binding.tabSupplies.background = activeBg
                binding.textSuppliesTab.setTextColor(textColorActive)
                binding.cardAlertBox.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.supplies_bg))
                binding.cardAlertBox.strokeColor = ContextCompat.getColor(requireContext(), R.color.supplies_border)
                binding.textAlertMessage.text = getString(R.string.equipment_donation_alert_message)
                binding.textAlertMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.supplies_green))
            }
        }

        applyChipStyles(selectedType)
    }

    private fun applyChipStyles(type: PostType) {
        val chipGroup = binding.chipGroupHashtags
        val bgStateList = ContextCompat.getColorStateList(requireContext(), R.color.bg_orange)
        val strokeStateList = ContextCompat.getColorStateList(requireContext(), R.color.petopia_orange)
        val textColorStateList = ContextCompat.getColorStateList(requireContext(), R.color.petopia_orange)

        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as com.google.android.material.chip.Chip
            chip.chipBackgroundColor = bgStateList
            chip.chipStrokeColor = strokeStateList
            chip.chipStrokeWidth = resources.displayMetrics.density * 1f
            chip.setTextColor(textColorStateList)
            chip.closeIconTint = textColorStateList
        }
    }

    private fun addHashtagChip(word: String) {
        val displayWord = if (word.startsWith("#")) word else "#$word"
        val chip = com.google.android.material.chip.Chip(requireContext())
        chip.text = displayWord
        chip.tag = word
        chip.isCloseIconVisible = true

        val textColorStateList = ContextCompat.getColorStateList(requireContext(), R.color.petopia_orange)
        chip.chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.bg_orange)
        chip.chipStrokeColor = ContextCompat.getColorStateList(requireContext(), R.color.petopia_orange)
        chip.chipStrokeWidth = resources.displayMetrics.density * 1f
        chip.setTextColor(textColorStateList)
        chip.closeIconTint = textColorStateList

        chip.setOnCloseIconClickListener {
            binding.chipGroupHashtags.removeView(chip)
            updateViewModelHashtags()
        }

        binding.chipGroupHashtags.addView(chip)
        updateViewModelHashtags()
    }

    private fun updateViewModelHashtags() {
        val hashtagsBuilder = StringBuilder()
        for (i in 0 until binding.chipGroupHashtags.childCount) {
            val chip = binding.chipGroupHashtags.getChildAt(i) as com.google.android.material.chip.Chip
            val rawValue = (chip.tag as? String) ?: chip.text.toString()
            hashtagsBuilder.append(rawValue).append(" ")
        }
        viewModel.setHashtags(hashtagsBuilder.toString().trim())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
