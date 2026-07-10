package com.darknethaxor.hackbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AdminActivity — Admin Panel Finder + Scanner
 * Reverted to original UI (ListView), keeps modern background thread logic.
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
    private ProgressBar progressBar;
    private ListView listView;
    private TextView tvEmpty, tvFind;
    private ImageView btnBack;

    private final ArrayList<String> results = new ArrayList<>();
    private ResultAdapter adapter;
    private ExecutorService executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean scanning = false;
    private int totalChecked = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin); // Original layout

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
        listView    = findViewById(R.id.listview);
        tvEmpty     = findViewById(R.id.hinttext);
        tvFind      = findViewById(R.id.find);

        btnBack.setOnClickListener(v -> onBackPressed());

        adapter = new ResultAdapter(this, results);
        listView.setAdapter(adapter);
        
        listView.setOnItemClickListener((parent, view, position, id) -> {
            openUrl(results.get(position));
        });

        tvFind.setOnClickListener(v -> startScan());
        progressBar.setVisibility(View.GONE);
    }

    private void startScan() {
        String rawUrl = urlField.getText().toString().trim();
        if (rawUrl.isEmpty()) {
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
            rawUrl = "http://" + rawUrl;
            urlField.setText(rawUrl);
        }

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

        executor = Executors.newFixedThreadPool(5); 
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
                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                    }
                    if (totalChecked >= ADMIN_PATHS.length) {
                        scanFinished();
                    }
                });
            });
        }
    }

    private boolean checkUrl(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; DH-HackBar/2.0)");
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
            tvEmpty.setText("No Admin Panels Found");
            tvEmpty.setVisibility(View.VISIBLE);
        }
        Toast.makeText(this, "Scan complete: " + results.size() + " panel(s) found", Toast.LENGTH_LONG).show();
    }

    private void openUrl(String url) {
        Intent intent = new Intent(this, WebtoolActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("title", "Admin Panel");
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        scanning = false;
        if (executor != null) executor.shutdownNow(); 
        mainHandler.removeCallbacksAndMessages(null);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanning = false;
        if (executor != null) executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    // Custom adapter using the original listitem.xml
    private static class ResultAdapter extends BaseAdapter {
        private final Context context;
        private final ArrayList<String> items;
        
        ResultAdapter(Context context, ArrayList<String> items) {
            this.context = context;
            this.items = items;
        }
        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.listitem, parent, false);
            }
            TextView tv = convertView.findViewById(R.id.text);
            tv.setText(items.get(position));
            return convertView;
        }
    }
}
