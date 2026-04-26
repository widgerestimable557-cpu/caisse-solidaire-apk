package com.caissesolidaire.app

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.caissesolidaire.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var webAppUrl: String = ""

    private val fileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        filePathCallback?.onReceiveValue(if (uri != null) arrayOf(uri) else null)
        filePathCallback = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val prefs = getSharedPreferences(SetupActivity.PREFS_NAME, Context.MODE_PRIVATE)
        webAppUrl = intent.getStringExtra(SetupActivity.EXTRA_URL) ?: prefs.getString(SetupActivity.PREF_URL, "") ?: ""
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Caisse Solidaire"
        val ws = binding.webView.settings
        ws.javaScriptEnabled = true; ws.domStorageEnabled = true; ws.databaseEnabled = true
        ws.useWideViewPort = true; ws.loadWithOverviewMode = true; ws.setSupportZoom(false)
        ws.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING))
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(ws, true)
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(binding.webView, true)
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(v: WebView?, u: String?, b: android.graphics.Bitmap?) { binding.progressBar.visibility = View.VISIBLE }
            override fun onPageFinished(v: WebView?, u: String?) { binding.progressBar.visibility = View.GONE; binding.swipeRefresh.isRefreshing = false }
            override fun onReceivedError(v: WebView?, r: WebResourceRequest?, e: WebResourceError?) { if (r?.isForMainFrame == true) { binding.progressBar.visibility = View.GONE; showOffline() } }
        }
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(v: WebView?, p: Int) { binding.progressBar.progress = p }
            override fun onShowFileChooser(w: WebView?, cb: ValueCallback<Array<Uri>>?, p: FileChooserParams?): Boolean {
                filePathCallback?.onReceiveValue(null); filePathCallback = cb; fileLauncher.launch("image/*"); return true
            }
            override fun onPermissionRequest(r: PermissionRequest?) { r?.grant(r.resources) }
        }
        binding.swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.accent))
        binding.swipeRefresh.setOnRefreshListener { if (isOnline()) { binding.offlineView.visibility = View.GONE; binding.webView.reload() } else { binding.swipeRefresh.isRefreshing = false; showOffline() } }
        binding.btnRetry.setOnClickListener { if (isOnline()) { binding.offlineView.visibility = View.GONE; load() } else Toast.makeText(this, "Hors ligne", Toast.LENGTH_SHORT).show() }
        if (isOnline()) load() else showOffline()
    }

    private fun load() { binding.offlineView.visibility = View.GONE; binding.webView.visibility = View.VISIBLE; binding.webView.loadUrl(webAppUrl) }
    private fun showOffline() { binding.webView.visibility = View.GONE; binding.offlineView.visibility = View.VISIBLE }
    private fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.getNetworkCapabilities(cm.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    override fun onCreateOptionsMenu(m: Menu): Boolean { menuInflater.inflate(R.menu.main_menu, m); return true }
    override fun onOptionsItemSelected(i: MenuItem): Boolean = when (i.itemId) {
        R.id.action_refresh -> { binding.webView.reload(); true }
        R.id.action_change_url -> {
            val input = android.widget.EditText(this).apply { setText(webAppUrl); setPadding(48, 32, 48, 32) }
            AlertDialog.Builder(this).setTitle("Changer de caisse").setView(input)
                .setPositiveButton("OK") { _, _ -> val u = input.text.toString().trim(); if (u.startsWith("https://")) { webAppUrl = u; getSharedPreferences(SetupActivity.PREFS_NAME, Context.MODE_PRIVATE).edit().putString(SetupActivity.PREF_URL, u).apply(); load() } }
                .setNegativeButton("Annuler", null).show(); true
        }
        R.id.action_clear_cache -> { binding.webView.clearCache(true); CookieManager.getInstance().removeAllCookies(null); load(); true }
        else -> super.onOptionsItemSelected(i)
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() { if (binding.webView.canGoBack()) binding.webView.goBack() else super.onBackPressed() }
}
