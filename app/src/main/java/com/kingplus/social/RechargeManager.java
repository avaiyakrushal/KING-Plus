package com.kingplus.social;

import android.content.SharedPreferences;

public final class RechargeManager {
    public interface Result { void onComplete(int newBalance, String receipt); }
    private RechargeManager() {}

    public static void simulateTestRecharge(SharedPreferences prefs, int coins, int displayRupees, Result result) {
        int balance = prefs.getInt("coins", 2500) + coins;
        String receipt = "TEST-RCH-" + System.currentTimeMillis() + " • " + coins + " coins • ₹" + displayRupees + " display price • NO MONEY CHARGED";
        String old = prefs.getString("test_recharge_history", "");
        prefs.edit().putInt("coins", balance).putString("test_recharge_history", receipt + "\n" + old).apply();
        result.onComplete(balance, receipt);
    }
}
