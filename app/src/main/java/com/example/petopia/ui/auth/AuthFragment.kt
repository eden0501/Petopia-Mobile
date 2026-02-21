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
import com.example.petopia.ui.auth.AuthViewModel
import com.google.android.material.tabs.TabLayout

class AuthFragment : Fragment(R.layout.fragment_auth) {

    private lateinit var viewModel: AuthViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userDao = AppLocalDB.db.userDao()

        val repository = UserRepository(userDao)

        val factory = AuthViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        // 2. Bind Views
        val authTabLayout = view.findViewById<TabLayout>(R.id.authTabLayout)
        val signupExtraFields = view.findViewById<LinearLayout>(R.id.signupExtraFields)
        val tvFormTitle = view.findViewById<TextView>(R.id.tvFormTitle)
        val tvFormDescription = view.findViewById<TextView>(R.id.tvFormDescription)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)

        val etUser = view.findViewById<EditText>(R.id.etUsername)
        val etPass = view.findViewById<EditText>(R.id.etPassword)
        val etPetCount = view.findViewById<EditText>(R.id.etPetCount)
        val etOwnerSince = view.findViewById<EditText>(R.id.etOwnerSince)

        // 3. Tab Toggle Logic
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

        // 4. Submit Button Logic
        btnSubmit.setOnClickListener {
            val username = etUser.text.toString()
            val password = etPass.text.toString()

            if (authTabLayout.selectedTabPosition == 0) {
                viewModel.login(username, password)
            } else {
//                val pets = etPetCount.text.toString().toIntOrNull() ?: 0
//                val date = etOwnerSince.text.toString()
                viewModel.signup(username, password)
            }
        }

        // 5. Observe Results
        viewModel.authStatus.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                Toast.makeText(context, getString(R.string.auth_success), Toast.LENGTH_SHORT).show()
                // Navigation.findNavController(view).navigate(R.id.action_auth_to_feed)
            } else if (success == false) {
                Toast.makeText(context, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }
}