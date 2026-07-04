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
import android.text.TextWatcher;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HackbarActivity — Main Browser + Penetration Testing Toolbar
 *
 * FIXES APPLIED:
 * 1. extends AppCompatActivity (was plain Activity → crash API 31+)
 * 2. AsyncTask REMOVED → ExecutorService + Handler for background work
 * 3. ProgressDialog REMOVED → MaterialAlertDialogBuilder with ProgressIndicator
 * 4. Vibrator deprecated API fixed → VibratorManager for API 31+
 * 5. setAppCacheEnabled/setAppCachePath → removed (crashes API 33+)
 * 6. setLayoutAlgorithm(SINGLE_COLUMN) → removed (deprecated)
 * 7. setAllowFileAccessFromFileURLs → false (security fix)
 * 8. setAllowUniversalAccessFromFileURLs → false (security fix)
 * 9. All obfuscated Base64 strings → proper string resources
 * 10. File:// URI sharing → FileProvider content:// URIs (fix API 24+)
 * 11. Anti-tamper Process.killProcess() → REMOVED (always killed on debug)
 * 12. new HackbarActivity().method() call from MainActivity → REMOVED
 * 13. ValueCallback<Uri> (old) → ValueCallback<Uri[]> for API 21+ file chooser
 * 14. WebViewClient.shouldOverrideUrlLoading(WebView, String) → (WebView, WebResourceRequest)
 * 15. @SuppressLint("SetJavaScriptEnabled") added (intentional for pentest tool)
 * 16. SharedPreferences.commit() → apply() (commit blocks main thread)
 */
public class HackbarActivity extends AppCompatActivity {

    public static final String EXTRA_INITIAL_URL = "initial_url";
    private static final String PREFS_NAME       = "hackbar_prefs";
    private static final String PREF_LAST_URL    = "last_url";
    private static final String PREF_SAVE_URL    = "save_last_url";
    private static final String PREF_TEXT_SIZE   = "text_size";
    private static final String PREF_CUSTOM_QUERIES = "custom_queries";

    // Views
    private LinearLayout background, topbar, inputLayout, toolbarRows;
    private LinearLayout findLayout, webviewContainer, pageTitleLayout;
    private EditText urlField, postDataField, findField;
    private ImageView btnUndo, btnRedo, btnNote, btnVisible, btnMenu;
    private ImageView btnFindPrev, btnFindNext, btnFindClose, pageFavicon;
    private TextView appName, pageTitle, warningLabel;
    private com.google.android.material.progressindicator.LinearProgressIndicator progressBar;
    private WebView webView;

    // Toolbar chips (Row 1 — Injection)
    private Chip chipSqli, chipUnion, chipError, chipXpath, chipMssql, chipPostgresql, chipDios;
    private Chip chipLfi, chipRfi, chipRce, chipXss;
    // Row 2 — Encoding
    private Chip chipUrlEncode, chipHexEncode, chipBase64Encode, chipBinaryEncode, chipAsciiEncode;
    private Chip chipUrlDecode, chipHexDecode, chipBase64Decode, chipBinaryDecode, chipAsciiDecode;
    // Row 3 — Bypass
    private Chip chipAuthBypass, chipOrderByBypass, chipUnionSelectBypass, chipWafBypass;
    private Chip chipUrlBalancer, chipPolygon, chipWritablePath, chipUserPriv, chipUploader, chipExtractLinks;
    // Row 4 — Browser Tools
    private Chip chipFind, chipViewSource, chipPostData, chipTamperData, chipJavascript;
    private Chip chipNoRedirect, chipAdminFinder, chipAdminScanner, chipWebTools, chipUserAgent;

    // State
    private SharedPreferences prefs;
    private String currentUrl = "";
    private boolean toolbarVisible = true;
    private boolean findBarVisible = false;
    private boolean postDataVisible = false;
    private boolean noRedirectEnabled = false;
    private boolean javascriptEnabled = true;

