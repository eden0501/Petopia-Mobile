package com.example.petopia.ui.home

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.graphics.ImageDecoder
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels

import com.example.petopia.R
import com.example.petopia.base.Constants
import com.example.petopia.types.PostType
import com.example.petopia.databinding.FragmentCreatePostBinding

class CreatePostDialogFragment : DialogFragment() {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreatePostViewModel by viewModels {
        CreatePostViewModelFactory(requireContext())
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

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }

        binding.tabRescue.setOnClickListener { viewModel.setPostType(PostType.RESCUE) }
        binding.tabCareTips.setOnClickListener { viewModel.setPostType(PostType.KNOWLEDGE) }
        binding.tabSupplies.setOnClickListener { viewModel.setPostType(PostType.SUPPLIES) }

        binding.editTitle.addTextChangedListener { viewModel.setTitle(it?.toString() ?: "") }
        binding.editDescription.addTextChangedListener { viewModel.setContent(it?.toString() ?: "") }
        
        binding.editHashtags.addTextChangedListener(object: TextWatcher {
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
            } else {
                binding.layoutUpload.visibility = View.VISIBLE
                binding.imagePreview.visibility = View.GONE
            }
        }

        viewModel.isUploading.observe(viewLifecycleOwner) { isUploading ->
            binding.btnCreatePost.isEnabled = !isUploading
            binding.btnCreatePost.text = if (isUploading) "Uploading..." else "Create Post"
        }

        viewModel.postCreated.observe(viewLifecycleOwner) { created ->
            if (created) {
                val bundle = Bundle()
                bundle.putBoolean(Constants.ResultKeys.SUCCESS, true)
                parentFragmentManager.setFragmentResult(Constants.ResultKeys.CREATE_POST_RESULT, bundle)
                dismiss()
            }
        }

        viewModel.titleError.observe(viewLifecycleOwner) { error ->
            binding.editTitle.setError(error, if (error != null) getTintedErrorIcon() else null)
        }

        viewModel.contentError.observe(viewLifecycleOwner) { error ->
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
        val bgColor = R.color.bg_orange
        val strokeColor = R.color.petopia_orange
        val textColor = R.color.petopia_orange

        val bgStateList = ContextCompat.getColorStateList(requireContext(), bgColor)
        val strokeStateList = ContextCompat.getColorStateList(requireContext(), strokeColor)
        val textColorStateList = ContextCompat.getColorStateList(requireContext(), textColor)

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

        val currentType = viewModel.postType.value ?: PostType.RESCUE
        val textColor = R.color.petopia_orange
        val bgColor = R.color.bg_orange
        val strokeColor = R.color.petopia_orange

        val textColorStateList = ContextCompat.getColorStateList(requireContext(), textColor)
        chip.chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), bgColor)
        chip.chipStrokeColor = ContextCompat.getColorStateList(requireContext(), strokeColor)
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
