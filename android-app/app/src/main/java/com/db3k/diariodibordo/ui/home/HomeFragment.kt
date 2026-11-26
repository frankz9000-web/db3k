package com.db3k.diariodibordo.ui.home

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
import com.db3k.diariodibordo.databinding.FragmentHomeBinding
import com.db3k.diariodibordo.ui.posts.PostAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Home fragment showing latest posts and upcoming events
 */
@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var postsAdapter: PostAdapter

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
        setupRecyclerView()
        setupSwipeRefresh()
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

    private fun observeData() {
        // Latest posts
        viewModel.latestPosts.observe(viewLifecycleOwner) { posts ->
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

        // Loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading && postsAdapter.currentList.isEmpty()) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

        // Error state
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.swipeRefresh.isRefreshing = false
                binding.progressBar.visibility = View.GONE
                showError(error)
            }
        }

        // Upcoming events (placeholder for now)
        viewModel.upcomingEvents.observe(viewLifecycleOwner) { events ->
            if (events.isNotEmpty()) {
                binding.eventsContainer.visibility = View.VISIBLE
                // TODO: Setup events horizontal list
            } else {
                binding.eventsContainer.visibility = View.GONE
            }
        }
    }

    private fun navigateToPost(slug: String) {
        val args = Bundle().apply { putString("slug", slug) }
        findNavController().navigate(R.id.action_homeFragment_to_postDetailFragment, args)
    }

    private fun showError(message: String) {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            message,
            com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
        ).setAction("Riprova") { viewModel.refreshPosts() }.show()
    }
}