package com.example.petopia.ui.auth

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.petopia.R
import com.example.petopia.data.local.dao.AppLocalDB
import com.example.petopia.data.model.User
import com.example.petopia.data.repository.UserRepository
import com.google.android.material.tabs.TabLayout
import java.util.Calendar

class AuthFragment : Fragment(R.layout.fragment_auth) {

    private lateinit var viewModel: AuthViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppLocalDB.getDatabase(requireContext())
        val repository = UserRepository(db.userDao(), db)
        val factory = AuthViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        val authTabLayout = view.findViewById<TabLayout>(R.id.authTabLayout)
        val signupExtraFields = view.findViewById<LinearLayout>(R.id.signupExtraFields)
        val tvFormTitle = view.findViewById<TextView>(R.id.tvFormTitle)
        val tvFormDescription = view.findViewById<TextView>(R.id.tvFormDescription)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etUser = view.findViewById<EditText>(R.id.etUsername)
        val etPass = view.findViewById<EditText>(R.id.etPassword)
        val etPetCount = view.findViewById<EditText>(R.id.etPetCount)
        val etOwnerSince = view.findViewById<EditText>(R.id.etOwnerSince)

        etOwnerSince.setOnClickListener {
            val calendar = Calendar.getInstance()
            val existingDate = etOwnerSince.text.toString()
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
                    etOwnerSince.setText("$day/${month + 1}/$year")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }

        authTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    tvFormTitle.text = getString(R.string.welcome_back)
                    tvFormDescription.text = getString(R.string.login_description)
                    signupExtraFields.visibility = View.GONE
                    btnSubmit.text = getString(R.string.login_tab)
                } else {
                    tvFormTitle.text = getString(R.string.join_petopia)
                    tvFormDescription.text = getString(R.string.create_account_description)
                    signupExtraFields.visibility = View.VISIBLE
                    btnSubmit.text = getString(R.string.signup_tab)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        btnSubmit.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPass.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Email and password are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false

            if (authTabLayout.selectedTabPosition == 0) {
                viewModel.login(email, password)
            } else {
                val username = etUser.text.toString().trim()
                if (username.isEmpty()) {
                    Toast.makeText(context, "Username is required", Toast.LENGTH_SHORT).show()
                    btnSubmit.isEnabled = true
                    return@setOnClickListener
                }
                val user = User(
                    email = email,
                    username = username,
                    petsCount = etPetCount.text.toString().toIntOrNull() ?: 0,
                    petOwnerSince = etOwnerSince.text.toString()
                )
                viewModel.signup(user, password)
            }
        }

        viewModel.authStatus.observe(viewLifecycleOwner) { result ->
            btnSubmit.isEnabled = true
            
            result?.let {
                if (it.isSuccess) {
                    findNavController().navigate(R.id.action_auth_to_home)
                } else {
                    val errorMsg = it.exceptionOrNull()?.message ?: "Authentication failed"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
