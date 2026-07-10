package com.darknethaxor.hackbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * AboutActivity
 * Reverted to original UI. Displays about app and device info.
 */
public class AboutActivity extends AppCompatActivity {

    private ImageView btnBack;
    
    private TextView tvAppName, tvVersion, tvCopyright, tvActivityName;
    private TextView tvMasterJavaName, tvMasterJavaDesig;
    private TextView tvNetHunterName, tvNetHunterDesig;
    private TextView tvFrozenFlameName, tvFrozenFlameDesig;
    private TextView tvMdJubayerName, tvMdJubayerDesig;
    
    private TextView tvAndroidVer, tvDeviceInfo, tvDeviceId;
    private LinearLayout llGithub, llFbGroup, llEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about); // Original layout

        bindViews();
        setupData();
        setupListeners();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.back);
        tvActivityName = findViewById(R.id.activityname);
        
        tvAppName = findViewById(R.id.appname);
        tvVersion = findViewById(R.id.versionname);
        tvCopyright = findViewById(R.id.copyright);
        
        tvMasterJavaName = findViewById(R.id.masterjavaname);
        tvMasterJavaDesig = findViewById(R.id.masterjavadesignation);
        
        tvNetHunterName = findViewById(R.id.nethuntername);
        tvNetHunterDesig = findViewById(R.id.nethunterdesignation);
        
        tvFrozenFlameName = findViewById(R.id.frozenflamename);
        tvFrozenFlameDesig = findViewById(R.id.frozenflamedesignation);
        
        tvMdJubayerName = findViewById(R.id.mdjubayername);
        tvMdJubayerDesig = findViewById(R.id.mdjubayerdesignation);
        
        tvAndroidVer = findViewById(R.id.androidversion);
        tvDeviceInfo = findViewById(R.id.deviceinfo);
        tvDeviceId = findViewById(R.id.deviceid);
        
        llGithub = findViewById(R.id.githubrepositorylinear);
        llFbGroup = findViewById(R.id.joinfbgrouplinear);
        llEmail = findViewById(R.id.emailuslinear);
        
        // Fix labels overwritten by decompilation
        tvActivityName.setText("About HackBar");
        tvAppName.setText("DH-HackBar");
        tvVersion.setText("Version 2.0 (Modernized)");
        tvCopyright.setText("© Darknet Haxor");
        
        tvMasterJavaName.setText("Master Java");
        tvMasterJavaDesig.setText("Original Developer");
        
        tvNetHunterName.setText("Zaman Topu");
        tvNetHunterDesig.setText("Modernizer / Maintainer");
        
        tvFrozenFlameName.setText("Frozen Flame");
        tvFrozenFlameDesig.setText("Contributor");
        
        tvMdJubayerName.setText("Md Jubayer");
        tvMdJubayerDesig.setText("Contributor");
    }

    private void setupData() {
        tvAndroidVer.setText(Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
        tvDeviceInfo.setText(Build.MANUFACTURER + " " + Build.MODEL);
        
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        tvDeviceId.setText(androidId != null ? androidId : "Unknown");
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        llGithub.setOnClickListener(v -> openUrl("https://github.com/Zaman-Topu"));
        llFbGroup.setOnClickListener(v -> openUrl("https://facebook.com/groups/darknethaxor"));
        llEmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:admin@example.com"));
            startActivity(Intent.createChooser(intent, "Send Email"));
        });
    }

    private void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}
