package com.example.petopia.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.petopia.R
import com.example.petopia.dao.AppLocalDB
import com.example.petopia.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvUsername = view.findViewById<TextView>(R.id.tvUsername)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        val tvPetsCount = view.findViewById<TextView>(R.id.tvPetsCount)
        val tvPetOwnerSince = view.findViewById<TextView>(R.id.tvPetOwnerSince)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            findNavController().navigate(R.id.authFragment)
            return
        }

        val db = AppLocalDB.getDatabase(requireContext())
        val repository = UserRepository(db.userDao())

        // Fetch and display user info
        viewLifecycleOwner.lifecycleScope.launch {
            val user = repository.getUser(currentUser.uid)
            user?.let {
                tvUsername.text = it.username
                tvEmail.text = it.email
                tvPetsCount.text = it.petsCount.toString()
                tvPetOwnerSince.text = it.petOwnerSince ?: "N/A"
            }
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            // Redirect to Auth and clear backstack
            findNavController().navigate(R.id.authFragment)
        }
    }
}
