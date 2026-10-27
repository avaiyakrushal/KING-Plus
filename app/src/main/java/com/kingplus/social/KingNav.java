package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;

/** Consistent root-tab navigation and exit behavior for KING Plus. */
public final class KingNav {
    private KingNav() {}

    public static void openRoot(Activity activity, int tab) {
        if (activity == null) return;
        Intent i;
        if (tab == 0) {
            // Home and Party are separate destinations. The Home hub provides
            // the Bolo Hi-style discovery surface; its Live Party Rooms action
            // opens the existing real Firebase-backed Party screen.
            if (activity instanceof KingParityHubActivity) return;
            i = new Intent(activity, KingParityHubActivity.class);
            i.putExtra("route", "home");
        } else if (tab == 1) {
            // Party is a distinct root destination; do not route it to the legacy Games tab.
            if (activity instanceof PartyActivity) return;
            i = new Intent(activity, PartyActivity.class);
        } else if (tab == 2) {
            if (activity instanceof DiscoverActivity) return;
            i = new Intent(activity, DiscoverActivity.class);
        } else if (tab == 3) {
            if (activity instanceof InboxActivity) return;
            i = new Intent(activity, InboxActivity.class);
        } else {
            i = new Intent(activity, MainActivity.class);
            i.putExtra("openTab", tab);
        }
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final Intent target = i;
        KingSafe.run(activity,"root-nav-"+tab,()->activity.startActivity(target));
    }

    public static void openMainTab(Activity activity, int tab) {
        Intent i = new Intent(activity, MainActivity.class);
        i.putExtra("openTab", tab);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final Intent target = i;
        KingSafe.run(activity,"main-tab-"+tab,()->activity.startActivity(target));
    }

    public static void confirmExit(Activity activity) {
        if (activity == null || activity.isFinishing()) return;
        new AlertDialog.Builder(activity)
            .setTitle("Exit KING Plus?")
            .setMessage("Do you want to close the app?")
            .setNegativeButton("Stay", null)
            .setPositiveButton("Exit", (d,w) -> activity.finishAffinity())
            .show();
    }
}
