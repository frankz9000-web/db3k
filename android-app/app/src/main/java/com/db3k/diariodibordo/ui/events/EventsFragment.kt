package com.db3k.diariodibordo.ui.events

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
import com.db3k.diariodibordo.databinding.FragmentEventsBinding
import com.db3k.diariodibordo.ui.posts.PostAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Events fragment showing upcoming events and races
 */
@AndroidEntryPoint
class EventsFragment : Fragment(R.layout.fragment_events) {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EventsViewModel by viewModels()
    private lateinit var eventsAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
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
        eventsAdapter = PostAdapter { post ->
            navigateToPost(post.slug)
        }
        binding.eventsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshEvents()
        }
        binding.swipeRefresh.setColorSchemeResources(R.color.ducati_red, R.color.ducati_silver)
    }

    private fun observeData() {
        viewModel.events.observe(viewLifecycleOwner) { events ->
            binding.swipeRefresh.isRefreshing = false
            if (events.isNotEmpty()) {
                binding.eventsRecyclerView.visibility = View.VISIBLE
                binding.emptyState.visibility = View.GONE
                eventsAdapter.submitList(events)
            } else {
                binding.eventsRecyclerView.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading && eventsAdapter.currentList.isEmpty()) {
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

        // Past events
        viewModel.pastEvents.observe(viewLifecycleOwner) { pastEvents ->
            if (pastEvents.isNotEmpty()) {
                binding.pastEventsContainer.visibility = View.VISIBLE
                // TODO: Setup past events section
            } else {
                binding.pastEventsContainer.visibility = View.GONE
            }
        }
    }

    private fun navigateToPost(slug: String) {
        val args = Bundle().apply { putString("slug", slug) }
        findNavController().navigate(R.id.action_eventsFragment_to_postDetailFragment, args)
    }

    private fun showError(message: String) {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            message,
            com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
        ).setAction("Riprova") { viewModel.refreshEvents() }.show()
    }
}