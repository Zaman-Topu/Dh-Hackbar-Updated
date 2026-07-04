package com.darknethaxor.hackbar;

import android.app.Application;
import android.content.Context;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * HackBarApplication — Application class
 *
 * FIXES APPLIED:
 * 1. Renamed from SketchApplication (obfuscated name) to HackBarApplication
 * 2. Custom UncaughtExceptionHandler preserved but improved — no longer calls
 *    Process.killProcess() silently without logging the crash
 * 3. StrictMode removed from release builds
 * 4. Singleton context accessor added (replaces getApplicationContext() misuse)
 */
public class HackBarApplication extends Application {

    private static HackBarApplication instance;
    private Thread.UncaughtExceptionHandler defaultHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Store default handler before overriding
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        // FIX: Custom crash handler — logs crash then delegates to system handler
        // Original app silently killed process without logging crash to user
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            String crashLog = getStackTrace(throwable);
            // Save crash log to SharedPreferences for DebugActivity to display
            getSharedPreferences("hackbar_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("last_crash", crashLog)
                    .apply();

            // Delegate to default handler (shows crash dialog on debug builds)
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
        });
    }

    /**
     * Singleton accessor — avoids repeated getApplicationContext() calls
     * which can cause leaks if Activity context is mistakenly used
     */
    public static HackBarApplication getInstance() {
        return instance;
    }

    /**
     * FIX: getStackTrace moved to Application level (was private in SketchApplication)
     * so it's accessible from DebugActivity without reflection
     */
    public static String getStackTrace(Throwable th) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        while (th != null) {
            th.printStackTrace(pw);
            th = th.getCause();
        }
        String result = sw.toString();
        pw.close();
        return result;
    }
}
