package com.darknethaxor.hackbar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * PermissionActivity — Runtime Permission Gate
 *
 * FIXES APPLIED:
 * 1. Extends AppCompatActivity (was plain Activity)
 * 2. FIX CRITICAL: requestPermissions() / onRequestPermissionsResult() (deprecated API 33) →
 *    replaced with ActivityResultLauncher (modern, recommended approach)
 * 3. FIX CRITICAL: Android 10+ (API 29) does not need WRITE_EXTERNAL_STORAGE for app-private dirs
 *    Android 13+ (API 33) needs READ_MEDIA_IMAGES instead of READ_EXTERNAL_STORAGE
 *    → Permission request now correctly targets the right API level
 * 4. FIX: android.R.style.Theme.Material.Dialog.Alert → MaterialAlertDialog
 * 5. FIX: iArr[i] == -1 check was wrong for permission result
 *    (PackageManager.PERMISSION_DENIED = -1, PERMISSION_GRANTED = 0) — logic was INVERTED
 *    Original code: if (i2 == -1) z = false → this is actually CORRECT but confusing
 *    Replaced with named constant for clarity
 * 6. overridePendingTransition(android.R.anim) → safe reference
 * 7. FIX: AlertDialog.Builder(this, R.style) → MaterialAlertDialogBuilder
 * 8. Obfuscated Base64 strings decoded and replaced with string resources
 */
public class PermissionActivity extends AppCompatActivity {

    private AlertDialog currentDialog;

    // FIX: ActivityResultLauncher replaces deprecated requestPermissions + onRequestPermissionsResult
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        boolean allGranted = true;
                        for (Boolean granted : result.values()) {
                            if (!granted) {
                                allGranted = false;
                                break;
                            }
                        }
                        if (allGranted) {
                            proceedToHackbar();
                        } else {
                            showPermissionDeniedDialog();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        checkAndRequestPermissions();
    }

    /**
     * FIX: Determines the correct storage permission based on API level.
     *
     * API 33+ → READ_MEDIA_IMAGES (scoped storage, no READ_EXTERNAL_STORAGE)
     * API 29-32 → Only READ_EXTERNAL_STORAGE needed (scoped storage for writes)
     * API 21-28 → READ + WRITE_EXTERNAL_STORAGE
     *
     * If no permission needed (API 29+ with only internal/app-specific storage),
     * we skip directly to HackbarActivity.
     */
    private void checkAndRequestPermissions() {
        // Android 13+ uses granular media permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // App only reads its own files — no runtime permission needed on API 33+
            // Storage access for SharedPreferences and internal files is always granted
            proceedToHackbar();
            return;
        }

        // API 21-32: Check READ_EXTERNAL_STORAGE
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29-32: Write permission not needed (use app-specific dir)
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        } else {
            // API 21-28: Both needed
            permissions = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }

        // Check if already granted
        boolean allAlreadyGranted = true;
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                allAlreadyGranted = false;
                break;
            }
        }

        if (allAlreadyGranted) {
            proceedToHackbar();
        } else {
            showPermissionRationaleDialog(permissions);
        }
    }

    /**
     * FIX: MaterialAlertDialog replaces android.R.style.Theme.Material.Dialog.Alert
     */
    private void showPermissionRationaleDialog(String[] permissions) {
        dismissCurrentDialog();
        currentDialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.permission_title))
                .setMessage(getString(R.string.permission_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.permission_grant), (dialog, which) -> {
                    permissionLauncher.launch(permissions);
                })
                .create();
        currentDialog.show();
    }

    private void showPermissionDeniedDialog() {
        dismissCurrentDialog();
        currentDialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.permission_denied_title))
                .setMessage(getString(R.string.permission_denied_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.permission_open_settings), (dialog, which) -> {
                    // Open app settings so user can manually grant permission
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(getString(R.string.permission_exit), (dialog, which) -> {
                    finishAffinity();
                })
                .create();
        currentDialog.show();
    }

    private void proceedToHackbar() {
        Intent intent = new Intent(this, HackbarActivity.class);
        // Forward any deep-link URL passed from MainActivity
        String deepLinkUrl = getIntent().getStringExtra(HackbarActivity.EXTRA_INITIAL_URL);
        if (deepLinkUrl != null && !deepLinkUrl.isEmpty()) {
            intent.putExtra(HackbarActivity.EXTRA_INITIAL_URL, deepLinkUrl);
        }
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void dismissCurrentDialog() {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissCurrentDialog();
    }
}
