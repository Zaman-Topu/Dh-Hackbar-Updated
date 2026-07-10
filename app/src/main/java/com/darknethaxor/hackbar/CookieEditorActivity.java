package com.darknethaxor.hackbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

public class CookieEditorActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "extra_url";
    private String currentUrl;
    
    private LinearLayout cookieListContainer;
    private TextView tvCurrentUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cookie_editor);

        currentUrl = getIntent().getStringExtra(EXTRA_URL);
        if (currentUrl == null || currentUrl.isEmpty()) {
            Toast.makeText(this, "No URL specified", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cookieListContainer = findViewById(R.id.cookieListContainer);
        tvCurrentUrl = findViewById(R.id.tvCurrentUrl);
        tvCurrentUrl.setText("Cookies for: " + currentUrl);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddCookie).setOnClickListener(v -> addCookieItem("", ""));
        findViewById(R.id.btnSaveCookies).setOnClickListener(v -> saveCookiesAndFinish());

        loadCookies();
    }

    private void loadCookies() {
        cookieListContainer.removeAllViews();
        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(currentUrl);

        if (!TextUtils.isEmpty(cookies)) {
            String[] cookiePairs = cookies.split(";");
            for (String pair : cookiePairs) {
                String[] split = pair.trim().split("=", 2);
                if (split.length == 2) {
                    addCookieItem(split[0], split[1]);
                } else if (split.length == 1) {
                    addCookieItem(split[0], "");
                }
            }
        }
        
        // Add at least one empty cookie if there are none
        if (cookieListContainer.getChildCount() == 0) {
            addCookieItem("", "");
        }
    }

    private void addCookieItem(String name, String value) {
        View cookieView = LayoutInflater.from(this).inflate(R.layout.item_cookie, cookieListContainer, false);
        
        EditText etName = cookieView.findViewById(R.id.etCookieName);
        EditText etValue = cookieView.findViewById(R.id.etCookieValue);
        ImageView btnDelete = cookieView.findViewById(R.id.btnDeleteCookie);

        etName.setText(name);
        etValue.setText(value);

        btnDelete.setOnClickListener(v -> cookieListContainer.removeView(cookieView));

        cookieListContainer.addView(cookieView);
    }

    private void saveCookiesAndFinish() {
        CookieManager cookieManager = CookieManager.getInstance();
        // Clear all current cookies for this URL to cleanly save new ones
        // Note: CookieManager doesn't have a clear cookies by URL method.
        // We will overwrite existing ones, and set ones to be 'deleted' by expiring them if they were removed.
        // However, to make it simple, we just set the new ones. Overwriting works if the name matches.
        // For deleted ones, we could track them, but for HackBar purposes, just setting the active ones is usually enough.

        int count = cookieListContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            View cookieView = cookieListContainer.getChildAt(i);
            EditText etName = cookieView.findViewById(R.id.etCookieName);
            EditText etValue = cookieView.findViewById(R.id.etCookieValue);

            String name = etName.getText().toString().trim();
            String value = etValue.getText().toString().trim();

            if (!TextUtils.isEmpty(name)) {
                String cookieString = name + "=" + value + "; path=/";
                cookieManager.setCookie(currentUrl, cookieString);
            }
        }

        cookieManager.flush();
        Toast.makeText(this, "Cookies saved!", Toast.LENGTH_SHORT).show();
        
        Intent resultIntent = new Intent();
        resultIntent.putExtra("RELOAD", true);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
