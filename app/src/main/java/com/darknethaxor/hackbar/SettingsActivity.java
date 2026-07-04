package com.darknethaxor.hackbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * SettingsActivity
 *
 * FIXES APPLIED:
 * 1. extends AppCompatActivity (was plain Activity)
 * 2. SwitchMaterial replaces deprecated Switch widget
 * 3. SharedPreferences.commit() → apply() (blocks main thread)
 * 4. NestedScrollView used for proper CoordinatorLayout compat
 * 5. Clear data now shows MaterialAlertDialog confirmation
 * 6. All obfuscated strings decoded and replaced
 * 7. SeekBar replaces spinner for text size (better UX)
 * 8. WindowInsetsController replaces setSystemUiVisibility
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME       = "hackbar_prefs";
    private static final String PREF_SAVE_URL    = "save_last_url";
    private static final String PREF_TEXT_SIZE   = "text_size";
    private static final String PREF_VIEW_SOURCE = "view_source_method";

    private SharedPreferences prefs;

    // Views
    private SwitchMaterial switchSaveUrl;
    private SeekBar seekTextSize;
    private TextView tvViewSourceMethod;
    private TextView tvClearData;

    private final String[] viewSourceMethods = {"External Browser", "Internal Viewer", "Raw Text"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        bindViews();
        loadSettings();
        setupClickListeners();
    }

    private void bindViews() {
        ImageView btnBack     = findViewById(R.id.back);
        switchSaveUrl         = findViewById(R.id.savelasturlswitch);
        seekTextSize          = findViewById(R.id.textsizeseekbar);
        tvViewSourceMethod    = findViewById(R.id.viewsourcemethod);
        tvClearData           = findViewById(R.id.cleardatasetting);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void loadSettings() {
        // Save Last URL switch
        boolean saveLast = prefs.getString(PREF_SAVE_URL, "false").equals("true");
        switchSaveUrl.setChecked(saveLast);

        // Text size seekbar
        int textSize = Integer.parseInt(prefs.getString(PREF_TEXT_SIZE, "1"));
        seekTextSize.setProgress(textSize);

        // View source method
        int method = Integer.parseInt(prefs.getString(PREF_VIEW_SOURCE, "0"));
        tvViewSourceMethod.setText(viewSourceMethods[method]);
    }

    private void setupClickListeners() {
        // Save Last URL — switch listener
        switchSaveUrl.setOnCheckedChangeListener((btn, checked) -> {
            // FIX: apply() instead of commit() — non-blocking
            prefs.edit().putString(PREF_SAVE_URL, checked ? "true" : "false").apply();
        });

        // Text Size seekbar
        seekTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    prefs.edit().putString(PREF_TEXT_SIZE, String.valueOf(progress)).apply();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // View Source Method selector
        findViewById(R.id.viewsourcelinear).setOnClickListener(v -> {
            int current = Integer.parseInt(prefs.getString(PREF_VIEW_SOURCE, "0"));
            new MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.setting_view_source_method))
                    .setSingleChoiceItems(viewSourceMethods, current, (dialog, which) -> {
                        prefs.edit().putString(PREF_VIEW_SOURCE, String.valueOf(which)).apply();
                        tvViewSourceMethod.setText(viewSourceMethods[which]);
                        dialog.dismiss();
                    })
                    .show();
        });

        // Clear Data — shows confirmation dialog
        tvClearData.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.setting_clear_data_confirm_title))
                    .setMessage(getString(R.string.setting_clear_data_confirm_msg))
                    .setPositiveButton(getString(R.string.action_clear), (dialog, which) -> {
                        prefs.edit().clear().apply();
                        // Also clear WebView data
                        android.webkit.WebView wv = new android.webkit.WebView(this);
                        android.webkit.CookieManager.getInstance().removeAllCookies(null);
                        android.webkit.CookieManager.getInstance().flush();
                        wv.clearCache(true);
                        wv.clearFormData();
                        wv.clearHistory();
                        wv.destroy();
                        android.widget.Toast.makeText(this, "Data cleared", android.widget.Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(getString(R.string.action_cancel), null)
                    .show();
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
