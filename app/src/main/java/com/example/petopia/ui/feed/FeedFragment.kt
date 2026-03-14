package com.example.petopia.ui.feed

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
import kotlinx.coroutines.launch

class FeedFragment : Fragment(R.layout.fragment_feed) {

    private lateinit var repository: UserRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppLocalDB.getDatabase(requireContext())
        repository = UserRepository(db.userDao())

        val tvUserInfo = view.findViewById<TextView>(R.id.tvUserInfo)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // Display current user info
        val firebaseUser = repository.getCurrentUser()
        if (firebaseUser != null) {
            lifecycleScope.launch {
                val localUser = db.userDao().getUserById(firebaseUser.uid)
                val info = """
                    UID: ${firebaseUser.uid}
                    Email: ${firebaseUser.email}
                    Username: ${localUser?.username ?: "N/A"}
                    Pets Count: ${localUser?.petsCount ?: 0}
                    Seniority: ${localUser?.seniority ?: "Newcomer"}
                """.trimIndent()
                tvUserInfo.text = info
            }
        } else {
            // If no user, go back to auth
            findNavController().navigate(R.id.authFragment)
        }

        btnLogout.setOnClickListener {
            repository.logout()
            findNavController().navigate(R.id.authFragment)
        }
    }
}
