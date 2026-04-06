package com.example.petopia.ui.profile

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.petopia.R
import com.example.petopia.databinding.FragmentEditProfileBinding
import java.util.Calendar

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditProfileViewModel by viewModels {
        EditProfileViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAppBar()
        setupBottomNav()
        setupListeners()
        observeViewModel()
    }

    private fun setupAppBar() {
        val toolbar = binding.includeAppBar.topAppBar
        toolbar.title = getString(R.string.app_name)
    }

    private fun setupBottomNav() {
        val includeNav = binding.includeBottomNav
        val orange = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.petopia_orange)
        val gray = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.gray)

        includeNav.iconHome.setColorFilter(gray)
        includeNav.textHome.setTextColor(gray)
        includeNav.iconProfile.setColorFilter(orange)
        includeNav.textProfile.setTextColor(orange)

        // Hide the FAB on edit profile page
        includeNav.fabAddPost.visibility = View.GONE

        includeNav.navHome.setOnClickListener {
            findNavController().navigate(R.id.action_editProfile_to_home)
        }
        includeNav.navProfile.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupListeners() {
        binding.etOwnerSince.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                R.style.DatePickerTheme,
                { _, year, month, day ->
                    binding.etOwnerSince.setText("$day/${month + 1}/$year")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSave.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            if (username.isEmpty()) {
                Toast.makeText(context, "Username is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val petsCount = binding.etPetCount.text.toString().toIntOrNull() ?: 0
            val ownerSince = binding.etOwnerSince.text.toString().ifBlank { null }
            viewModel.saveProfile(username, petsCount, ownerSince)
        }

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun showDeleteDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_delete_account)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<View>(R.id.btnClose).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnKeepAccount).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnConfirmDelete).setOnClickListener {
            dialog.dismiss()
            viewModel.deleteAccount()
        }

        dialog.show()
    }

    private fun observeViewModel() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.etUsername.setText(it.username)
                binding.etPetCount.setText(it.petsCount.toString())
                binding.etOwnerSince.setText(it.petOwnerSince ?: "")
            }
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    Toast.makeText(context, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(context, it.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    Toast.makeText(context, getString(R.string.account_deleted), Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_editProfile_to_auth)
                } else {
                    Toast.makeText(context, it.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
