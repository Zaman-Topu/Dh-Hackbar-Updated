package com.darknethaxor.hackbar;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * DebugActivity — Shows last crash log
 *
 * FIXES:
 * 1. extends AppCompatActivity (was plain Activity — or declared twice in manifest)
 * 2. Single declaration in manifest (was duplicate → build error)
 * 3. Reads crash log from SharedPreferences via HackBarApplication.getStackTrace()
 */
public class DebugActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        ImageView btnBack = findViewById(R.id.back);
        TextView tvLog = findViewById(R.id.debug_log);
        TextView tvClear = findViewById(R.id.debug_clear);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // Load last crash from prefs
        String crashLog = getSharedPreferences("hackbar_prefs", Context.MODE_PRIVATE)
                .getString("last_crash", "No crash logs found.");
        tvLog.setText(crashLog);

        tvClear.setOnClickListener(v -> {
            getSharedPreferences("hackbar_prefs", Context.MODE_PRIVATE)
                    .edit().remove("last_crash").apply();
            tvLog.setText("No crash logs found.");
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
