package com.example.petopia.ui.auth

import android.app.DatePickerDialog
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.petopia.R
import com.example.petopia.data.model.User
import com.example.petopia.databinding.FragmentAuthBinding
import com.google.android.material.tabs.TabLayout
import java.util.Calendar

class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AuthViewModel

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, it)
            } else {
                val source = ImageDecoder.createSource(requireContext().contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            }
            viewModel.setProfileImageBitmap(bitmap)
            binding.profilePictureImage.setImageBitmap(bitmap)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = AuthViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        binding.profilePictureImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.petOwnerSinceInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val existingDate = binding.petOwnerSinceInput.text.toString()
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
                    binding.petOwnerSinceInput.setText("$day/${month + 1}/$year")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }

        binding.authModeTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    binding.formTitleText.text = getString(R.string.welcome_back)
                    binding.formDescriptionText.text = getString(R.string.login_description)
                    binding.signupFieldsContainer.visibility = View.GONE
                    binding.submitButton.text = getString(R.string.login_tab)
                } else {
                    binding.formTitleText.text = getString(R.string.join_petopia)
                    binding.formDescriptionText.text = getString(R.string.create_account_description)
                    binding.signupFieldsContainer.visibility = View.VISIBLE
                    binding.submitButton.text = getString(R.string.signup_tab)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.submitButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, getString(R.string.email_password_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.submitButton.isEnabled = false

            if (binding.authModeTabLayout.selectedTabPosition == 0) {
                viewModel.login(email, password)
            } else {
                val username = binding.usernameInput.text.toString().trim()
                if (username.isEmpty()) {
                    Toast.makeText(context, getString(R.string.username_required), Toast.LENGTH_SHORT).show()
                    binding.submitButton.isEnabled = true
                    return@setOnClickListener
                }
                val user = User(
                    email = email,
                    username = username,
                    petsCount = binding.petCountInput.text.toString().toIntOrNull() ?: 0,
                    petOwnerSince = binding.petOwnerSinceInput.text.toString()
                )
                viewModel.signup(user, password, requireContext())
            }
        }

        viewModel.authStatus.observe(viewLifecycleOwner) { result ->
            binding.submitButton.isEnabled = true
            
            result?.let {
                if (it.isSuccess) {
                    findNavController().navigate(R.id.action_auth_to_home)
                } else {
                    Toast.makeText(context, getString(R.string.auth_failed_default), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
