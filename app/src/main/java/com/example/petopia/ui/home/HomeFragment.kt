package com.example.petopia.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petopia.R
import com.example.petopia.base.Constants
import com.example.petopia.types.HomeItem
import com.example.petopia.types.PostType
import com.example.petopia.types.PostDisplayItem
import com.example.petopia.types.PostFilter
import com.example.petopia.databinding.FragmentHomeBinding
import com.example.petopia.util.showDeletePostDialog

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(requireContext())
    }
    private lateinit var adapter: PostAdapter
    private var lastFilterPopupDismissTime = 0L

    private var shouldScrollToTop = false

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
            onAddCommentClick = { item, text -> viewModel.addComment(item.post.id, text) },
            onRefreshFactClick = { viewModel.refreshFact() },
            onFactVisible = { factId -> viewModel.loadFact(factId) },
            onEditClick = { item -> openEditDialog(item) },
            onDeleteClick = { item ->
                showDeletePostDialog {
                    viewModel.deletePost(item.post.id)
                }
            },
            currentUserId = viewModel.getCurrentUserId()
        )
        binding.recyclerFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFeed.adapter = adapter

        viewModel.filteredHomeItems.observe(viewLifecycleOwner) { items ->
            val layoutManager = binding.recyclerFeed.layoutManager as? LinearLayoutManager
            val firstPos = layoutManager?.findFirstVisibleItemPosition() ?: -1
            val firstVisibleId = if (firstPos != -1 && firstPos < adapter.itemCount) {
                val item = adapter.currentList[firstPos]
                if (item is HomeItem.PostItem) item.displayItem.post.id else null
            } else null

            adapter.submitList(items) {
                val postStillExists = firstVisibleId?.let { id ->
                    items.any { it is HomeItem.PostItem && it.displayItem.post.id == id }
                } ?: true

                if (shouldScrollToTop || !postStillExists) {
                    binding.recyclerFeed.scrollToPosition(0)
                    shouldScrollToTop = false
                }
            }
        }

        parentFragmentManager.setFragmentResultListener(Constants.ResultKeys.CREATE_POST_RESULT, viewLifecycleOwner) { _, bundle ->
            if (bundle.getBoolean(Constants.ResultKeys.SUCCESS)) {
                shouldScrollToTop = true
                viewModel.loadPosts()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.loadingOverlay.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorResId ->
            errorResId?.let {
                android.widget.Toast.makeText(context, getString(it), android.widget.Toast.LENGTH_LONG).show()
            }
        }

        setupFilterDropdown()
        setupBottomNav()
    }

    private fun openEditDialog(item: PostDisplayItem) {
        val dialog = CreatePostDialogFragment.newEditInstance(item.post)
        dialog.show(parentFragmentManager, "edit_post")
    }


    private fun setupFilterDropdown() {
        val filterContainer = binding.includeFilter.filterDropdownContainer
        val filterText = binding.includeFilter.filterDropdown

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
                        tv.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.text_body))
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
        binding.includeBottomNav.fabAddPost.setOnClickListener {
            findNavController().navigate(R.id.createPostDialogFragment)
        }
        
        binding.includeBottomNav.navHome.setOnClickListener {
            setActiveTab(isHome = true)
        }
        
        binding.includeBottomNav.navProfile.setOnClickListener {
            setActiveTab(isHome = false)
            val action = HomeFragmentDirections.actionHomeToProfile(
                userId = viewModel.getCurrentUserId() ?: ""
            )
            findNavController().navigate(action)
        }
    }

    private fun setActiveTab(isHome: Boolean) {
        val nav = binding.includeBottomNav
        val orange = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.petopia_orange)
        val gray = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.gray)

        if (isHome) {
            nav.iconHome.setColorFilter(orange)
            nav.textHome.setTextColor(orange)

            nav.iconProfile.setColorFilter(gray)
            nav.textProfile.setTextColor(gray)
        } else {
            nav.iconProfile.setColorFilter(orange)
            nav.textProfile.setTextColor(orange)

            nav.iconHome.setColorFilter(gray)
            nav.textHome.setTextColor(gray)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
