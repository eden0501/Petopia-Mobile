package com.example.petopia.ui.profile

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petopia.R
import com.example.petopia.base.Constants
import com.example.petopia.databinding.FragmentProfileBinding
import com.example.petopia.ui.home.CreatePostDialogFragment
import com.example.petopia.ui.home.PostAdapter
import com.example.petopia.types.HomeItem
import com.example.petopia.types.PostDisplayItem
import com.example.petopia.util.showDeletePostDialog
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val args: ProfileFragmentArgs by navArgs()

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(requireContext())
    }

    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAppBar()
        setupPostsFeed()
        setupBottomNav()
        observeViewModel()

        parentFragmentManager.setFragmentResultListener(Constants.ResultKeys.CREATE_POST_RESULT, viewLifecycleOwner) { _, bundle ->
            if (bundle.getBoolean(Constants.ResultKeys.SUCCESS)) {
                viewModel.loadProfile()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadProfile()
    }

    private fun setupAppBar() {
        val toolbar = binding.includeAppBar.topAppBar
        toolbar.inflateMenu(R.menu.menu_profile)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_settings -> {
                    val action = ProfileFragmentDirections.actionProfileToEditProfile(
                        userId = args.userId
                    )
                    findNavController().navigate(action)
                    true
                }
                R.id.action_logout -> {
                    showLogoutDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun showLogoutDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_logout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<View>(R.id.btnClose).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnCancelLogout).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<View>(R.id.btnConfirmLogout).setOnClickListener {
            dialog.dismiss()
            viewModel.logout()
            findNavController().navigate(R.id.action_profile_to_auth)
        }

        dialog.show()
    }

    private fun setupPostsFeed() {
        postAdapter = PostAdapter(
            onLikeClick = { item -> viewModel.toggleLike(item.post.id) },
            onCommentClick = { item -> viewModel.toggleComments(item.post.id) },
            onAddCommentClick = { item, text -> viewModel.addComment(item.post.id, text) },
            onEditClick = { item -> openEditDialog(item) },
            onDeleteClick = { item ->
                showDeletePostDialog {
                    viewModel.deletePost(item.post.id)
                    Toast.makeText(context, getString(R.string.post_deleted), Toast.LENGTH_SHORT).show()
                }
            },
            currentUserId = args.userId
        )
        binding.recyclerMyPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMyPosts.adapter = postAdapter
        binding.recyclerMyPosts.isNestedScrollingEnabled = false
    }

    private fun openEditDialog(item: PostDisplayItem) {
        val dialog = CreatePostDialogFragment.newEditInstance(item.post)
        dialog.show(parentFragmentManager, "edit_post")
    }


    private fun observeViewModel() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvUsername.text = it.username
                if (it.petOwnerSince.isNullOrEmpty() || it.petsCount == 0) {
                    binding.tvSubtitle.visibility = View.GONE
                } else {
                    binding.tvSubtitle.visibility = View.VISIBLE
                    binding.tvSubtitle.text = getString(
                        R.string.profile_subtitle_format,
                        getString(R.string.pet_owner_since),
                        it.petOwnerSince
                    )
                }
                binding.tvPetsBadge.text = getString(R.string.pets_badge_format, it.petsCount)

                if (!it.profileImageUrl.isNullOrEmpty()) {
                    Picasso.get()
                        .load(it.profileImageUrl)
                        .placeholder(R.drawable.bg_stub_avatar)
                        .error(R.drawable.bg_stub_avatar)
                        .into(binding.ivProfilePicture)
                }
            }
        }

        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            val homeItems = posts.map { HomeItem.PostItem(it) }
            postAdapter.submitList(homeItems)
            binding.tvEmptyPosts.visibility = if (posts.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.postCount.observe(viewLifecycleOwner) { count ->
            binding.tvPostsCount.text = count.toString()
        }

        viewModel.likesCount.observe(viewLifecycleOwner) { count ->
            binding.tvLikesCount.text = count.toString()
        }

        viewModel.commentsCount.observe(viewLifecycleOwner) { count ->
            binding.tvCommentsCount.text = count.toString()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.swipeRefresh.isRefreshing = loading
        }

        viewModel.error.observe(viewLifecycleOwner) { errorResId ->
            errorResId?.let {
                Toast.makeText(context, getString(it), Toast.LENGTH_LONG).show()
            }
        }

        binding.swipeRefresh.setColorSchemeResources(R.color.petopia_orange)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadProfile()
        }
    }

    private fun setupBottomNav() {
        val includeNav = binding.includeBottomNav
        val orange = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.petopia_orange)
        val gray = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.gray)

        includeNav.iconHome.setColorFilter(gray)
        includeNav.textHome.setTextColor(gray)
        includeNav.iconProfile.setColorFilter(orange)
        includeNav.textProfile.setTextColor(orange)

        includeNav.navHome.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_home)
        }

        includeNav.fabAddPost.setOnClickListener {
            findNavController().navigate(R.id.createPostDialogFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
