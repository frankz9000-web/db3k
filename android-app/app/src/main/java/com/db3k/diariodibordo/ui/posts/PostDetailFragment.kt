package com.db3k.diariodibordo.ui.posts

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.db3k.diariodibordo.R
import com.db3k.diariodibordo.databinding.FragmentPostDetailBinding
import com.db3k.diariodibordo.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Post detail fragment with WebView for rich content
 */
@AndroidEntryPoint
class PostDetailFragment : Fragment() {

    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PostDetailViewModel by viewModels()
    private var currentPost: com.db3k.diariodibordo.data.model.Post? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebView()
        observeData()

        // Get slug from arguments
        arguments?.getString("slug")?.let { slug ->
            viewModel.loadPost(slug)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupWebView() {
        binding.webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = true
                displayZoomControls = false
                setSupportZoom(true)
                textZoom = 100
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    if (url.startsWith("http")) {
                        requireActivity().startActivity(
                            android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                        )
                        return true
                    }
                    return false
                }
            }
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }
    }

    private fun observeData() {
        viewModel.post.observe(viewLifecycleOwner) { post ->
            currentPost = post
            post?.let { bindPost(it) }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.contentLayout.visibility = if (loading) View.GONE else View.VISIBLE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.progressBar.visibility = View.GONE
                showError(error)
            }
        }
    }

    private fun bindPost(post: com.db3k.diariodibordo.data.model.Post) {
        binding.title.text = post.title
        binding.date.text = post.getFormattedDate()
        binding.author.text = post.author ?: ""
        binding.category.text = post.category ?: ""

        // Load cover image
        post.coverImage?.let { imageUrl ->
            binding.coverImage.visibility = View.VISIBLE
            Glide.with(this)
                .load(imageUrl)
                .apply(RequestOptions()
                    .placeholder(R.color.ducati_medium_gray)
                    .error(R.color.ducati_medium_gray)
                    .transform(RoundedCorners(16))
                )
                .into(binding.coverImage)
        } ?: run {
            binding.coverImage.visibility = View.GONE
        }

        // Render content in WebView with Ducati styling
        val htmlContent = buildHtmlContent(post)
        binding.webView.loadDataWithBaseURL(
            "https://db3k-blog.pages.dev/",
            htmlContent,
            "text/html",
            "UTF-8",
            null
        )

        // Tags
        if (post.tags.isNotEmpty()) {
            binding.tagsContainer.visibility = View.VISIBLE
            binding.tagsContainer.removeAllViews()
            post.tags.forEach { tag ->
                val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                    text = tag
                    isClickable = false
                    setTextAppearance(R.style.TextAppearance_DB3K_LabelSmall)
                }
                binding.tagsContainer.addView(chip)
            }
        } else {
            binding.tagsContainer.visibility = View.GONE
        }
    }

    private fun buildHtmlContent(post: com.db3k.diariodibordo.data.model.Post): String {
        val content = post.content ?: post.excerpt ?: ""
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        color: #FFFFFF;
                        background-color: #0D0D0D;
                        margin: 0;
                        padding: 16px;
                        line-height: 1.6;
                        font-size: 16px;
                    }
                    h1, h2, h3, h4 { color: #E31837; margin-top: 24px; margin-bottom: 12px; }
                    h1 { font-size: 28px; border-bottom: 2px solid #E31837; padding-bottom: 8px; }
                    h2 { font-size: 24px; }
                    h3 { font-size: 20px; }
                    p { margin: 12px 0; }
                    a { color: #FF6B6B; text-decoration: none; }
                    a:hover { text-decoration: underline; }
                    img { max-width: 100%; height: auto; border-radius: 12px; margin: 16px 0; }
                    blockquote { border-left: 4px solid #E31837; padding-left: 16px; margin: 16px 0; color: #B0B0B0; font-style: italic; }
                    code { background: #2D2D2D; padding: 2px 6px; border-radius: 4px; font-family: monospace; }
                    pre { background: #1A1A1A; padding: 16px; border-radius: 12px; overflow-x: auto; border: 1px solid #333; }
                    pre code { background: none; padding: 0; }
                    ul, ol { padding-left: 24px; }
                    li { margin: 8px 0; }
                    hr { border: none; border-top: 1px solid #333; margin: 24px 0; }
                    .ducati-accent { color: #E31837; font-weight: 600; }
                </style>
            </head>
            <body>
                $content
            </body>
            </html>
        """.trimIndent()
    }

    private fun showError(message: String) {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            message,
            com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
        ).setAction("Riprova") {
            arguments?.getString("slug")?.let { viewModel.loadPost(it) }
        }.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_post_detail, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                sharePost()
                true
            }
            R.id.action_open_browser -> {
                openInBrowser()
                true
            }
            R.id.action_copy_link -> {
                copyLink()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun sharePost() {
        currentPost?.let { post ->
            val shareText = "${post.title}\n\n${post.url ?: "https://db3k-blog.pages.dev/post/${post.slug}"}"
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                putExtra(android.content.Intent.EXTRA_SUBJECT, post.title)
            }
            startActivity(android.content.Intent.createChooser(intent, "Condividi post"))
        }
    }

    private fun openInBrowser() {
        currentPost?.url?.let { url ->
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
            startActivity(intent)
        }
    }

    private fun copyLink() {
        currentPost?.url?.let { url ->
            val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Post Link", url)
            clipboard.setPrimaryClip(clip)
            com.google.android.material.snackbar.Snackbar.make(binding.root, "Link copiato", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
        }
    }
}