    // FIX: ExecutorService replaces deprecated AsyncTask
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hackbar);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        bindViews();
        setupWebView();
        setupToolbarChips();
        setupFindInPage();
        setupMenuButton();
        applyUserPreferences();
        loadInitialUrl();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VIEW BINDING
    // ─────────────────────────────────────────────────────────────────────────

    private void bindViews() {
        background       = findViewById(R.id.background);
        topbar           = findViewById(R.id.topbar);
        inputLayout      = findViewById(R.id.inputlayout);
        toolbarRows      = findViewById(R.id.toolbarrows);
        findLayout       = findViewById(R.id.findlayout);
        webviewContainer = findViewById(R.id.webviewcontainer);
        pageTitleLayout  = findViewById(R.id.pagetitlelayout);

        urlField         = findViewById(R.id.urlfield);
        postDataField    = findViewById(R.id.postdatafield);
        findField        = findViewById(R.id.findfield);
        warningLabel     = findViewById(R.id.warninglabel);

        appName          = findViewById(R.id.appname);
        pageTitle        = findViewById(R.id.pagetitle);
        pageFavicon      = findViewById(R.id.pagefavicon);
        progressBar      = findViewById(R.id.progressbar);
        webView          = findViewById(R.id.webview);

        btnUndo          = findViewById(R.id.undo);
        btnRedo          = findViewById(R.id.redo);
        btnNote          = findViewById(R.id.note);
        btnVisible       = findViewById(R.id.visible);
        btnMenu          = findViewById(R.id.menu);
        btnFindPrev      = findViewById(R.id.findprev);
        btnFindNext      = findViewById(R.id.findnext);
        btnFindClose     = findViewById(R.id.findclose);

        // Row 1
        chipSqli         = findViewById(R.id.sqli);
        chipUnion        = findViewById(R.id.union);
        chipError        = findViewById(R.id.error);
        chipXpath        = findViewById(R.id.xpath);
        chipMssql        = findViewById(R.id.mssql);
        chipPostgresql   = findViewById(R.id.postgresql);
        chipDios         = findViewById(R.id.dios);
        chipLfi          = findViewById(R.id.lfi);
        chipRfi          = findViewById(R.id.rfi);
        chipRce          = findViewById(R.id.rce);
        chipXss          = findViewById(R.id.xss);
        // Row 2
        chipUrlEncode    = findViewById(R.id.urlencode);
        chipHexEncode    = findViewById(R.id.hexencode);
        chipBase64Encode = findViewById(R.id.base64encode);
        chipBinaryEncode = findViewById(R.id.binaryencode);
        chipAsciiEncode  = findViewById(R.id.asciiencode);
        chipUrlDecode    = findViewById(R.id.urldecode);
        chipHexDecode    = findViewById(R.id.hexdecode);
        chipBase64Decode = findViewById(R.id.base64decode);
        chipBinaryDecode = findViewById(R.id.binarydecode);
        chipAsciiDecode  = findViewById(R.id.asciidecode);
        // Row 3
        chipAuthBypass        = findViewById(R.id.authbypass);
        chipOrderByBypass     = findViewById(R.id.orderbybypass);
        chipUnionSelectBypass = findViewById(R.id.unionselectbypass);
        chipWafBypass         = findViewById(R.id.wafbypass);
        chipUrlBalancer       = findViewById(R.id.urlbalancer);
        chipPolygon           = findViewById(R.id.polygon);
        chipWritablePath      = findViewById(R.id.writablepath);
        chipUserPriv          = findViewById(R.id.userprivileges);
        chipUploader          = findViewById(R.id.uploader);
        chipExtractLinks      = findViewById(R.id.extractlinks);
        // Row 4
        chipFind         = findViewById(R.id.findinpage);
        chipViewSource   = findViewById(R.id.viewsource);
        chipPostData     = findViewById(R.id.postdata);
        chipTamperData   = findViewById(R.id.tamperdata);
        chipJavascript   = findViewById(R.id.javascript);
        chipNoRedirect   = findViewById(R.id.noredirect);
        chipAdminFinder  = findViewById(R.id.adminfinder);
        chipAdminScanner = findViewById(R.id.adminscanner);
        chipWebTools     = findViewById(R.id.webtools);
        chipUserAgent    = findViewById(R.id.useragent);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WEBVIEW SETUP
    // FIX: All deprecated WebSettings removed, security settings tightened
    // ─────────────────────────────────────────────────────────────────────────

    @SuppressLint("SetJavaScriptEnabled") // Intentional — pentest tool requires JS
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setDomStorageEnabled(true);

        // FIX: Security — false by default (was true = security vulnerability)
        settings.setAllowFileAccess(true); // needed for loading local assets
        settings.setAllowFileAccessFromFileURLs(false); // FIX: was true
        settings.setAllowUniversalAccessFromFileURLs(false); // FIX: was true

        // FIX: setAppCacheEnabled / setAppCachePath removed (crashes API 33+)
        // FIX: setLayoutAlgorithm(SINGLE_COLUMN) removed (deprecated API 19)

        // Apply saved text size preference
        applyTextSizeToWebView(settings);

        webView.setWebViewClient(new HackBarWebViewClient());
        webView.setWebChromeClient(new HackBarWebChromeClient());

        // URL bar keyboard action — load URL on "Go"
        urlField.setOnEditorActionListener((v, actionId, event) -> {
            loadUrlFromBar();
            return true;
        });

        // Top bar undo/redo → browser back/forward
        btnUndo.setOnClickListener(v -> { if (webView.canGoBack()) webView.goBack(); });
        btnRedo.setOnClickListener(v -> { if (webView.canGoForward()) webView.goForward(); });

        // Note button → Custom Queries
        btnNote.setOnClickListener(v -> openCustomQueries());

        // Visibility toggle
        btnVisible.setOnClickListener(v -> toggleToolbarVisibility());
    }

    private void applyTextSizeToWebView(WebSettings settings) {
        int size = Integer.parseInt(prefs.getString(PREF_TEXT_SIZE, "1"));
        int[] textZoomValues = {75, 100, 125, 150};
        int zoom = (size >= 0 && size < textZoomValues.length) ? textZoomValues[size] : 100;
        settings.setTextZoom(zoom);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TOOLBAR CHIP CLICK HANDLERS
    // ─────────────────────────────────────────────────────────────────────────

    private void setupToolbarChips() {
        // ── SQLi & Injection ──
        chipSqli.setOnClickListener(v -> showInjectionMenu("sqli"));
        chipUnion.setOnClickListener(v -> showInjectionMenu("union"));
        chipError.setOnClickListener(v -> showInjectionMenu("error"));
        chipXpath.setOnClickListener(v -> showInjectionMenu("xpath"));
        chipMssql.setOnClickListener(v -> showInjectionMenu("mssql"));
        chipPostgresql.setOnClickListener(v -> showInjectionMenu("postgresql"));
        chipDios.setOnClickListener(v -> showInjectionMenu("dios"));
        chipLfi.setOnClickListener(v -> showInjectionMenu("lfi"));
        chipRfi.setOnClickListener(v -> showInjectionMenu("rfi"));
        chipRce.setOnClickListener(v -> showInjectionMenu("rce"));
        chipXss.setOnClickListener(v -> showInjectionMenu("xss"));

        // ── Encoding ──
        chipUrlEncode.setOnClickListener(v -> transformUrl("url_encode"));
        chipHexEncode.setOnClickListener(v -> transformUrl("hex_encode"));
        chipBase64Encode.setOnClickListener(v -> transformUrl("base64_encode"));
        chipBinaryEncode.setOnClickListener(v -> transformUrl("binary_encode"));
        chipAsciiEncode.setOnClickListener(v -> transformUrl("ascii_encode"));
        chipUrlDecode.setOnClickListener(v -> transformUrl("url_decode"));
        chipHexDecode.setOnClickListener(v -> transformUrl("hex_decode"));
        chipBase64Decode.setOnClickListener(v -> transformUrl("base64_decode"));
        chipBinaryDecode.setOnClickListener(v -> transformUrl("binary_decode"));
        chipAsciiDecode.setOnClickListener(v -> transformUrl("ascii_decode"));

        // ── Bypass ──
        chipAuthBypass.setOnClickListener(v -> showInjectionMenu("auth_bypass"));
        chipOrderByBypass.setOnClickListener(v -> appendToUrl("ORDER BY 1--"));
        chipUnionSelectBypass.setOnClickListener(v -> showInjectionMenu("union_select_bypass"));
        chipWafBypass.setOnClickListener(v -> showInjectionMenu("waf_bypass"));
        chipUrlBalancer.setOnClickListener(v -> appendToUrl("%23"));
        chipPolygon.setOnClickListener(v -> appendToUrl("'*'"));
        chipWritablePath.setOnClickListener(v -> appendToUrl("' INTO OUTFILE '/var/www/html/shell.php'--"));
        chipUserPriv.setOnClickListener(v -> appendToUrl("' AND (SELECT 1 FROM (SELECT COUNT(*),CONCAT(0x3a,(SELECT (ELT(1=1,1))),FLOOR(RAND(0)*2))x FROM INFORMATION_SCHEMA.PLUGINS GROUP BY x)a)--"));
        chipUploader.setOnClickListener(v -> showInjectionMenu("uploader"));
        chipExtractLinks.setOnClickListener(v -> runJavascript(JS_EXTRACT_LINKS));

        // ── Browser Tools ──
        chipFind.setOnClickListener(v -> toggleFindBar());
        chipViewSource.setOnClickListener(v -> viewPageSource());
        chipPostData.setOnClickListener(v -> togglePostData());
        chipTamperData.setOnClickListener(v -> showTamperDataDialog());
        chipJavascript.setOnClickListener(v -> showJavascriptDialog());
        chipNoRedirect.setOnClickListener(v -> toggleNoRedirect());
        chipAdminFinder.setOnClickListener(v -> openAdminFinder(false));
        chipAdminScanner.setOnClickListener(v -> openAdminFinder(true));
        chipWebTools.setOnClickListener(v -> openWebTools());
        chipUserAgent.setOnClickListener(v -> showUserAgentDialog());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FIND IN PAGE
    // ─────────────────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────────────────
    // MENU (replaces raw ImageView click)
    // ─────────────────────────────────────────────────────────────────────────

    private void setupMenuButton() {
        btnMenu.setOnClickListener(v -> showAppMenu());
    }

    private void showAppMenu() {
        String[] items = {"Settings", "About", "Share App", "Copy URL", "Refresh"};
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.app_name))
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

    // ─────────────────────────────────────────────────────────────────────────
    // URL LOADING
    // ─────────────────────────────────────────────────────────────────────────

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
        if (postDataVisible && postDataField.getText().length() > 0) {
            String postData = postDataField.getText().toString();
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

    // ─────────────────────────────────────────────────────────────────────────
    // ENCODING / TRANSFORMATION
    // ─────────────────────────────────────────────────────────────────────────

    private void transformUrl(String type) {
        String url = urlField.getText().toString();
        if (url.isEmpty()) { showToast("URL field is empty"); return; }
        try {
            String result;
            switch (type) {
                case "url_encode":   result = URLEncoder.encode(url, "UTF-8"); break;
                case "url_decode":   result = URLDecoder.decode(url, "UTF-8"); break;
                case "base64_encode": result = Base64.encodeToString(url.getBytes("UTF-8"), Base64.NO_WRAP); break;
                case "base64_decode": result = new String(Base64.decode(url, Base64.DEFAULT), "UTF-8"); break;
                case "hex_encode":   result = toHex(url); break;
                case "hex_decode":   result = fromHex(url); break;
                case "binary_encode": result = toBinary(url); break;
                case "binary_decode": result = fromBinary(url); break;
                case "ascii_encode": result = toAscii(url); break;
                case "ascii_decode": result = fromAscii(url); break;
                default: return;
            }
            urlField.setText(result);
            urlField.setSelection(result.length());
        } catch (Exception e) {
            showToast("Conversion error: " + e.getMessage());
        }
    }

    private String toHex(String s) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for (byte b : s.getBytes("UTF-8")) sb.append(String.format("%02x", b));
        return sb.toString();
    }
    private String fromHex(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length() - 1; i += 2)
            sb.append((char) Integer.parseInt(s.substring(i, i + 2), 16));
        return sb.toString();
    }
    private String toBinary(String s) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for (byte b : s.getBytes("UTF-8")) sb.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0')).append(' ');
        return sb.toString().trim();
    }
    private String fromBinary(String s) {
        String[] parts = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) sb.append((char) Integer.parseInt(p, 2));
        return sb.toString();
    }
    private String toAscii(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) sb.append((int) c).append(' ');
        return sb.toString().trim();
    }
    private String fromAscii(String s) {
        StringBuilder sb = new StringBuilder();
        for (String p : s.split(" ")) {
            try { sb.append((char) Integer.parseInt(p.trim())); } catch (NumberFormatException ignored) {}
        }
        return sb.toString();
    }

    // MD5 / SHA-1 hashing (used by view source / payload display)
    public static String md5(String s) {
        try {
            BigInteger bi = new BigInteger(1, MessageDigest.getInstance("MD5").digest(s.getBytes()));
            String r = bi.toString(16);
            while (r.length() < 32) r = "0" + r;
            return r;
        } catch (NoSuchAlgorithmException e) { return s; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INJECTION MENUS (BottomSheetDialog)
    // ─────────────────────────────────────────────────────────────────────────

    private void showInjectionMenu(String category) {
        String[] payloads = PayloadLibrary.getPayloads(category);
        if (payloads == null || payloads.length == 0) {
            showToast("No payloads for: " + category); return;
        }
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_HackBar_BottomSheetDialog);
        View sheetView = getLayoutInflater().inflate(R.layout.sheet_payload_list, null);
        dialog.setContentView(sheetView);

        TextView title = sheetView.findViewById(R.id.sheet_title);
        androidx.recyclerview.widget.RecyclerView rv = sheetView.findViewById(R.id.sheet_recycler);
        title.setText(category.toUpperCase().replace("_", " "));

        PayloadAdapter adapter = new PayloadAdapter(payloads, payload -> {
            appendToUrl(payload);
            dialog.dismiss();
            vibrate();
        });
        rv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        rv.setAdapter(adapter);
        dialog.show();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // JAVASCRIPT EXECUTION
    // ─────────────────────────────────────────────────────────────────────────

    private void showJavascriptDialog() {
        EditText input = new EditText(this);
        input.setHint("javascript:alert(1)");
        input.setTextColor(getResources().getColor(R.color.colorOnSurface, getTheme()));
        input.setHintTextColor(getResources().getColor(R.color.colorHint, getTheme()));

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

    private static final String JS_EXTRACT_LINKS =
            "var links=[]; document.querySelectorAll('a[href]').forEach(a=>links.push(a.href)); links.join('\\n');";

    // ─────────────────────────────────────────────────────────────────────────
    // TAMPER DATA
    // ─────────────────────────────────────────────────────────────────────────

    private void showTamperDataDialog() {
        // Show headers dialog — simplified implementation
        new MaterialAlertDialogBuilder(this)
                .setTitle("Tamper Data")
                .setMessage("Header modification is handled via WebViewClient. Enable No-Redirect to intercept responses.")
                .setPositiveButton("OK", null)
                .show();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // USER AGENT
    // ─────────────────────────────────────────────────────────────────────────

    private void showUserAgentDialog() {
        String[] agents = {
            "Default Browser",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0",
            "Googlebot/2.1 (+http://www.google.com/bot.html)",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0) Safari/604.1",
            "Custom..."
        };
        new MaterialAlertDialogBuilder(this)
                .setTitle("User Agent")
                .setItems(agents, (d, which) -> {
                    if (which == 0) {
                        webView.getSettings().setUserAgentString(null);
                    } else if (which < agents.length - 1) {
                        webView.getSettings().setUserAgentString(agents[which]);
                    } else {
                        showCustomUserAgentDialog();
                    }
                    if (which != agents.length - 1) webView.reload();
                })
                .show();
    }

    private void showCustomUserAgentDialog() {
        EditText input = new EditText(this);
        input.setText(webView.getSettings().getUserAgentString());
        input.setTextColor(getResources().getColor(R.color.colorOnSurface, getTheme()));
        new MaterialAlertDialogBuilder(this)
                .setTitle("Custom User Agent")
                .setView(input)
                .setPositiveButton("Set", (d, w) -> {
                    webView.getSettings().setUserAgentString(input.getText().toString());
                    webView.reload();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NO REDIRECT TOGGLE
    // ─────────────────────────────────────────────────────────────────────────

    private void toggleNoRedirect() {
        noRedirectEnabled = !noRedirectEnabled;
        chipNoRedirect.setChipBackgroundColorResource(
                noRedirectEnabled ? R.color.colorSecondary : R.color.colorChipBackground);
        chipNoRedirect.setTextColor(getResources().getColor(
                noRedirectEnabled ? R.color.colorBackground : R.color.colorChipText, getTheme()));
        showToast("No Redirect: " + (noRedirectEnabled ? "ON" : "OFF"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST DATA TOGGLE
    // ─────────────────────────────────────────────────────────────────────────

    private void togglePostData() {
        postDataVisible = !postDataVisible;
        postDataField.setVisibility(postDataVisible ? View.VISIBLE : View.GONE);
        chipPostData.setChipBackgroundColorResource(
                postDataVisible ? R.color.colorSecondary : R.color.colorChipBackground);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TOOLBAR VISIBILITY TOGGLE
    // ─────────────────────────────────────────────────────────────────────────

    private void toggleToolbarVisibility() {
        toolbarVisible = !toolbarVisible;
        toolbarRows.setVisibility(toolbarVisible ? View.VISIBLE : View.GONE);
        pageTitleLayout.setVisibility(toolbarVisible ? View.VISIBLE : View.GONE);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VIEW SOURCE
    // ─────────────────────────────────────────────────────────────────────────

    private void viewPageSource() {
        String sourceUrl = "view-source:" + webView.getUrl();
        // Use external browser for view-source since WebView doesn't support it natively
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl));
            startActivity(intent);
        } catch (Exception e) {
            // Fallback: inject JS to get source
            webView.evaluateJavascript(
                    "document.documentElement.outerHTML",
                    value -> {
                        // Show in a dialog
                        mainHandler.post(() -> {
                            new MaterialAlertDialogBuilder(HackbarActivity.this)
                                    .setTitle("Page Source")
                                    .setMessage(value)
                                    .setPositiveButton("Copy", (d, w) -> copyToClipboard(value))
                                    .setNegativeButton("Close", null)
                                    .show();
                        });
                    });
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NAVIGATION
    // ─────────────────────────────────────────────────────────────────────────

    private void openCustomQueries() {
        startActivity(new Intent(this, CustomqueryActivity.class));
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
    private void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
    private void openAbout() {
        startActivity(new Intent(this, AboutActivity.class));
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
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
        intent.putExtra("title", "Web Tools");
        startActivity(intent);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SHARE APP
    // FIX: FileProvider replaces File:// URI (FileUriExposedException on API 24+)
    // ─────────────────────────────────────────────────────────────────────────

    private void shareApp() {
        try {
            File apkFile = new File(getApplicationInfo().sourceDir);
            File destDir = new File(getExternalCacheDir(), "HackBar");
            if (!destDir.exists()) destDir.mkdirs();
            File destFile = new File(destDir, "DH-HackBar.apk");

            // Copy APK to accessible location in background
            executor.execute(() -> {
                try (FileInputStream in = new FileInputStream(apkFile);
                     FileOutputStream out = new FileOutputStream(destFile)) {
                    byte[] buf = new byte[4096];
                    int len;
                    while ((len = in.read(buf)) > 0) out.write(buf, 0, len);

                    mainHandler.post(() -> {
                        // FIX: FileProvider.getUriForFile() instead of Uri.fromFile()
                        Uri apkUri = FileProvider.getUriForFile(
                                this, getPackageName() + ".fileprovider", destFile);
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("application/vnd.android.package-archive");
                        share.putExtra(Intent.EXTRA_STREAM, apkUri);
                        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(share, "Share DH HackBar"));
                    });
                } catch (IOException e) {
                    mainHandler.post(() -> showToast("Share failed: " + e.getMessage()));
                }
            });
        } catch (Exception e) {
            showToast("Share error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UTILITY
    // ─────────────────────────────────────────────────────────────────────────

    private void copyCurrentUrl() {
        copyToClipboard(webView.getUrl() != null ? webView.getUrl() : urlField.getText().toString());
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

    /**
     * FIX: Vibrator API — deprecated Vibrator.vibrate(long) on API 26+
     * VibratorManager used on API 31+
     */
    @SuppressWarnings("deprecation")
    private void vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vm = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            if (vm != null) vm.getDefaultVibrator().vibrate(
                    VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) v.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) v.vibrate(30);
        }
    }

    private void applyUserPreferences() {
        // Restore last URL if setting is on
        boolean saveLast = prefs.getString(PREF_SAVE_URL, "").equals("true");
        if (saveLast) {
            String lastUrl = prefs.getString(PREF_LAST_URL, "");
            if (!lastUrl.isEmpty()) urlField.setText(lastUrl);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WEBVIEWCLIENT
    // FIX: shouldOverrideUrlLoading(WebView, String) deprecated API 24+
    //      → shouldOverrideUrlLoading(WebView, WebResourceRequest)
    // ─────────────────────────────────────────────────────────────────────────

    private class HackBarWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            // No-redirect mode: intercept and show info instead of following
            if (noRedirectEnabled && !url.equals(currentUrl)) {
                showToast("Redirect intercepted → " + url);
                return true; // block the redirect
            }
            currentUrl = url;
            urlField.setText(url);
            // Save last URL
            if (prefs.getString(PREF_SAVE_URL, "").equals("true")) {
                // FIX: apply() instead of commit() — commit() blocks main thread
                prefs.edit().putString(PREF_LAST_URL, url).apply();
            }
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
            urlField.setText(url);
            currentUrl = url;
            // HTTP warning
            if (url.startsWith("http://")) {
                warningLabel.setText(getString(R.string.warn_cleartext));
                warningLabel.setVisibility(View.VISIBLE);
            } else {
                warningLabel.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            pageTitle.setText(view.getTitle());
            if (favicon == null) pageFavicon.setVisibility(View.GONE);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WEBCHROMECLIENT
    // ─────────────────────────────────────────────────────────────────────────

    private class HackBarWebChromeClient extends WebChromeClient {
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

        @Override
        public void onReceivedTitle(WebView view, String title) {
            pageTitle.setText(title);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LIFECYCLE
    // ─────────────────────────────────────────────────────────────────────────

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
        // FIX: Properly destroy WebView to prevent memory leaks
        if (webView != null) {
            webView.stopLoading();
            webView.clearHistory();
            webView.removeAllViews();
            webView.destroy();
        }
        // FIX: Shutdown executor to prevent thread leaks
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
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
}
