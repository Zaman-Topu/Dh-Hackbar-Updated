package com.darknethaxor.hackbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity — Splash Screen
 *
 * FIXES APPLIED:
 * 1. Extends AppCompatActivity (was plain Activity — crashes on API 33 with new theme system)
 * 2. Timer / TimerTask replaced with Handler.postDelayed() — TimerTask crashes if
 *    view references are used after Activity is destroyed (common NPE crash source)
 * 3. Removed direct new HackbarActivity().a(context) call — calling methods on
 *    Activity instance created with 'new' (not system-created) ALWAYS crashes
 * 4. Anti-tamper / APK signature check code REMOVED — it uses Process.killProcess()
 *    and hardcoded signatures which cause immediate crash on any recompile/debug build
 * 5. onStop() → finish() replaced with proper lifecycle (was causing double-finish crash)
 * 6. windowSoftInputMode handled via theme instead of code
 * 7. setSystemUiVisibility() deprecated on API 30+ → replaced with WindowInsetsController
 * 8. Intent extras validated before use (null-safety)
 */
public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 1800;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean launched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // FIX: Edge-to-edge display (replaces deprecated setSystemUiVisibility)
        applyEdgeToEdge();

        // Handle deep link URL (when app is opened from a browser link)
        String deepLinkUrl = extractDeepLinkUrl();

        // FIX: Use Handler instead of Timer/TimerTask
        // Timer runs on non-main thread → any UI call from it crashes
        handler.postDelayed(() -> {
            if (!isFinishing() && !isDestroyed()) {
                navigateToNext(deepLinkUrl);
            }
        }, SPLASH_DELAY_MS);
    }

    /**
     * FIX: Replaced deprecated setSystemUiVisibility(3846)
     * with WindowInsetsController (API 30+) / WindowManager flags (API 21+)
     */
    @SuppressWarnings("deprecation")
    private void applyEdgeToEdge() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // API 30+ — use WindowInsetsController
            getWindow().setDecorFitsSystemWindows(false);
        } else {
            // API 21-29 — use legacy flags
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        // Display cutout support (replaces layoutInDisplayCutoutMode = 1 direct set)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(lp);
        }
    }

    /**
     * FIX: Safe URL extraction from Intent — original code didn't null-check Intent data
     */
    private String extractDeepLinkUrl() {
        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            Uri data = intent.getData();
            return data.toString();
        }
        return "";
    }

    /**
     * Navigate to PermissionActivity (which checks & requests storage permission,
     * then forwards to HackbarActivity)
     */
    private void navigateToNext(String deepLinkUrl) {
        if (launched) return;
        launched = true;

        Intent intent = new Intent(this, PermissionActivity.class);
        if (!deepLinkUrl.isEmpty()) {
            intent.putExtra(HackbarActivity.EXTRA_INITIAL_URL, deepLinkUrl);
        }
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // FIX: Cancel pending handler callbacks to prevent memory leaks
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onBackPressed() {
        // Disable back press on splash screen — matches original behavior
        // but done safely without finishing prematurely
    }
}
