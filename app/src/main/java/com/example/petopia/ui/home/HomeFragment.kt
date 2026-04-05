package com.example.petopia.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petopia.R
import com.example.petopia.data.model.PostDisplayItem
import com.example.petopia.data.model.PostType
import com.example.petopia.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(requireContext())
    }
    private lateinit var adapter: PostAdapter
    private var lastFilterPopupDismissTime = 0L

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

        adapter = PostAdapter(
            onLikeClick = { item -> viewModel.toggleLike(item.post.id) },
            onCommentClick = { item -> viewModel.toggleComments(item.post.id) },
            onAddCommentClick = { item, text -> viewModel.addComment(item.post.id, text) }
        )
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
            PostFilter.RESCUE -> posts.filter { it.post.postType == PostType.RESCUE }
            PostFilter.CARE_TIPS -> posts.filter { it.post.postType == PostType.KNOWLEDGE }
            PostFilter.SUPPLIES -> posts.filter { it.post.postType == PostType.SUPPLIES }
        }
        adapter.submitList(filtered)
    }

    private fun setupFilterDropdown() {
        val filterContainer = binding.root.findViewById<View>(R.id.includeFilter) ?: return
        val filterText = filterContainer.findViewById<android.widget.TextView>(R.id.filterDropdown) ?: return

        filterContainer.setOnClickListener {
            if (System.currentTimeMillis() - lastFilterPopupDismissTime < 300) {
                return@setOnClickListener
            }
            val popup = android.widget.ListPopupWindow(requireContext())
            popup.anchorView = filterContainer
            popup.width = filterContainer.measuredWidth
            popup.isModal = true
            
            popup.setOnDismissListener {
                lastFilterPopupDismissTime = System.currentTimeMillis()
            }
            
            val backgroundDrawable = androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.bg_filter_popup)
            popup.setBackgroundDrawable(backgroundDrawable)

            val items = listOf(
                getString(R.string.filter_all_posts),
                getString(R.string.rescue_alert),
                getString(R.string.care_tip),
                getString(R.string.equipment_donation)
            )

            val adapter = object : android.widget.ArrayAdapter<String>(requireContext(), R.layout.row_filter_item, items) {
                override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                    val view = convertView ?: layoutInflater.inflate(R.layout.row_filter_item, parent, false)
                    val tv = view.findViewById<android.widget.TextView>(R.id.textFilterName)
                    val iconCheck = view.findViewById<android.widget.ImageView>(R.id.iconCheck)
                    val innerContainer = view.findViewById<android.widget.LinearLayout>(R.id.innerContainer)

                    tv.text = getItem(position)

                    val currentFilterIndex = when (viewModel.selectedFilter.value) {
                        PostFilter.ALL -> 0
                        PostFilter.RESCUE -> 1
                        PostFilter.CARE_TIPS -> 2
                        PostFilter.SUPPLIES -> 3
                        null -> 0
                    }

                    if (position == currentFilterIndex) {
                        innerContainer.setBackgroundResource(R.drawable.bg_filter_selected)
                        tv.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.petopia_orange))
                        iconCheck.visibility = View.VISIBLE
                        iconCheck.setColorFilter(androidx.core.content.ContextCompat.getColor(context, R.color.petopia_orange))
                    } else {
                        innerContainer.setBackgroundResource(0)
                        tv.setTextColor(android.graphics.Color.parseColor("#424242"))
                        iconCheck.visibility = View.GONE
                    }
                    return view
                }
            }

            popup.setAdapter(adapter)
            popup.setOnItemClickListener { _, _, position, _ ->
                val filter = when (position) {
                    0 -> PostFilter.ALL
                    1 -> PostFilter.RESCUE
                    2 -> PostFilter.CARE_TIPS
                    3 -> PostFilter.SUPPLIES
                    else -> PostFilter.ALL
                }
                viewModel.setFilter(filter)
                filterText.text = items[position]
                popup.dismiss()
            }
            popup.show()
        }
    }

    private fun setupBottomNav() {
        binding.root.findViewById<View>(R.id.fabAddPost)?.setOnClickListener {
            findNavController().navigate(R.id.createPostDialogFragment)
        }
        
        val navHome = binding.root.findViewById<View>(R.id.navHome)
        val navProfile = binding.root.findViewById<View>(R.id.navProfile)
        
        navHome?.setOnClickListener {
            setActiveTab(isHome = true)
            // TODO: navigate to home
        }
        
        navProfile?.setOnClickListener {
            setActiveTab(isHome = false)
            findNavController().navigate(R.id.action_home_to_profile)
        }
    }

    private fun setActiveTab(isHome: Boolean) {
        val navHome = binding.root.findViewById<View>(R.id.navHome) ?: return
        val iconHome = binding.root.findViewById<android.widget.ImageView>(R.id.iconHome) ?: return
        val textHome = binding.root.findViewById<android.widget.TextView>(R.id.textHome) ?: return

        val navProfile = binding.root.findViewById<View>(R.id.navProfile) ?: return
        val iconProfile = binding.root.findViewById<android.widget.ImageView>(R.id.iconProfile) ?: return
        val textProfile = binding.root.findViewById<android.widget.TextView>(R.id.textProfile) ?: return

        val orange = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.petopia_orange)
        val gray = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.gray)

        if (isHome) {
            iconHome.setColorFilter(orange)
            textHome.setTextColor(orange)

            iconProfile.setColorFilter(gray)
            textProfile.setTextColor(gray)
        } else {
            iconProfile.setColorFilter(orange)
            textProfile.setTextColor(orange)

            iconHome.setColorFilter(gray)
            textHome.setTextColor(gray)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
