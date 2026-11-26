package com.db3k.diariodibordo.ui.notifications

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
import com.db3k.diariodibordo.databinding.FragmentNotificationsBinding
import com.db3k.diariodibordo.data.model.Notification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Notifications fragment showing push notification history
 */
@AndroidEntryPoint
class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationsViewModel by viewModels()
    private lateinit var notificationsAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeRefresh()
        setupActions()
        observeData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        notificationsAdapter = NotificationAdapter(
            onClick = { notification ->
                handleNotificationClick(notification)
            },
            onLongClick = { notification ->
                showNotificationOptions(notification)
            }
        )
        binding.notificationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshNotifications()
        }
        binding.swipeRefresh.setColorSchemeResources(R.color.ducati_red, R.color.ducati_silver)
    }

    private fun setupActions() {
        binding.markAllRead.setOnClickListener {
            viewModel.markAllAsRead()
        }

        binding.clearAll.setOnClickListener {
            showClearAllConfirmation()
        }
    }

    private fun observeData() {
        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            binding.swipeRefresh.isRefreshing = false
            if (notifications.isNotEmpty()) {
                binding.notificationsRecyclerView.visibility = View.VISIBLE
                binding.emptyState.visibility = View.GONE
                notificationsAdapter.submitList(notifications)
            } else {
                binding.notificationsRecyclerView.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading && notificationsAdapter.currentList.isEmpty()) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

        viewModel.unreadCount.observe(viewLifecycleOwner) { count ->
            binding.unreadBadge.text = count.toString()
            binding.unreadBadge.visibility = if (count > 0) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.swipeRefresh.isRefreshing = false
                binding.progressBar.visibility = View.GONE
                showError(error)
            }
        }
    }

    private fun handleNotificationClick(notification: Notification) {
        // Mark as read
        viewModel.markAsRead(notification.id)

        // Navigate to post or open URL
        notification.url?.let { url ->
            if (url.startsWith("http")) {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                startActivity(intent)
            } else {
                // Deep link to post
                val slug = url
                val args = Bundle().apply { putString("slug", slug) }
                findNavController().navigate(R.id.action_notificationsFragment_to_postDetailFragment, args)
            }
        } ?: notification.postSlug?.let { slug ->
            val args = Bundle().apply { putString("slug", slug) }
            findNavController().navigate(R.id.action_notificationsFragment_to_postDetailFragment, args)
        }
    }

    private fun showNotificationOptions(notification: Notification) {
        val options = arrayOf("Segna come letto", "Elimina", "Condividi")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(notification.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewModel.markAsRead(notification.id)
                    1 -> viewModel.deleteNotification(notification.id)
                    2 -> shareNotification(notification)
                }
            }
            .show()
    }

    private fun shareNotification(notification: Notification) {
        val shareText = "${notification.title}\n${notification.body}\n\n${notification.url ?: ""}"
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
        }
        startActivity(android.content.Intent.createChooser(intent, "Condividi notifica"))
    }

    private fun showClearAllConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Svuota tutte le notifiche")
            .setMessage("Sei sicuro di voler eliminare tutte le notifiche? Questa azione non può essere annullata.")
            .setPositiveButton("Elimina tutto") { _, _ ->
                viewModel.clearAllNotifications()
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun showError(message: String) {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            message,
            com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
        ).setAction("Riprova") { viewModel.refreshNotifications() }.show()
    }
}