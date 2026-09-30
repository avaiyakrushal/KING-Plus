package com.kingplus.social;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public final class EconomyManager {
    private static final String PREF = "king_economy";
    private EconomyManager() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static int getTestCoins(Context context, int fallback) {
        SharedPreferences p = prefs(context);
        if (!p.contains("testCoins")) p.edit().putInt("testCoins", Math.max(0, fallback)).apply();
        return p.getInt("testCoins", Math.max(0, fallback));
    }

    public static boolean spendTestCoins(Context context, int cost, String gift, String target) {
        if (cost <= 0) return false;
        SharedPreferences p = prefs(context);
        int balance = getTestCoins(context, 2500);
        if (balance < cost) return false;
        long sent = p.getLong("sentCoins", 0L) + cost;
        long gifts = p.getLong("giftsSent", 0L) + 1L;
        long xp = p.getLong("userXp", 0L) + Math.max(1, cost / 10);
        p.edit()
            .putInt("testCoins", balance - cost)
            .putLong("sentCoins", sent)
            .putLong("giftsSent", gifts)
            .putLong("wealthXp", sent)
            .putLong("userXp", xp)
            .putString("history", prepend(p.getString("history", ""), "SENT|" + clean(gift) + "|" + cost + "|" + clean(target) + "|" + System.currentTimeMillis()))
            .apply();
        return true;
    }

    public static void recordTestGiftReceived(Context context, int value, String gift, String from) {
        if (value <= 0) return;
        SharedPreferences p = prefs(context);
        long received = p.getLong("receivedCoins", 0L) + value;
        long gifts = p.getLong("giftsReceived", 0L) + 1L;
        long xp = p.getLong("userXp", 0L) + Math.max(1, value / 10);
        p.edit()
            .putLong("receivedCoins", received)
            .putLong("giftsReceived", gifts)
            .putLong("charmXp", received)
            .putLong("userXp", xp)
            .putString("history", prepend(p.getString("history", ""), "RECEIVED|" + clean(gift) + "|" + value + "|" + clean(from) + "|" + System.currentTimeMillis()))
            .apply();
    }

    public static void noteCloudGiftSent(Context context, int value, String gift, String target) {
        SharedPreferences p = prefs(context);
        p.edit().putString("cloudHistory", prepend(p.getString("cloudHistory", ""), "CLOUD|" + clean(gift) + "|" + value + "|" + clean(target) + "|" + System.currentTimeMillis())).apply();
    }

    public static long sentCoins(Context context) { return prefs(context).getLong("sentCoins", 0L); }
    public static long receivedCoins(Context context) { return prefs(context).getLong("receivedCoins", 0L); }
    public static long giftsSent(Context context) { return prefs(context).getLong("giftsSent", 0L); }
    public static long giftsReceived(Context context) { return prefs(context).getLong("giftsReceived", 0L); }
    public static long userXp(Context context) { return prefs(context).getLong("userXp", 0L); }
    public static int userLevel(Context context) { return level(userXp(context), 500L); }
    public static int wealthLevel(Context context) { return level(sentCoins(context), 1000L); }
    public static int charmLevel(Context context) { return level(receivedCoins(context), 1000L); }

    public static int level(long xp, long step) {
        if (step <= 0) step = 1000L;
        long level = xp / step + 1L;
        return (int)Math.max(1L, Math.min(999L, level));
    }

    public static String equippedProfileFrame(Context context) {
        return prefs(context).getString("profileFrame", "Classic");
    }

    public static String equippedRoomFrame(Context context) {
        return prefs(context).getString("roomFrame", "Classic");
    }

    public static void equipProfileFrame(Context context, String frame) {
        prefs(context).edit().putString("profileFrame", clean(frame)).apply();
    }

    public static void equipRoomFrame(Context context, String frame) {
        prefs(context).edit().putString("roomFrame", clean(frame)).apply();
    }

    public static List<String> localHistory(Context context) {
        List<String> out = new ArrayList<>();
        String raw = prefs(context).getString("history", "");
        if (raw == null || raw.trim().isEmpty()) return out;
        String[] rows = raw.split("\\n");
        for (String row : rows) if (!row.trim().isEmpty()) out.add(row);
        return out;
    }

    private static String prepend(String old, String row) {
        String result = row;
        if (old != null && !old.trim().isEmpty()) result += "\n" + old;
        String[] rows = result.split("\\n");
        if (rows.length <= 50) return result;
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            if (i > 0) b.append('\n');
            b.append(rows[i]);
        }
        return b.toString();
    }

    private static String clean(String value) {
        if (value == null) return "";
        return value.replace("|", " ").replace("\n", " ").trim();
    }
}
