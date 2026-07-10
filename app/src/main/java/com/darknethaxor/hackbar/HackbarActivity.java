package com.darknethaxor.hackbar;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HackbarActivity — Main Browser + Penetration Testing Toolbar
 * Reverted to original UI layout views, keeping modernized background logic.
 */
public class HackbarActivity extends AppCompatActivity {

    public static final String EXTRA_INITIAL_URL = "initial_url";
    private static final String PREFS_NAME       = "hackbar_prefs";
    private static final String PREF_LAST_URL    = "last_url";
    private static final String PREF_SAVE_URL    = "save_last_url";
    private static final String PREF_TEXT_SIZE   = "text_size";

    // Views (Matching original hackbar.xml exactly)
    private LinearLayout background, topbar, inputLayout, findLayout, webviewContainer, pageTitleLayout;
    private EditText urlField, paramsField, findField;
    private ImageView btnUndo, btnRedo, btnNote, btnShowHide, btnMenu;
    private ImageView btnFindPrev, btnFindNext, btnFindClose, pageFavicon;
    private TextView appName, pageTitle, findCount;
    private ProgressBar progressBar;
    private WebView webView;

    // Row 1
    private TextView tvBack, tvForward, tvClear, tvExecute, tvReload, tvStop, tvCopy, tvPaste;
    // Row 2
    private TextView tvColumnCount, tvUnionStatements, tvBasicStatements, tvDios, tvLocalVariable;
    private TextView tvErrorBased, tvPrintSystem, tvDoubleQuery, tvXpathInjection, tvMssql;
    private TextView tvPostgresql, tvLfi, tvRce, tvXss, tvCustomQuery;
    // Row 3
    private TextView tvReplace, tvWafBypass, tvOrderByBypass, tvUnionSelectBypass, tvUrlBalancer;
    private TextView tvPolygon, tvWritablePath, tvAuthBypass, tvUserPrivileges, tvUploader, tvExtractLinks;
    // Row 4
    private TextView tvFind, tvViewSource, tvPostData, tvTamperData, tvJavascript;
    private TextView tvNoRedirect, tvAdminFinder, tvAdminScanner, tvWebTools, tvUserAgent, tvCookieEditor;

