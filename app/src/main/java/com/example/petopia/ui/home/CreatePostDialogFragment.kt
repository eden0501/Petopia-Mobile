package com.example.petopia.ui.home

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels

import com.example.petopia.R
import com.example.petopia.data.model.PostType
import com.example.petopia.databinding.FragmentCreatePostBinding

class CreatePostDialogFragment : DialogFragment() {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreatePostViewModel by viewModels()

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
        binding.editHashtags.addTextChangedListener { viewModel.setHashtags(it?.toString() ?: "") }

        binding.btnCreatePost.setOnClickListener {
            viewModel.submitPost()
            dismiss()
        }
        
        binding.layoutUpload.setOnClickListener {
        }
    }

    private fun observeViewModel() {
        viewModel.postType.observe(viewLifecycleOwner) { type ->
            updateTabSelection(type)
        }

        viewModel.imageUrl.observe(viewLifecycleOwner) { url ->
            if (url != null) {
                binding.layoutUpload.visibility = View.GONE
                binding.imagePreview.visibility = View.VISIBLE
                // binding.imagePreview.setImageURI(Uri.parse(url)) 
            } else {
                binding.layoutUpload.visibility = View.VISIBLE
                binding.imagePreview.visibility = View.GONE
            }
        }
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

        binding.textRescueTab.setTypeface(null, android.graphics.Typeface.NORMAL)
        binding.textCareTab.setTypeface(null, android.graphics.Typeface.NORMAL)
        binding.textSuppliesTab.setTypeface(null, android.graphics.Typeface.NORMAL)

        when (selectedType) {
            PostType.RESCUE -> {
                binding.tabRescue.background = activeBg
                binding.textRescueTab.setTextColor(textColorActive)
                binding.textRescueTab.setTypeface(null, android.graphics.Typeface.BOLD)
                
                binding.cardAlertBox.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.rescue_bg))
                binding.cardAlertBox.strokeColor = ContextCompat.getColor(requireContext(), R.color.rescue_border)
                binding.textAlertMessage.text = "Report urgent animal distress situations that need immediate community help"
                binding.textAlertMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.rescue_red))
            }
            PostType.KNOWLEDGE -> {
                binding.tabCareTips.background = activeBg
                binding.textCareTab.setTextColor(textColorActive)
                binding.textCareTab.setTypeface(null, android.graphics.Typeface.BOLD)
                
                binding.cardAlertBox.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.care_bg))
                binding.cardAlertBox.strokeColor = ContextCompat.getColor(requireContext(), R.color.care_border)
                binding.textAlertMessage.text = "Share your expertise and knowledge to help others care for their pets"
                binding.textAlertMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.care_blue))
            }
            PostType.SUPPLIES -> {
                binding.tabSupplies.background = activeBg
                binding.textSuppliesTab.setTextColor(textColorActive)
                binding.textSuppliesTab.setTypeface(null, android.graphics.Typeface.BOLD)
                
                binding.cardAlertBox.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.supplies_bg))
                binding.cardAlertBox.strokeColor = ContextCompat.getColor(requireContext(), R.color.supplies_border)
                binding.textAlertMessage.text = "Offer pet equipment and supplies for donation to help animals in need"
                binding.textAlertMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.supplies_green))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
