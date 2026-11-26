package com.db3k.diariodibordo.ui.posts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.db3k.diariodibordo.R
import com.db3k.diariodibordo.databinding.FragmentPostsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Posts list fragment with categories and search
 */
@AndroidEntryPoint
class PostsFragment : Fragment(R.layout.fragment_posts) {

    private var _binding: FragmentPostsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PostsViewModel by viewModels()
    private lateinit var postsAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeRefresh()
        setupSearch()
        observeData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        postsAdapter = PostAdapter { post ->
            navigateToPost(post.slug)
        }
        binding.postsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshPosts()
        }
        binding.swipeRefresh.setColorSchemeResources(R.color.ducati_red, R.color.ducati_silver)
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchPosts(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchPosts(newText ?: "")
                return true
            }
        })
    }

    private fun observeData() {
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            binding.swipeRefresh.isRefreshing = false
            if (posts.isNotEmpty()) {
                binding.postsRecyclerView.visibility = View.VISIBLE
                binding.emptyState.visibility = View.GONE
                postsAdapter.submitList(posts)
            } else {
                binding.postsRecyclerView.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading && postsAdapter.currentList.isEmpty()) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.swipeRefresh.isRefreshing = false
                binding.progressBar.visibility = View.GONE
                showError(error)
            }
        }

        // Categories
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            setupCategoryChips(categories)
        }
    }

    private fun setupCategoryChips(categories: List<String>) {
        binding.categoryChips.removeAllViews()
        val allChip = com.google.android.material.chip.Chip(requireContext()).apply {
            text = "Tutti"
            isChecked = true
            setOnClickListener { viewModel.filterByCategory(null) }
        }
        binding.categoryChips.addView(allChip)

        categories.forEach { category ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = category
                setOnClickListener { viewModel.filterByCategory(category) }
            }
            binding.categoryChips.addView(chip)
        }
    }

    private fun navigateToPost(slug: String) {
        val args = Bundle().apply { putString("slug", slug) }
        findNavController().navigate(R.id.action_postsFragment_to_postDetailFragment, args)
    }

    private fun showError(message: String) {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            message,
            com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
        ).setAction("Riprova") { viewModel.refreshPosts() }.show()
    }
}