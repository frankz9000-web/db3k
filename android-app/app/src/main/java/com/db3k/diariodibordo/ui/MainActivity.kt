package com.db3k.diariodibordo.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.db3k.diariodibordo.R
import com.db3k.diariodibordo.databinding.ActivityMainBinding
import com.db3k.diariodibordo.messaging.TokenRefreshWorker
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Main Activity with bottom navigation and toolbar
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupBottomNavigation()
        setupNavigation()
        handleIntent(intent)
        scheduleTokenRefresh()
    }

    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupBottomNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.postsFragment,
                R.id.eventsFragment,
                R.id.notificationsFragment,
                R.id.settingsFragment
            )
        )

        binding.bottomNavigation.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun setupNavigation() {
        // Handle deep links from notifications
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateToolbarTitle(destination.label)
        }
    }

    private fun updateToolbarTitle(label: CharSequence?) {
        binding.toolbarTitle.text = label ?: getString(R.string.app_name)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            val action = it.action
            val data = it.data

            if (Intent.ACTION_VIEW == action && data != null) {
                // Handle deep link to post
                val slug = data.getQueryParameter("slug") ?: data.lastPathSegment
                if (slug != null && slug.isNotBlank()) {
                    navigateToPost(slug)
                }
            } else if (it.hasExtra("notification_url")) {
                // Handle notification click
                val url = it.getStringExtra("notification_url") ?: ""
                if (url.isNotBlank()) {
                    openUrl(url)
                }
            }
        }
    }

    private fun navigateToPost(slug: String) {
        val args = Bundle().apply { putString("slug", slug) }
        navController.navigate(R.id.action_global_postDetailFragment, args)
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleTokenRefresh() {
        lifecycleScope.launch {
            TokenRefreshWorker.scheduleWork()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                // TODO: Implement search
                true
            }
            R.id.action_refresh -> {
                viewModel.refreshPosts()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}