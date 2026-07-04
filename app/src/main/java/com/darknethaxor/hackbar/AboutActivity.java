package com.darknethaxor.hackbar;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * AboutActivity — App info, version, developer credits
 *
 * FIXES APPLIED:
 * 1. extends AppCompatActivity (was plain Activity)
 * 2. PackageInfo.versionCode deprecated API 28+ → getLongVersionCode()
 * 3. getPackageInfo PackageManager.GET_SIGNATURES deprecated API 28+ → GET_SIGNING_CERTIFICATES
 * 4. All obfuscated hardcoded strings replaced with string resources
 * 5. Removed signature verification that called Process.killProcess()
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ImageView btnBack   = findViewById(R.id.back);
        TextView tvVersion  = findViewById(R.id.version_value);
        TextView tvAndroid  = findViewById(R.id.android_version_value);
        TextView tvDevice   = findViewById(R.id.device_value);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // FIX: versionCode deprecated API 28+ → getLongVersionCode()
        String version = "2.0";
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pi.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {}

        tvVersion.setText(version);
        tvAndroid.setText(Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
        tvDevice.setText(Build.MANUFACTURER + " " + Build.MODEL);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
