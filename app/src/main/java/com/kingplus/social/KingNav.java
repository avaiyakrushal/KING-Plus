package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.function.IntConsumer;

/** Consistent root-tab navigation and exit behavior for KING Plus. */
public final class KingNav {
    private KingNav() {}

    public static void openRoot(Activity activity, int tab) {
        if (activity == null) return;
        Intent i;
        if (tab == 0 || tab == 1) {
            // A hub instance may currently show KTV, PK, Match or Stage rather than Home.
            i = new Intent(activity, KingParityHubActivity.class);
            i.putExtra("route", "home");
        } else if (tab == 2) {
            // The Games tab always opens the same playable-games destination.
            i = new Intent(activity, MainActivity.class);
            i.putExtra("openTab", 1);
        } else if (tab == 3) {
            if (activity instanceof DiscoverActivity) return;
            i = new Intent(activity, DiscoverActivity.class);
        } else if (tab == 4) {
            if (activity instanceof InboxActivity) return;
            i = new Intent(activity, InboxActivity.class);
        } else {
            i = new Intent(activity, MainActivity.class);
            i.putExtra("openTab", 4);
        }
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final Intent target = i;
        KingSafe.run(activity,"root-nav-"+tab,()->activity.startActivity(target));
    }

    // Preserve historic route IDs: 1 is an alias for Home, not a sixth visible tab.
    public static final int NAV_HEIGHT_DP = 68;
    public static final int ACTION_HEIGHT_DP = 48;
    public static final int ACTION_TEXT_SP = 14;
    private static final int[] ROOT_IDS = {0, 2, 3, 4, 5};
    private static final String[] ROOT_NAMES = {"Home", "Games", "Discover", "Messages", "Me"};
    private static final String[] ROOT_ICONS = {"⌂", "🎮", "◇", "✉", "●"};

    private static int dp(Activity a, int n) {
        return Math.round(n * a.getResources().getDisplayMetrics().density);
    }

    public static LinearLayout bottomBar(Activity a, int selected, IntConsumer onSelect) {
        LinearLayout bar = new LinearLayout(a);
        bar.setGravity(Gravity.CENTER);
        bar.setPadding(dp(a,4),dp(a,4),dp(a,4),dp(a,4));
        bar.setBackground(KingRoyalTheme10021.bottomNav(a));
        int activeId = selected == 1 ? 0 : selected;
        for (int i=0;i<ROOT_IDS.length;i++) {
            final int tab=ROOT_IDS[i];
            boolean active=tab==activeId;
            LinearLayout item=new LinearLayout(a);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            if(active)item.setBackground(KingRoyalTheme10021.violetCard(a,14));
            TextView icon=new TextView(a);
            icon.setText(ROOT_ICONS[i]);icon.setTextSize(22);icon.setGravity(Gravity.CENTER);
            icon.setTextColor(active?0xffffdf80:0xff766a80);
            TextView label=new TextView(a);
            label.setText(ROOT_NAMES[i]);label.setTextSize(11);label.setGravity(Gravity.CENTER);
            label.setTypeface(null,Typeface.BOLD);
            label.setTextColor(active?Color.WHITE:0xff766a80);
            label.setSingleLine(true);
            item.addView(icon,new LinearLayout.LayoutParams(-1,dp(a,28)));
            item.addView(label,new LinearLayout.LayoutParams(-1,dp(a,23)));
            item.setContentDescription(ROOT_NAMES[i]+(active?" selected":""));
            item.setSelected(active);
            item.setOnClickListener(v->onSelect.accept(tab));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1);
            lp.setMargins(dp(a,2),0,dp(a,2),0);
            bar.addView(item,lp);
        }
        return bar;
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
