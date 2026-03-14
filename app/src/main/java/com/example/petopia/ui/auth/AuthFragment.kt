package com.example.petopia.ui.auth

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
import com.example.petopia.dao.AppLocalDB
import com.example.petopia.data.model.User
import com.example.petopia.data.repository.UserRepository
import com.google.android.material.tabs.TabLayout

class AuthFragment : Fragment(R.layout.fragment_auth) {

    private lateinit var viewModel: AuthViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize dependencies (In a real app, use Hilt or a Service Locator)
        val db = AppLocalDB.getDatabase(requireContext())
        val repository = UserRepository(db.userDao())
        val factory = AuthViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        // Bind Views
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

        // Tab Toggle Logic
        authTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) { // Login Mode
                    tvFormTitle.text = getString(R.string.welcome_back)
                    tvFormDescription.text = getString(R.string.login_description)
                    signupExtraFields.visibility = View.GONE
                    btnSubmit.text = getString(R.string.login_tab)
                } else { // Sign Up Mode
                    tvFormTitle.text = getString(R.string.join_petopia)
                    tvFormDescription.text = getString(R.string.create_account_description)
                    signupExtraFields.visibility = View.VISIBLE
                    btnSubmit.text = getString(R.string.signup_tab)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Submit Button Logic
        btnSubmit.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPass.text.toString()

            if (authTabLayout.selectedTabPosition == 0) {
                // Login
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    viewModel.login(email, password)
                } else {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Sign Up
                val username = etUser.text.toString()
                if (email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty()) {
                    val user = User(
                        id = "", // Will be replaced by Firebase UID
                        email = email,
                        username = username,
                        petsCount = etPetCount.text.toString().toIntOrNull() ?: 0,
                        seniority = etOwnerSince.text.toString()
                    )
                    viewModel.signup(user, password)
                } else {
                    Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe Auth Status
        viewModel.authStatus.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    // Navigate to Home
                    findNavController().navigate(R.id.action_auth_to_home)
                } else {
                    Toast.makeText(context, "Error: ${it.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
