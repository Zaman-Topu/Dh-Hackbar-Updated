package com.darknethaxor.hackbar;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AdminActivity — Admin Panel Finder + Scanner
 *
 * FIXES APPLIED:
 * 1. extends AppCompatActivity (was plain Activity)
 * 2. AsyncTask REMOVED → ExecutorService + Handler (modern threading)
 * 3. HttpURLConnection used instead of deprecated Apache HttpClient
 * 4. ListView → RecyclerView
 * 5. ProgressDialog → LinearProgressIndicator in layout
 * 6. All obfuscated strings decoded
 * 7. Proper cancellation on back press (executor.shutdownNow())
 * 8. apply() replaces commit()
 */
public class AdminActivity extends AppCompatActivity {

    private static final String[] ADMIN_PATHS = {
        "/admin/", "/admin.php", "/admin.html", "/administrator/",
        "/administrator/index.php", "/admin/login.php", "/adminpanel/",
        "/wp-admin/", "/wp-login.php", "/admin/admin.php",
        "/backend/", "/manage/", "/management/", "/panel/",
        "/controlpanel/", "/cp/", "/cpanel/", "/phpmyadmin/",
        "/dbadmin/", "/pma/", "/myadmin/", "/mysql/", "/sqlitemanager/",
        "/admin1/", "/admin2/", "/admin3/", "/admin4/", "/admin5/",
        "/adminarea/", "/bb-admin/", "/adminLogin/", "/admin_area/",
        "/panel-administracion/", "/instadmin/", "/moderator/",
        "/webadmin/", "/admincp/", "/cms/", "/cms/login/",
        "/adm/", "/account/login/", "/user/login/", "/login/",
        "/members/login/", "/auth/", "/webmaster/", "/dashboard/",
    };

    private EditText urlField;
    private LinearProgressIndicator progressBar;
    private RecyclerView recyclerView;
    private TextView tvEmpty, tvStatus;
    private ImageView btnBack;

    private final ArrayList<String> results = new ArrayList<>();
    private AdminResultAdapter adapter;
    private ExecutorService executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean scanning = false;
    private int totalChecked = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        bindViews();

        String initialUrl = getIntent().getStringExtra(HackbarActivity.EXTRA_INITIAL_URL);
        if (initialUrl != null && !initialUrl.isEmpty()) {
            urlField.setText(initialUrl);
        }
    }

    private void bindViews() {
        btnBack     = findViewById(R.id.back);
        urlField    = findViewById(R.id.urlfield);
        progressBar = findViewById(R.id.progressbar);
        recyclerView = findViewById(R.id.listview);
        tvEmpty     = findViewById(R.id.hinttext);

        btnBack.setOnClickListener(v -> onBackPressed());

        // FIX: RecyclerView + Adapter replaces ListView + ArrayAdapter
        adapter = new AdminResultAdapter(results, url -> openUrl(url));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.find).setOnClickListener(v -> startScan());
    }

    private void startScan() {
        String rawUrl = urlField.getText().toString().trim();
        if (rawUrl.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_invalid_url), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
            rawUrl = "http://" + rawUrl;
            urlField.setText(rawUrl);
        }

        // Extract base URL (remove path)
        String baseUrl;
        try {
            URL url = new URL(rawUrl);
            baseUrl = url.getProtocol() + "://" + url.getHost();
        } catch (Exception e) {
            Toast.makeText(this, "Invalid URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        results.clear();
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(false);
        progressBar.setMax(ADMIN_PATHS.length);
        totalChecked = 0;
        scanning = true;

        // FIX: ExecutorService replaces deprecated AsyncTask
        executor = Executors.newFixedThreadPool(5); // 5 concurrent checks
        final String finalBaseUrl = baseUrl;

        for (String path : ADMIN_PATHS) {
            final String checkUrl = finalBaseUrl + path;
            executor.submit(() -> {
                if (!scanning) return;
                boolean found = checkUrl(checkUrl);
                mainHandler.post(() -> {
                    totalChecked++;
                    progressBar.setProgress(totalChecked);
                    if (found) {
                        results.add(checkUrl);
                        adapter.notifyItemInserted(results.size() - 1);
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                    if (totalChecked >= ADMIN_PATHS.length) {
                        scanFinished();
                    }
                });
            });
        }
    }

    /**
     * FIX: HttpURLConnection replaces deprecated Apache HttpClient
     * Returns true if the URL returns HTTP 200 or 301/302 (found)
     */
    private boolean checkUrl(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (compatible; DH-HackBar/2.0)");
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            return responseCode == 200 || responseCode == 301 || responseCode == 302;
        } catch (Exception e) {
            return false;
        }
    }

    private void scanFinished() {
        scanning = false;
        progressBar.setVisibility(View.GONE);
        if (results.isEmpty()) {
            tvEmpty.setText(getString(R.string.admin_no_results));
            tvEmpty.setVisibility(View.VISIBLE);
        }
        Toast.makeText(this, "Scan complete: " + results.size() + " panel(s) found", Toast.LENGTH_LONG).show();
    }

    private void openUrl(String url) {
        Intent intent = new Intent(this, WebtoolActivity.class);
        intent.putExtra(WebtoolActivity.EXTRA_URL, url);
        intent.putExtra(WebtoolActivity.EXTRA_TITLE, "Admin Panel");
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        scanning = false;
        if (executor != null) executor.shutdownNow(); // FIX: cancel background tasks
        mainHandler.removeCallbacksAndMessages(null);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanning = false;
        if (executor != null) executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }
}
