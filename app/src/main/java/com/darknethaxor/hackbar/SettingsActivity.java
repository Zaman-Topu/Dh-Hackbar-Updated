package com.darknethaxor.hackbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * SettingsActivity
 * Reverted to original UI, using standard Android Switch and SeekBar.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "hackbar_prefs";
    private static final String PREF_SAVE_URL = "save_last_url";
    private static final String PREF_TEXT_SIZE = "text_size";

    private SharedPreferences prefs;

    private ImageView btnBack;
    private Switch switchSaveUrl;
    private SeekBar seekTextSize;
    private TextView tvClearData, tvActivityName, tvSaveLastUrlSetting, tvSaveLastUrlDetails;
    private TextView tvTextSizeSetting, tvTextSizeDetails, tvViewSourceSetting, tvViewSourceMethod;
    private LinearLayout viewSourceLinear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings); // Original layout

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        bindViews();
        loadSettings();
        setupListeners();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.back);
        tvActivityName = findViewById(R.id.activityname);

        switchSaveUrl = findViewById(R.id.savelasturlswitch);
        seekTextSize = findViewById(R.id.textsizeseekbar);
        tvClearData = findViewById(R.id.cleardatasetting);
        
        viewSourceLinear = findViewById(R.id.viewsourcelinear);
        tvViewSourceMethod = findViewById(R.id.viewsourcemethod);
        
        // These will literally say "Fuck" because of the old XML, but binding them anyway
        tvSaveLastUrlSetting = findViewById(R.id.savelasturlsetting);
        tvSaveLastUrlDetails = findViewById(R.id.savelasturldetails);
        tvTextSizeSetting = findViewById(R.id.textsizesetting);
        tvTextSizeDetails = findViewById(R.id.textsizedetails);
        tvViewSourceSetting = findViewById(R.id.viewsourcesetting);
        
        // We can manually set the texts here to fix the "Fuck" issue even though the XML has it
        tvActivityName.setText("Settings");
        tvSaveLastUrlSetting.setText("Save Last URL");
        tvSaveLastUrlDetails.setText("Restore the last visited URL on startup");
        tvTextSizeSetting.setText("Text Size");
        tvTextSizeDetails.setText("Adjust the WebView text size");
        tvViewSourceSetting.setText("View Source Method");
        tvClearData.setText("Clear App Data");
    }

    private void loadSettings() {
        boolean saveUrl = "true".equals(prefs.getString(PREF_SAVE_URL, "false"));
        switchSaveUrl.setChecked(saveUrl);

        int textSize = 1;
        try {
            textSize = Integer.parseInt(prefs.getString(PREF_TEXT_SIZE, "1"));
        } catch (NumberFormatException ignored) {}
        seekTextSize.setProgress(textSize);
        
        tvViewSourceMethod.setText("App"); // Hardcoded default for now
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        switchSaveUrl.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putString(PREF_SAVE_URL, isChecked ? "true" : "false").apply();
        });

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

        tvClearData.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Clear Data")
                .setMessage("Are you sure you want to clear all app data and cache?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    new WebView(this).clearCache(true);
                    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply();
                    getSharedPreferences("custom_queries", Context.MODE_PRIVATE).edit().clear().apply();
                    Toast.makeText(this, "Data Cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
        });
        
        viewSourceLinear.setOnClickListener(v -> {
            Toast.makeText(this, "View source method can't be changed in this version.", Toast.LENGTH_SHORT).show();
        });
    }
}
