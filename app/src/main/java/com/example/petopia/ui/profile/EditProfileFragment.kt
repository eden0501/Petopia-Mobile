package com.example.petopia.ui.profile

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.petopia.R
import com.example.petopia.databinding.FragmentEditProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        setupListeners()
        observeViewModel()
    }

    private fun setupAppBar() {
        val toolbar = binding.includeAppBar.topAppBar
        toolbar.title = getString(R.string.edit_profile)
        toolbar.navigationIcon = null
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

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(R.id.action_editProfile_to_auth)
        }

        binding.btnDeleteAccount.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.delete_account))
                .setMessage(getString(R.string.delete_account_confirm))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                    viewModel.deleteAccount()
                }
                .show()
        }
    }

    private fun observeViewModel() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.etUsername.setText(it.username)
                binding.etEmail.setText(it.email)
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
