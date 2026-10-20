package com.kingplus.social;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

public class KingApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        KingCrashWatch958.install(this);
        final Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            try {
                StringWriter sw = new StringWriter();
                error.printStackTrace(new PrintWriter(sw));
                String trace = sw.toString();
                if (trace.length() > 12000) trace = trace.substring(0, 12000);
                SharedPreferences p = getSharedPreferences("king_stability", MODE_PRIVATE);
                p.edit()
                    .putLong("lastCrashAt", System.currentTimeMillis())
                    .putString("lastCrashThread", thread == null ? "unknown" : thread.getName())
                    .putString("lastCrash", trace)
                    .putString("device", Build.MANUFACTURER + " " + Build.MODEL + " / Android " + Build.VERSION.RELEASE)
                    .commit();
                Log.e("KINGPlus", "Uncaught crash captured", error);
            } catch (Throwable ignored) {}
            if (previous != null) previous.uncaughtException(thread, error);
            else System.exit(10);
        });
    }
}
