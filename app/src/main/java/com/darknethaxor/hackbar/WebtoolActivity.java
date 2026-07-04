package com.darknethaxor.hackbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.LinearProgressIndicator;

/**
 * WebtoolActivity — Embedded WebView for online pen-testing tools
 *
 * FIXES APPLIED:
 * 1. extends AppCompatActivity (was plain Activity)
 * 2. setAppCacheEnabled / setAppCachePath REMOVED — crashes API 33+
 * 3. setLayoutAlgorithm(SINGLE_COLUMN) REMOVED — deprecated API 19
 * 4. setAllowFileAccessFromFileURLs → false (security fix)
 * 5. setAllowUniversalAccessFromFileURLs → false (security fix)
 * 6. shouldOverrideUrlLoading(WebView, String) → (WebView, WebResourceRequest)
 * 7. addTestDevice() REMOVED from AdView — was in production
 * 8. LinearProgressIndicator replaces ProgressBar
 * 9. R.id.content reference removed (crashes if not in activity layout)
 * 10. apply() replaces commit()
 * 11. WebView properly destroyed in onDestroy()
 */
public class WebtoolActivity extends AppCompatActivity {

    public static final String EXTRA_URL   = "url";
    public static final String EXTRA_TITLE = "title";

    private static final String PREFS_NAME = "hackbar_prefs";

    private LinearLayout background;
    private LinearProgressIndicator progressBar;
    private LinearLayout webviewContainer;
    private ImageView btnBack, btnOpen;
    private TextView tvTitle;
    private WebView webView;
    private SharedPreferences prefs;
    private String savedUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webtool);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        bindViews();
        setupWebView();
        loadUrl();
    }

    private void bindViews() {
        background       = findViewById(R.id.background);
        progressBar      = findViewById(R.id.progressbar);
        webviewContainer = findViewById(R.id.webviewcontainer);
        btnBack          = findViewById(R.id.back);
        btnOpen          = findViewById(R.id.note);
        tvTitle          = findViewById(R.id.activityname);
        webView          = findViewById(R.id.webview);

        btnBack.setOnClickListener(v -> onBackPressed());
        btnOpen.setOnClickListener(v -> {
            // Open current URL in external browser
            try {
                android.content.Intent intent = new android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse(webView.getUrl()));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "No browser found", Toast.LENGTH_SHORT).show();
            }
        });

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        if (title != null && !title.isEmpty()) tvTitle.setText(title);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setUseWideViewPort(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        // FIX: was true — security vulnerability
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        // FIX: setAppCacheEnabled REMOVED — deprecated API 30, crashes API 33+
        // FIX: setLayoutAlgorithm(SINGLE_COLUMN) REMOVED — deprecated
        webView.setScrollbarFadingEnabled(true);
        webView.setWebViewClient(new WebtoolWebViewClient());
        webView.setWebChromeClient(new WebtoolWebChromeClient());

        // FIX: Handle clear cache setting using apply()
        String clearCache = prefs.getString("clear_on_exit", "false");
        if ("true".equals(clearCache)) {
            webView.clearCache(true);
            webView.clearFormData();
            webView.clearHistory();
            webView.clearSslPreferences();
            prefs.edit().putString("clear_on_exit", "false").apply(); // FIX: apply()
        }
    }

    private void loadUrl() {
        String url = getIntent().getStringExtra(EXTRA_URL);
        savedUrl = getIntent().getStringExtra(HackbarActivity.EXTRA_INITIAL_URL);
        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WEBVIEWCLIENT
    // FIX: shouldOverrideUrlLoading(WebView, String) → (WebView, WebResourceRequest)
    // ─────────────────────────────────────────────────────────────────────────

    private class WebtoolWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            // Allow all URLs — web tools site navigation should work normally
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            if (view.getTitle() != null) tvTitle.setText(view.getTitle());
        }
    }

    private class WebtoolWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress < 100) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setIndeterminate(false);
                progressBar.setProgress(newProgress);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            // Restore saved URL to SharedPreferences
            if (savedUrl != null && !savedUrl.isEmpty()) {
                prefs.edit().putString(HackbarActivity.EXTRA_INITIAL_URL, savedUrl).apply();
            }
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // FIX: Proper WebView cleanup prevents memory leaks
        if (webView != null) {
            webView.stopLoading();
            webView.clearHistory();
            webView.removeAllViews();
            webView.destroy();
        }
    }
}
