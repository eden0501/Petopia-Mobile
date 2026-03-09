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
import com.example.petopia.R
import com.example.petopia.dao.AppLocalDB
import com.example.petopia.data.repository.UserRepository
import com.google.android.material.tabs.TabLayout

class AuthFragment : Fragment(R.layout.fragment_auth) {

    private lateinit var viewModel: AuthViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize dependencies using fragment context for better safety
        val db = AppLocalDB.getDatabase(requireContext())
        val userDao = db.userDao()
        val repository = UserRepository(userDao)
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
            val username = etUser.text.toString()
            val password = etPass.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (authTabLayout.selectedTabPosition == 0) {
                viewModel.login(email, password)
            } else {
                if (username.isBlank()) {
                    Toast.makeText(context, "Please enter a username", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.signup(email, password, username)
            }
        }

        // Observe Results
        viewModel.authStatus.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                Toast.makeText(context, getString(R.string.auth_success), Toast.LENGTH_SHORT).show()
            } else if (success == false) {
                Toast.makeText(context, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
