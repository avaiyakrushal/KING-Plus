package com.kingplus.social;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.ApplicationExitInfo;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import java.util.List;

/**
 * Local diagnostics for process exits (including native crash / LMK / ANR).
 * Records only Activity class/stage, timestamp and heap usage. No personal data is uploaded.
 */
public final class KingCrashWatch958 {
    private static final String PREF = "king_crash_watch_958";
    private static volatile boolean installed;
    private static String previousStage = "";
    private static long previousStageAt;
    private static long previousHeapUsedMb;

    private KingCrashWatch958() {}

    public static synchronized void install(Application application) {
        if (installed || application == null) return;
        installed = true;
        try {
            SharedPreferences p = application.getSharedPreferences(PREF, Context.MODE_PRIVATE);
            previousStage = p.getString("stage", "");
            previousStageAt = p.getLong("stageAt", 0);
            previousHeapUsedMb = p.getLong("javaHeapUsedMb", 0);
        } catch (Throwable ignored) {}

        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity a, Bundle b) { mark(a, "created"); }
            @Override public void onActivityStarted(Activity a) { mark(a, "started"); }
            @Override public void onActivityResumed(Activity a) { mark(a, "resumed"); }
            @Override public void onActivityPaused(Activity a) { mark(a, "paused"); }
            @Override public void onActivityStopped(Activity a) { mark(a, "stopped"); }
            @Override public void onActivitySaveInstanceState(Activity a, Bundle b) {}
            @Override public void onActivityDestroyed(Activity a) { mark(a, "destroyed"); }
        });

        application.registerComponentCallbacks(new ComponentCallbacks2() {
            @Override public void onConfigurationChanged(Configuration c) {}
            @Override public void onLowMemory() { recordTrim(application, 1000); }
            @Override public void onTrimMemory(int level) { recordTrim(application, level); }
        });
    }

    public static void mark(Activity a, String stage) {
        if (a == null) return;
        try {
            String safeStage = stage == null ? "" : stage.replaceAll("[^A-Za-z0-9_./-]", "_");
            if (safeStage.length() > 60) safeStage = safeStage.substring(0, 60);
            String label = a.getClass().getSimpleName() + "/" + safeStage;
            Runtime r = Runtime.getRuntime();
            long usedMb = (r.totalMemory() - r.freeMemory()) / (1024 * 1024);
            a.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
                .putString("stage", label)
                .putLong("stageAt", System.currentTimeMillis())
                .putLong("javaHeapUsedMb", usedMb)
                .commit();
        } catch (Throwable ignored) {}
    }

    private static void recordTrim(Context c, int level) {
        try {
            c.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
                .putInt("trimLevel", level)
                .putLong("trimAt", System.currentTimeMillis())
                .commit();
        } catch (Throwable ignored) {}
    }

    public static void showPreviousExit(Activity a) {
        if (a == null || a.isFinishing() || a.isDestroyed() || Build.VERSION.SDK_INT < 30) return;
        try {
            ActivityManager am = (ActivityManager) a.getSystemService(Context.ACTIVITY_SERVICE);
            if (am == null) return;
            List<ApplicationExitInfo> exits = am.getHistoricalProcessExitReasons(a.getPackageName(), 0, 10);
            if (exits == null || exits.isEmpty()) return;
            SharedPreferences p = a.getSharedPreferences(PREF, Context.MODE_PRIVATE);
            long alreadySeen = p.getLong("shownExitAt", 0);
            long now = System.currentTimeMillis();
            ApplicationExitInfo relevant = null;
            for (ApplicationExitInfo item : exits) {
                if (!a.getPackageName().equals(item.getProcessName())) continue;
                if (item.getTimestamp() <= alreadySeen || item.getTimestamp() > now + 60000L) continue;
                if (item.getTimestamp() < now - 7L * 24 * 3600 * 1000) continue;
                if (relevant == null || item.getTimestamp() > relevant.getTimestamp()) relevant = item;
            }
            if (relevant == null) return;
            p.edit().putLong("shownExitAt", relevant.getTimestamp()).apply();

            final int reason = relevant.getReason();
            if (!isAbnormal(reason)) return;

            String stage = (previousStageAt > 0 && Math.abs(previousStageAt - relevant.getTimestamp()) < 15 * 60 * 1000L)
                ? previousStage : "(not recorded)";
            String report = "Android process exit: " + label(reason)
                + "\nLast screen: " + stage
                + "\nExit time (ms): " + relevant.getTimestamp()
                + "\nJava heap at last stage: " + previousHeapUsedMb + " MB"
                + "\nLast sampled PSS: " + relevant.getPss() + " KB"
                + "\nLast sampled RSS: " + relevant.getRss() + " KB"
                + "\nBuild: 9.5.8";
            String desc = relevant.getDescription();
            if (desc != null && !desc.trim().isEmpty()) {
                report += "\nOS detail: " + desc.substring(0, Math.min(220, desc.length()));
            }
            final String copy = report;
            new AlertDialog.Builder(a)
                .setTitle("KING Plus – Android crash diagnostics")
                .setMessage(report + "\n\nThis report is stored on your phone; it is not sent automatically.")
                .setPositiveButton("Copy details", (d, w) -> {
                    try {
                        ClipboardManager cb = (ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE);
                        if (cb != null) cb.setPrimaryClip(ClipData.newPlainText("KING Plus diagnostics", copy));
                    } catch (Throwable ignored) {}
                })
                .setNegativeButton("Close", null)
                .show();
        } catch (Throwable e) {
            try { android.util.Log.w("KINGPlusCrashWatch", "Could not read Android exit reason", e); }
            catch (Throwable ignored) {}
        }
    }

    private static boolean isAbnormal(int reason) {
        // Java crashes are already handled by the existing previous-crash dialog.
        return reason == ApplicationExitInfo.REASON_CRASH_NATIVE
            || reason == ApplicationExitInfo.REASON_ANR
            || reason == ApplicationExitInfo.REASON_LOW_MEMORY
            || reason == ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE
            || reason == ApplicationExitInfo.REASON_INITIALIZATION_FAILURE
            || reason == ApplicationExitInfo.REASON_SIGNALED;
    }

    private static String label(int reason) {
        switch (reason) {
            case ApplicationExitInfo.REASON_CRASH: return "Java crash";
            case ApplicationExitInfo.REASON_CRASH_NATIVE: return "Native crash (Voice/Video or library)";
            case ApplicationExitInfo.REASON_ANR: return "App not responding";
            case ApplicationExitInfo.REASON_LOW_MEMORY: return "Low memory (Android terminated process)";
            case ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE: return "Excessive resource usage";
            case ApplicationExitInfo.REASON_INITIALIZATION_FAILURE: return "Initialization failure";
            case ApplicationExitInfo.REASON_SIGNALED: return "Terminated by OS signal";
            default: return "Reason code " + reason;
        }
    }
}
