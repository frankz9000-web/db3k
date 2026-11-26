package com.db3k.diariodibordo.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.db3k.diariodibordo.BuildConfig
import com.db3k.diariodibordo.R
import com.db3k.diariodibordo.databinding.FragmentSettingsBinding
import com.db3k.diariodibordo.data.preferences.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Settings fragment with Ducati-styled preferences
 */
@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPreferences()
        observePreferences()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupPreferences() {
        // Version
        binding.versionText.text = getString(R.string.settings_version, BuildConfig.VERSION_NAME)

        // Push notifications master switch
        binding.switchPushEnabled.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPushEnabled(isChecked)
            updatePushCategoryVisibility(isChecked)
        }

        // Category switches
        binding.switchNewPosts.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPushNewPosts(isChecked)
        }
        binding.switchRaceResults.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPushRaceResults(isChecked)
        }
        binding.switchEvents.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPushEvents(isChecked)
        }

        // Sound & vibration
        binding.switchSound.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPushSound(isChecked)
        }
        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPushVibration(isChecked)
        }

        // About links
        binding.privacyPolicy.setOnClickListener {
            openUrl("https://db3k-blog.pages.dev/privacy")
        }
        binding.termsOfService.setOnClickListener {
            openUrl("https://db3k-blog.pages.dev/terms")
        }
        binding.githubRepo.setOnClickListener {
            openUrl("https://github.com/db3k/blog-app")
        }
        binding.sendFeedback.setOnClickListener {
            sendFeedback()
        }
    }

    private fun observePreferences() {
        viewModel.pushEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.switchPushEnabled.isChecked = enabled
            updatePushCategoryVisibility(enabled)
        }

        viewModel.pushNewPosts.observe(viewLifecycleOwner) { enabled ->
            binding.switchNewPosts.isChecked = enabled
        }

        viewModel.pushRaceResults.observe(viewLifecycleOwner) { enabled ->
            binding.switchRaceResults.isChecked = enabled
        }

        viewModel.pushEvents.observe(viewLifecycleOwner) { enabled ->
            binding.switchEvents.isChecked = enabled
        }

        viewModel.pushSound.observe(viewLifecycleOwner) { enabled ->
            binding.switchSound.isChecked = enabled
        }

        viewModel.pushVibration.observe(viewLifecycleOwner) { enabled ->
            binding.switchVibration.isChecked = enabled
        }
    }

    private fun updatePushCategoryVisibility(enabled: Boolean) {
        val visibility = if (enabled) View.VISIBLE else View.GONE
        binding.pushCategoriesCard.visibility = visibility
        binding.pushSoundCard.visibility = visibility
        binding.pushVibrationCard.visibility = visibility
    }

    private fun openUrl(url: String) {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
        startActivity(intent)
    }

    private fun sendFeedback() {
        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:feedback@db3k.it")
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback DB3K Blog App v${BuildConfig.VERSION_NAME}")
            putExtra(android.content.Intent.EXTRA_TEXT, "Scrivi qui il tuo feedback...")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            com.google.android.material.snackbar.Snackbar.make(binding.root, "Nessun client email configurato", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
        }
    }
}