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
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.petopia.R
import com.example.petopia.databinding.FragmentEditProfileBinding
import java.util.Calendar

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val args: EditProfileFragmentArgs by navArgs()

    private val viewModel: EditProfileViewModel by viewModels {
        EditProfileViewModelFactory(requireContext())
    }

    // Track original values to detect changes
    private var originalUsername = ""
    private var originalPetCount = ""
    private var originalOwnerSince = ""

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
        updateSaveButtonState()
    }

    private fun setupAppBar() {
        val toolbar = binding.includeAppBar.topAppBar
        toolbar.title = getString(R.string.app_name)
    }

    private fun setupBottomNav() {
        val includeNav = binding.includeBottomNav
        val orange = ContextCompat.getColor(requireContext(), R.color.petopia_orange)
        val gray = ContextCompat.getColor(requireContext(), R.color.gray)

        includeNav.iconHome.setColorFilter(gray)
        includeNav.textHome.setTextColor(gray)
        includeNav.iconProfile.setColorFilter(gray)
        includeNav.textProfile.setTextColor(gray)
        includeNav.fabAddPost.visibility = View.GONE

        includeNav.navHome.setOnClickListener {
            findNavController().navigate(R.id.action_editProfile_to_home)
        }
        includeNav.navProfile.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupListeners() {
        // Track text changes to enable/disable save button
        binding.etUsername.addTextChangedListener { updateSaveButtonState() }
        binding.etPetCount.addTextChangedListener { updateSaveButtonState() }

        binding.etOwnerSince.setOnClickListener {
            val calendar = Calendar.getInstance()
            val existingDate = binding.etOwnerSince.text.toString()
            if (existingDate.isNotBlank()) {
                try {
                    val parts = existingDate.split("/")
                    calendar.set(Calendar.DAY_OF_MONTH, parts[0].toInt())
                    calendar.set(Calendar.MONTH, parts[1].toInt() - 1)
                    calendar.set(Calendar.YEAR, parts[2].toInt())
                } catch (_: Exception) { }
            }

            DatePickerDialog(
                requireContext(),
                R.style.DatePickerTheme,
                { _, year, month, day ->
                    binding.etOwnerSince.setText("$day/${month + 1}/$year")
                    updateSaveButtonState()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSave.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            if (username.isEmpty()) {
                Toast.makeText(context, getString(R.string.username_required), Toast.LENGTH_SHORT).show()
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

    private fun hasChanges(): Boolean {
        return binding.etUsername.text.toString() != originalUsername ||
                binding.etPetCount.text.toString() != originalPetCount ||
                binding.etOwnerSince.text.toString() != originalOwnerSince
    }

    private fun updateSaveButtonState() {
        val changed = hasChanges()
        binding.btnSave.isEnabled = changed
        if (changed) {
            binding.btnSave.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.petopia_orange))
            binding.btnSave.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.btnSave.strokeColor = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.petopia_orange)
            )
        } else {
            binding.btnSave.setBackgroundColor(Color.TRANSPARENT)
            binding.btnSave.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_400))
            binding.btnSave.strokeColor = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.gray_200)
            )
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

        val etPassword = dialog.findViewById<android.widget.EditText>(R.id.etPassword)

        dialog.findViewById<View>(R.id.btnClose).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnKeepAccount).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnConfirmDelete).setOnClickListener {
            val password = etPassword.text.toString()
            if (password.isEmpty()) {
                Toast.makeText(context, getString(R.string.password_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dialog.dismiss()
            viewModel.deleteAccount(password)
        }

        dialog.show()
    }

    private fun observeViewModel() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                originalUsername = it.username
                originalPetCount = it.petsCount.toString()
                originalOwnerSince = it.petOwnerSince ?: ""

                binding.etUsername.setText(it.username)
                binding.etPetCount.setText(it.petsCount.toString())
                binding.etOwnerSince.setText(it.petOwnerSince ?: "")

                updateSaveButtonState()
            }
        }

        viewModel.isSaving.observe(viewLifecycleOwner) { saving ->
            binding.savingOverlay.visibility = if (saving) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !saving
            binding.btnCancel.isEnabled = !saving
            binding.btnDeleteAccount.isEnabled = !saving
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    Toast.makeText(context, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(context, it.exceptionOrNull()?.message ?: getString(R.string.generic_error), Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    Toast.makeText(context, getString(R.string.account_deleted), Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_editProfile_to_auth)
                } else {
                    Toast.makeText(context, it.exceptionOrNull()?.message ?: getString(R.string.generic_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