    // State
    private SharedPreferences prefs;
    private String currentUrl = "";
    private boolean toolbarVisible = true;
    private boolean findBarVisible = false;
    private boolean postDataVisible = false;
    private boolean noRedirectEnabled = false;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hackbar); // Original layout file name

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        bindViews();
        setupWebView();
        setupToolbarClicks();
        setupFindInPage();
        applyUserPreferences();
        loadInitialUrl();
    }

    private void bindViews() {
        background       = findViewById(R.id.background);
        topbar           = findViewById(R.id.topbar);
        inputLayout      = findViewById(R.id.inputlayout);
        findLayout       = findViewById(R.id.findlayout);
        webviewContainer = findViewById(R.id.webviewcontainer);
        pageTitleLayout  = findViewById(R.id.pagetitlelayout);

        urlField         = findViewById(R.id.urlfield);
        paramsField      = findViewById(R.id.paramsfield);
        findField        = findViewById(R.id.findfield);

        appName          = findViewById(R.id.appname);
        findCount        = findViewById(R.id.findcount);
        pageTitle        = findViewById(R.id.pagetitle);
        pageFavicon      = findViewById(R.id.pagefavicon);
        progressBar      = findViewById(R.id.progressbar);
        webView          = findViewById(R.id.webview);

        btnUndo          = findViewById(R.id.undo);
        btnRedo          = findViewById(R.id.redo);
        btnNote          = findViewById(R.id.note);
        btnShowHide      = findViewById(R.id.showhide);
        btnMenu          = findViewById(R.id.menu);
        btnFindPrev      = findViewById(R.id.findprev);
        btnFindNext      = findViewById(R.id.findnext);
        btnFindClose     = findViewById(R.id.findclose);

        // Row 1
        tvBack      = findViewById(R.id.back);
        tvForward   = findViewById(R.id.forward);
        tvClear     = findViewById(R.id.clear);
        tvExecute   = findViewById(R.id.execute);
        tvReload    = findViewById(R.id.reload);
        tvStop      = findViewById(R.id.stop);
        tvCopy      = findViewById(R.id.copy);
        tvPaste     = findViewById(R.id.paste);

        // Row 2
        tvColumnCount     = findViewById(R.id.columncount);
        tvUnionStatements = findViewById(R.id.unionstatements);
        tvBasicStatements = findViewById(R.id.basicstatements);
        tvDios            = findViewById(R.id.dios);
        tvLocalVariable   = findViewById(R.id.localvariable);
        tvErrorBased      = findViewById(R.id.errorbased);
        tvPrintSystem     = findViewById(R.id.printsystem);
        tvDoubleQuery     = findViewById(R.id.doublequery);
        tvXpathInjection  = findViewById(R.id.xpathinjection);
        tvMssql           = findViewById(R.id.mssql);
        tvPostgresql      = findViewById(R.id.postgresql);
        tvLfi             = findViewById(R.id.lfi);
        tvRce             = findViewById(R.id.rce);
        tvXss             = findViewById(R.id.xss);
        tvCustomQuery     = findViewById(R.id.customquery);

        // Row 3
        tvReplace           = findViewById(R.id.replace);
        tvWafBypass         = findViewById(R.id.wafbypass);
        tvOrderByBypass     = findViewById(R.id.orderbybypass);
        tvUnionSelectBypass = findViewById(R.id.unionselectbypass);
        tvUrlBalancer       = findViewById(R.id.urlbalancer);
        tvPolygon           = findViewById(R.id.polygon);
        tvWritablePath      = findViewById(R.id.writablepath);
        tvAuthBypass        = findViewById(R.id.authbypass);
        tvUserPrivileges    = findViewById(R.id.userprivileges);
        tvUploader          = findViewById(R.id.uploader);
        tvExtractLinks      = findViewById(R.id.extractlinks);

        // Row 4
        tvFind           = findViewById(R.id.find);
        tvViewSource     = findViewById(R.id.viewsource);
        tvPostData       = findViewById(R.id.postdata);
        tvTamperData     = findViewById(R.id.tamperdata);
        tvJavascript     = findViewById(R.id.javascript);
        tvNoRedirect     = findViewById(R.id.noredirect);
        tvAdminFinder    = findViewById(R.id.adminfinder);
        tvAdminScanner   = findViewById(R.id.adminscanner);
        tvWebTools       = findViewById(R.id.webtools);
        tvUserAgent      = findViewById(R.id.useragent);
        tvCookieEditor   = findViewById(R.id.cookieeditor);
        
        // Hide elements that start hidden
        findLayout.setVisibility(View.GONE);
        paramsField.setVisibility(View.GONE);
        findCount.setVisibility(View.GONE);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);

        applyTextSizeToWebView(settings);
        webView.setWebViewClient(new HackBarWebViewClient());
        webView.setWebChromeClient(new HackBarWebChromeClient());

        urlField.setOnEditorActionListener((v, actionId, event) -> {
            loadUrlFromBar();
            return true;
        });

        // Top bar
        btnUndo.setOnClickListener(v -> { if (webView.canGoBack()) webView.goBack(); });
        btnRedo.setOnClickListener(v -> { if (webView.canGoForward()) webView.goForward(); });
        btnNote.setOnClickListener(v -> openCustomQueries());
        btnShowHide.setOnClickListener(v -> toggleToolbarVisibility());
        btnMenu.setOnClickListener(v -> showAppMenu());
    }

    private void applyTextSizeToWebView(WebSettings settings) {
        int size = Integer.parseInt(prefs.getString(PREF_TEXT_SIZE, "1"));
        int[] textZoomValues = {75, 100, 125, 150};
        int zoom = (size >= 0 && size < textZoomValues.length) ? textZoomValues[size] : 100;
        settings.setTextZoom(zoom);
    }

    private void setupToolbarClicks() {
        // Row 1
        tvBack.setOnClickListener(v -> { if (webView.canGoBack()) webView.goBack(); });
        tvForward.setOnClickListener(v -> { if (webView.canGoForward()) webView.goForward(); });
        tvClear.setOnClickListener(v -> urlField.setText(""));
        tvExecute.setOnClickListener(v -> loadUrlFromBar());
        tvReload.setOnClickListener(v -> webView.reload());
        tvStop.setOnClickListener(v -> webView.stopLoading());
        tvCopy.setOnClickListener(v -> copyCurrentUrl());
        tvPaste.setOnClickListener(v -> pasteUrl());

        // Row 2
        tvColumnCount.setOnClickListener(v -> appendToUrl(" ORDER BY 1--"));
        tvUnionStatements.setOnClickListener(v -> showInjectionMenu("union"));
        tvBasicStatements.setOnClickListener(v -> showInjectionMenu("sqli"));
        tvDios.setOnClickListener(v -> showInjectionMenu("dios"));
        tvLocalVariable.setOnClickListener(v -> showInjectionMenu("local_variable"));
        tvErrorBased.setOnClickListener(v -> showInjectionMenu("error"));
        tvPrintSystem.setOnClickListener(v -> showInjectionMenu("print_system"));
        tvDoubleQuery.setOnClickListener(v -> showInjectionMenu("double_query"));
        tvXpathInjection.setOnClickListener(v -> showInjectionMenu("xpath"));
        tvMssql.setOnClickListener(v -> showInjectionMenu("mssql"));
        tvPostgresql.setOnClickListener(v -> showInjectionMenu("postgresql"));
        tvLfi.setOnClickListener(v -> showInjectionMenu("lfi"));
        tvRce.setOnClickListener(v -> showInjectionMenu("rce"));
        tvXss.setOnClickListener(v -> showInjectionMenu("xss"));
        tvCustomQuery.setOnClickListener(v -> openCustomQueries());

        // Row 3
        tvReplace.setOnClickListener(v -> showInjectionMenu("replace"));
        tvWafBypass.setOnClickListener(v -> showInjectionMenu("waf_bypass"));
        tvOrderByBypass.setOnClickListener(v -> appendToUrl(" ORDER BY 1--"));
        tvUnionSelectBypass.setOnClickListener(v -> showInjectionMenu("union_select_bypass"));
        tvUrlBalancer.setOnClickListener(v -> appendToUrl("%23"));
        tvPolygon.setOnClickListener(v -> appendToUrl("'*'"));
        tvWritablePath.setOnClickListener(v -> appendToUrl("' INTO OUTFILE '/var/www/html/shell.php'--"));
        tvAuthBypass.setOnClickListener(v -> showInjectionMenu("auth_bypass"));
        tvUserPrivileges.setOnClickListener(v -> appendToUrl("' AND (SELECT 1 FROM (SELECT COUNT(*),CONCAT(0x3a,(SELECT (ELT(1=1,1))),FLOOR(RAND(0)*2))x FROM INFORMATION_SCHEMA.PLUGINS GROUP BY x)a)--"));
        tvUploader.setOnClickListener(v -> showInjectionMenu("uploader"));
        tvExtractLinks.setOnClickListener(v -> runJavascript("var links=[]; document.querySelectorAll('a[href]').forEach(a=>links.push(a.href)); alert(links.join('\\n'));"));

        // Row 4
        tvFind.setOnClickListener(v -> toggleFindBar());
        tvViewSource.setOnClickListener(v -> viewPageSource());
        tvPostData.setOnClickListener(v -> togglePostData());
        tvTamperData.setOnClickListener(v -> showToast("Header modification is handled via WebViewClient. Enable No-Redirect to intercept responses."));
        tvJavascript.setOnClickListener(v -> showJavascriptDialog());
        tvNoRedirect.setOnClickListener(v -> toggleNoRedirect());
        tvAdminFinder.setOnClickListener(v -> openAdminFinder(false));
        tvAdminScanner.setOnClickListener(v -> openAdminFinder(true));
        tvWebTools.setOnClickListener(v -> openWebTools());
        tvUserAgent.setOnClickListener(v -> showUserAgentDialog());
        tvCookieEditor.setOnClickListener(v -> openCookieEditor());
    }

    private void setupFindInPage() {
        findField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                webView.findAllAsync(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        btnFindPrev.setOnClickListener(v -> webView.findNext(false));
        btnFindNext.setOnClickListener(v -> webView.findNext(true));
        btnFindClose.setOnClickListener(v -> {
            findBarVisible = false;
            findLayout.setVisibility(View.GONE);
            webView.clearMatches();
        });
    }

    private void toggleFindBar() {
        findBarVisible = !findBarVisible;
        findLayout.setVisibility(findBarVisible ? View.VISIBLE : View.GONE);
        if (findBarVisible) findField.requestFocus();
        else webView.clearMatches();
    }

    private void showAppMenu() {
        String[] items = {"Settings", "About", "Share App", "Copy URL", "Refresh"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("HackBar")
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0: openSettings(); break;
                        case 1: openAbout(); break;
                        case 2: shareApp(); break;
                        case 3: copyCurrentUrl(); break;
                        case 4: webView.reload(); break;
                    }
                })
                .show();
    }

    private void loadInitialUrl() {
        String url = getIntent().getStringExtra(EXTRA_INITIAL_URL);
        if (url == null || url.isEmpty()) {
            if (prefs.getString(PREF_SAVE_URL, "").equals("true")) {
                url = prefs.getString(PREF_LAST_URL, "");
            }
        }
        if (url != null && !url.isEmpty()) {
            urlField.setText(url);
            loadUrl(url);
        }
    }

    private void loadUrlFromBar() {
        String url = urlField.getText().toString().trim();
        if (url.isEmpty()) return;
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
            urlField.setText(url);
        }
        loadUrl(url);
    }

    private void loadUrl(String url) {
        currentUrl = url;
        if (postDataVisible && paramsField.getText().length() > 0) {
            String postData = paramsField.getText().toString();
            try {
                webView.postUrl(url, postData.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                webView.loadUrl(url);
            }
        } else {
            webView.loadUrl(url);
        }
    }

    private void appendToUrl(String payload) {
        String current = urlField.getText().toString();
        urlField.setText(current + payload);
        urlField.setSelection(urlField.getText().length());
    }

    private void showInjectionMenu(String category) {
        String[] payloads = PayloadLibrary.getPayloads(category);
        if (payloads == null || payloads.length == 0) {
            showToast("No payloads for: " + category); return;
        }
        
        new MaterialAlertDialogBuilder(this)
                .setTitle(category.toUpperCase().replace("_", " "))
                .setItems(payloads, (dialog, which) -> {
                    appendToUrl(payloads[which]);
                    vibrate();
                })
                .show();
    }

    private void showJavascriptDialog() {
        EditText input = new EditText(this);
        input.setHint("javascript:alert(1)");
        new MaterialAlertDialogBuilder(this)
                .setTitle("Execute JavaScript")
                .setView(input)
                .setPositiveButton("Run", (d, w) -> {
                    String js = input.getText().toString().trim();
                    if (!js.isEmpty()) runJavascript(js);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void runJavascript(String script) {
        webView.getSettings().setJavaScriptEnabled(true);
        if (script.startsWith("javascript:")) {
            webView.loadUrl(script);
        } else {
            webView.evaluateJavascript(script, null);
        }
    }

    private void showUserAgentDialog() {
        String[] agents = {
            "Default Browser", "Mozilla/5.0 Chrome", "Googlebot", "Safari iPhone", "Custom..."
        };
        new MaterialAlertDialogBuilder(this)
                .setTitle("User Agent")
                .setItems(agents, (d, which) -> {
                    if (which == 0) {
                        webView.getSettings().setUserAgentString(null);
                    } else if (which < agents.length - 1) {
                        webView.getSettings().setUserAgentString(agents[which]);
                    }
                    if (which != agents.length - 1) webView.reload();
                })
                .show();
    }

    private void toggleNoRedirect() {
        noRedirectEnabled = !noRedirectEnabled;
        showToast("No Redirect: " + (noRedirectEnabled ? "ON" : "OFF"));
    }

    private void togglePostData() {
        postDataVisible = !postDataVisible;
        paramsField.setVisibility(postDataVisible ? View.VISIBLE : View.GONE);
    }

    private void toggleToolbarVisibility() {
        toolbarVisible = !toolbarVisible;
        findViewById(R.id.row1scroll).setVisibility(toolbarVisible ? View.VISIBLE : View.GONE);
        findViewById(R.id.row2scroll).setVisibility(toolbarVisible ? View.VISIBLE : View.GONE);
        findViewById(R.id.row3scroll).setVisibility(toolbarVisible ? View.VISIBLE : View.GONE);
        findViewById(R.id.row4scroll).setVisibility(toolbarVisible ? View.VISIBLE : View.GONE);
    }

    private void viewPageSource() {
        String sourceUrl = "view-source:" + webView.getUrl();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl)));
        } catch (Exception e) {
            webView.evaluateJavascript("document.documentElement.outerHTML", value -> {
                mainHandler.post(() -> new MaterialAlertDialogBuilder(this)
                        .setTitle("Page Source")
                        .setMessage(value)
                        .setPositiveButton("Copy", (d, w) -> copyToClipboard(value))
                        .setNegativeButton("Close", null)
                        .show());
            });
        }
    }

    private void openCustomQueries() {
        startActivity(new Intent(this, CustomqueryActivity.class));
    }
    private void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }
    private void openAbout() {
        startActivity(new Intent(this, AboutActivity.class));
    }
    private void openAdminFinder(boolean scanner) {
        Intent intent = new Intent(this, AdminActivity.class);
        intent.putExtra("mode", scanner ? "scanner" : "finder");
        intent.putExtra(EXTRA_INITIAL_URL, currentUrl);
        startActivity(intent);
    }
    private void openWebTools() {
        Intent intent = new Intent(this, WebtoolActivity.class);
        intent.putExtra("url", "https://hackertarget.com/find-dns-host-records");
        startActivity(intent);
    }

    private void shareApp() {
        try {
            File apkFile = new File(getApplicationInfo().sourceDir);
            File destDir = new File(getExternalCacheDir(), "HackBar");
            if (!destDir.exists()) destDir.mkdirs();
            File destFile = new File(destDir, "DH-HackBar.apk");

            executor.execute(() -> {
                try (FileInputStream in = new FileInputStream(apkFile);
                     FileOutputStream out = new FileOutputStream(destFile)) {
                    byte[] buf = new byte[4096];
                    int len;
                    while ((len = in.read(buf)) > 0) out.write(buf, 0, len);

                    mainHandler.post(() -> {
                        Uri apkUri = FileProvider.getUriForFile(
                                this, getPackageName() + ".fileprovider", destFile);
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("application/vnd.android.package-archive");
                        share.putExtra(Intent.EXTRA_STREAM, apkUri);
                        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(share, "Share DH HackBar"));
                    });
                } catch (IOException ignored) {}
            });
        } catch (Exception ignored) {}
    }

    private void copyCurrentUrl() {
        copyToClipboard(webView.getUrl() != null ? webView.getUrl() : urlField.getText().toString());
    }
    private void pasteUrl() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null && cm.hasPrimaryClip() && cm.getPrimaryClip().getItemCount() > 0) {
            urlField.setText(cm.getPrimaryClip().getItemAt(0).getText());
        }
    }
    private void copyToClipboard(String text) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("HackBar", text));
            showToast("Copied to clipboard");
        }
    }
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("deprecation")
    private void vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vm = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            if (vm != null) vm.getDefaultVibrator().vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) v.vibrate(30);
        }
    }

    private void applyUserPreferences() {
        if (prefs.getString(PREF_SAVE_URL, "").equals("true")) {
            String lastUrl = prefs.getString(PREF_LAST_URL, "");
            if (!lastUrl.isEmpty()) urlField.setText(lastUrl);
        }
    }

    private class HackBarWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            if (noRedirectEnabled && !url.equals(currentUrl)) {
                showToast("Redirect intercepted → " + url);
                return true; 
            }
            currentUrl = url;
            urlField.setText(url);
            if (prefs.getString(PREF_SAVE_URL, "").equals("true")) {
                prefs.edit().putString(PREF_LAST_URL, url).apply();
            }
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
            urlField.setText(url);
            currentUrl = url;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            pageTitle.setText(view.getTitle());
        }
    }

    private class HackBarWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress < 100) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(newProgress);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        }
        @Override
        public void onReceivedTitle(WebView view, String title) {
            pageTitle.setText(title);
        }
    }

    @Override
    public void onBackPressed() {
        if (findBarVisible) {
            toggleFindBar();
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.stopLoading();
            webView.clearHistory();
            webView.removeAllViews();
            webView.destroy();
        }
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }
    private void openCookieEditor() {
        if (TextUtils.isEmpty(currentUrl)) {
            showToast("Please load a URL first.");
            return;
        }
        Intent intent = new Intent(this, CookieEditorActivity.class);
        intent.putExtra(CookieEditorActivity.EXTRA_URL, currentUrl);
        startActivityForResult(intent, 1002);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1002 && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("RELOAD", false)) {
                webView.reload();
            }
        }
    }
}
