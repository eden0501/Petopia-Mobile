package com.example.petopia.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petopia.R
import com.example.petopia.data.PostDisplayItem
import com.example.petopia.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PostAdapter()
        binding.recyclerFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFeed.adapter = adapter

        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            updateFilteredPosts(posts)
        }
        viewModel.selectedFilter.observe(viewLifecycleOwner) { _ ->
            updateFilteredPosts(viewModel.posts.value ?: emptyList())
        }

        setupFilterDropdown()
        setupBottomNav()
    }

    private fun updateFilteredPosts(posts: List<PostDisplayItem>) {
        val filter = viewModel.selectedFilter.value ?: PostFilter.ALL
        val filtered = when (filter) {
            PostFilter.ALL -> posts
            PostFilter.RESCUE -> posts.filter { it.post.postType == com.example.petopia.data.PostType.RESCUE }
            PostFilter.CARE_TIPS -> posts.filter { it.post.postType == com.example.petopia.data.PostType.KNOWLEDGE }
            PostFilter.SUPPLIES -> posts.filter { it.post.postType == com.example.petopia.data.PostType.SUPPLIES }
        }
        adapter.submitList(filtered)
    }

    private fun setupFilterDropdown() {
        val filterContainer = binding.root.findViewById<View>(R.id.filterDropdownContainer) ?: return
        val filterText = binding.root.findViewById<android.widget.TextView>(R.id.filterDropdown) ?: return

        filterContainer.setOnClickListener {
            PopupMenu(requireContext(), filterContainer).apply {
                menuInflater.inflate(R.menu.menu_filter, menu)
                setOnMenuItemClickListener { item ->
                    val filter = when (item.itemId) {
                        R.id.filter_all -> PostFilter.ALL
                        R.id.filter_rescue -> PostFilter.RESCUE
                        R.id.filter_care_tips -> PostFilter.CARE_TIPS
                        R.id.filter_equipment -> PostFilter.SUPPLIES
                        else -> PostFilter.ALL
                    }
                    viewModel.setFilter(filter)
                    filterText.text = when (filter) {
                        PostFilter.ALL -> getString(R.string.filter_all_posts)
                        PostFilter.RESCUE -> getString(R.string.filter_rescue_alerts)
                        PostFilter.CARE_TIPS -> getString(R.string.filter_care_tips)
                        PostFilter.SUPPLIES -> getString(R.string.filter_equipment_donations)
                    }
                    true
                }
                show()
            }
        }
    }

    private fun setupBottomNav() {
        binding.root.findViewById<View>(R.id.fabAddPost)?.setOnClickListener {
            // TODO: navigate to create post
        }
        binding.root.findViewById<View>(R.id.navProfile)?.setOnClickListener {
            // TODO: navigate to profile
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
