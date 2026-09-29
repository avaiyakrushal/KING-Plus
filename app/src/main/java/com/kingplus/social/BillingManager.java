package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;

/**
 * Billing is intentionally disabled in the current KING Plus test build.
 * This stub keeps older UI call sites build-safe without initializing Google Play Billing
 * or charging real money.
 */
public final class BillingManager {
    private BillingManager() {}

    public static void showRecharge(Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;
        new AlertDialog.Builder(activity)
            .setTitle("Recharge disabled")
            .setMessage("Billing and real-money recharge are disabled in this KING Plus test build.")
            .setPositiveButton("OK", null)
            .show();
    }
}
