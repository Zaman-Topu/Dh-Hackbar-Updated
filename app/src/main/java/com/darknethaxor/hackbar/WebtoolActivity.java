package com.darknethaxor.hackbar;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * WebtoolActivity
 * Reverted to original UI (webtool.xml)
 */
public class WebtoolActivity extends AppCompatActivity {

    private ImageView btnBack, btnNote;
    private TextView tvTitle;
    private ProgressBar progressBar;
    private WebView webView;
    private String currentUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webtool); // Original layout

        bindViews();
        setupWebView();
        loadInitialData();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.back);
        btnNote = findViewById(R.id.note);
        tvTitle = findViewById(R.id.activityname);
        progressBar = findViewById(R.id.progressbar);
        webView = findViewById(R.id.webview);

        btnBack.setOnClickListener(v -> onBackPressed());
        btnNote.setOnClickListener(v -> copyCurrentUrl());
        
        tvTitle.setText("Web Tools"); // Override the decompiled "Fuck"
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                currentUrl = url;
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                tvTitle.setText(view.getTitle());
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void loadInitialData() {
        String url = getIntent().getStringExtra("url");
        String title = getIntent().getStringExtra("title");
        
        if (title != null && !title.isEmpty()) {
            tvTitle.setText(title);
        }
        
        if (url != null && !url.isEmpty()) {
            currentUrl = url;
            webView.loadUrl(url);
        }
    }

    private void copyCurrentUrl() {
        if (currentUrl != null && !currentUrl.isEmpty()) {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("URL", currentUrl));
                Toast.makeText(this, "Copied URL to clipboard", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
