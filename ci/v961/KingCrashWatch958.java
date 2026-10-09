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
import java.util.List;

/** v9.6.1 local process exit attribution; no network uploads. */
public final class KingCrashWatch958 {
    private static final String PREF = "king_crash_watch_958";
    private static volatile boolean installed;
    private static String previousStage = "";
    private static long previousStageAt;
    private static long previousHeapUsedMb;
    private static int previousPid;
    private KingCrashWatch958() {}

    public static synchronized void install(Application application) {
        if (installed || application == null) return;
        installed = true;
        try {
            SharedPreferences p = application.getSharedPreferences(PREF, Context.MODE_PRIVATE);
            previousStage = p.getString("stage", "");
            previousStageAt = p.getLong("stageAt", 0);
            previousHeapUsedMb = p.getLong("javaHeapUsedMb", 0);
            previousPid = p.getInt("pid", 0);
        } catch (Throwable ignored) {}

        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity a, Bundle b) { mark(a, "created"); }
            @Override public void onActivityStarted(Activity a) {}
            @Override public void onActivityResumed(Activity a) { mark(a, "resumed"); }
            @Override public void onActivityPaused(Activity a) {}
            @Override public void onActivityStopped(Activity a) {}
            @Override public void onActivitySaveInstanceState(Activity a, Bundle b) {}
            @Override public void onActivityDestroyed(Activity a) {}
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
            Runtime r = Runtime.getRuntime();
            long usedMb = (r.totalMemory() - r.freeMemory()) / (1024 * 1024);
            a.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
                .putString("stage", a.getClass().getSimpleName() + "/" + safeStage)
                .putLong("stageAt", System.currentTimeMillis())
                .putLong("javaHeapUsedMb", usedMb)
                .putInt("pid", android.os.Process.myPid())
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
            android.content.pm.PackageInfo pkg = a.getPackageManager()
                .getPackageInfo(a.getPackageName(), 0);
            final long installedAt = pkg.lastUpdateTime;
            final String installedVersion = pkg.versionName == null ? "unknown" : pkg.versionName;
            ActivityManager am = (ActivityManager) a.getSystemService(Context.ACTIVITY_SERVICE);
            if (am == null) return;
            List<ApplicationExitInfo> exits = am.getHistoricalProcessExitReasons(a.getPackageName(), 0, 20);
            if (exits == null || exits.isEmpty()) return;
            SharedPreferences p = a.getSharedPreferences(PREF, Context.MODE_PRIVATE);
            long alreadySeen = p.getLong("shownExitAt", 0);
            long now = System.currentTimeMillis();
            ApplicationExitInfo relevant = null;
            for (ApplicationExitInfo item : exits) {
                if (!a.getPackageName().equals(item.getProcessName())) continue;
                if (!isAbnormal(item.getReason())) continue;
                if (item.getTimestamp() <= alreadySeen || item.getTimestamp() > now + 60000L) continue;
                // Never present a crash from a previous APK version.
                long oldest = Math.max(installedAt, now - 2L * 24 * 3600 * 1000);
                if (item.getTimestamp() < oldest) continue;
                if (relevant == null || item.getTimestamp() > relevant.getTimestamp())
                    relevant = item;
            }
            if (relevant == null) return;
            p.edit().putLong("shownExitAt", relevant.getTimestamp()).apply();

            boolean sameProcess = previousPid > 0 && previousPid == relevant.getPid()
                && previousStageAt > 0
                && previousStageAt <= relevant.getTimestamp()
                && relevant.getTimestamp() - previousStageAt <= 15L * 60 * 1000;
            String lastStage = sameProcess ? previousStage : "(not recorded for this process)";
            String lastHeap = sameProcess ? previousHeapUsedMb + " MB" : "(not recorded)";
            java.text.DateFormat formatter = java.text.DateFormat.getDateTimeInstance();
            String report = "Android exit: " + label(relevant.getReason())
                + "\nInstalled KING Plus version: " + installedVersion
                + "\nCrash time: " + formatter.format(new java.util.Date(relevant.getTimestamp()))
                + "\nCrash timestamp (ms): " + relevant.getTimestamp()
                + "\nProcess PID: " + relevant.getPid()
                + "\nLast screen: " + lastStage
                + "\nJava heap on last screen: " + lastHeap
                + "\nPSS: " + relevant.getPss() + " KB"
                + "\nRSS: " + relevant.getRss() + " KB"
                + "\nAPK installed: " + formatter.format(new java.util.Date(installedAt))
                + "\nA native crash does not identify the responsible library on its own.";
            String desc = relevant.getDescription();
            if (desc != null && !desc.trim().isEmpty())
                report += "\nOS detail: " + desc.substring(0, Math.min(220, desc.length()));
            final String toCopy = report;
            new AlertDialog.Builder(a)
                .setTitle("KING Plus – new crash report")
                .setMessage(report + "\n\nStored locally; not sent automatically.")
                .setPositiveButton("Copy details", (d,w) -> {
                    try {
                        ClipboardManager cb = (ClipboardManager)
                            a.getSystemService(Context.CLIPBOARD_SERVICE);
                        if (cb != null) cb.setPrimaryClip(
                            ClipData.newPlainText("KING Plus crash report", toCopy));
                    } catch (Throwable ignored) {}
                })
                .setNegativeButton("Close", null)
                .show();
        } catch (Throwable e) {
            try { android.util.Log.w("KINGPlusCrashWatch", "Process exit report unavailable", e); }
            catch (Throwable ignored) {}
        }
    }

    private static boolean isAbnormal(int reason) {
        return reason == ApplicationExitInfo.REASON_CRASH_NATIVE
            || reason == ApplicationExitInfo.REASON_ANR
            || reason == ApplicationExitInfo.REASON_LOW_MEMORY
            || reason == ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE
            || reason == ApplicationExitInfo.REASON_INITIALIZATION_FAILURE
            || reason == ApplicationExitInfo.REASON_SIGNALED;
    }

    private static String label(int reason) {
        switch(reason) {
            case ApplicationExitInfo.REASON_CRASH_NATIVE: return "Native crash (library unknown)";
            case ApplicationExitInfo.REASON_ANR: return "App not responding (ANR)";
            case ApplicationExitInfo.REASON_LOW_MEMORY: return "Low memory kill";
            case ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE: return "Excessive resource usage";
            case ApplicationExitInfo.REASON_INITIALIZATION_FAILURE: return "Initialization failure";
            case ApplicationExitInfo.REASON_SIGNALED: return "Killed by signal";
            default: return "Reason " + reason;
        }
    }
}
