package com.example.petopia.ui.auth

import android.app.DatePickerDialog
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.petopia.R
import com.example.petopia.data.model.User
import com.google.android.material.tabs.TabLayout
import java.util.Calendar

class AuthFragment : Fragment(R.layout.fragment_auth) {

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
            view?.findViewById<ImageView>(R.id.profilePictureImage)?.setImageBitmap(bitmap)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = AuthViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        val authModeTabLayout = view.findViewById<TabLayout>(R.id.authModeTabLayout)
        val signupFieldsContainer = view.findViewById<LinearLayout>(R.id.signupFieldsContainer)
        val formTitleText = view.findViewById<TextView>(R.id.formTitleText)
        val formDescriptionText = view.findViewById<TextView>(R.id.formDescriptionText)
        val submitButton = view.findViewById<Button>(R.id.submitButton)
        val profilePictureImage = view.findViewById<ImageView>(R.id.profilePictureImage)

        val emailInput = view.findViewById<EditText>(R.id.emailInput)
        val usernameInput = view.findViewById<EditText>(R.id.usernameInput)
        val passwordInput = view.findViewById<EditText>(R.id.passwordInput)
        val petCountInput = view.findViewById<EditText>(R.id.petCountInput)
        val petOwnerSinceInput = view.findViewById<EditText>(R.id.petOwnerSinceInput)

        profilePictureImage?.setOnClickListener {
            pickImage.launch("image/*")
        }

        petOwnerSinceInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val existingDate = petOwnerSinceInput.text.toString()
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
                    petOwnerSinceInput.setText("$day/${month + 1}/$year")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }

        authModeTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    formTitleText.text = getString(R.string.welcome_back)
                    formDescriptionText.text = getString(R.string.login_description)
                    signupFieldsContainer.visibility = View.GONE
                    submitButton.text = getString(R.string.login_tab)
                } else {
                    formTitleText.text = getString(R.string.join_petopia)
                    formDescriptionText.text = getString(R.string.create_account_description)
                    signupFieldsContainer.visibility = View.VISIBLE
                    submitButton.text = getString(R.string.signup_tab)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        submitButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, getString(R.string.email_password_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitButton.isEnabled = false

            if (authModeTabLayout.selectedTabPosition == 0) {
                viewModel.login(email, password)
            } else {
                val username = usernameInput.text.toString().trim()
                if (username.isEmpty()) {
                    Toast.makeText(context, getString(R.string.username_required), Toast.LENGTH_SHORT).show()
                    submitButton.isEnabled = true
                    return@setOnClickListener
                }
                val user = User(
                    email = email,
                    username = username,
                    petsCount = petCountInput.text.toString().toIntOrNull() ?: 0,
                    petOwnerSince = petOwnerSinceInput.text.toString()
                )
                viewModel.signup(user, password, requireContext())
            }
        }

        viewModel.authStatus.observe(viewLifecycleOwner) { result ->
            submitButton.isEnabled = true
            
            result?.let {
                if (it.isSuccess) {
                    findNavController().navigate(R.id.action_auth_to_home)
                } else {
                    Toast.makeText(context, getString(R.string.auth_failed_default), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
