package com.kingplus.social;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

/** Reports whether Android currently has a validated internet connection. */
public final class KingNetwork {
    private KingNetwork() {}
    public static boolean online(Context context) {
        try {
            ConnectivityManager manager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(manager==null)return false;
            Network active=manager.getActiveNetwork();
            NetworkCapabilities caps=active==null?null:manager.getNetworkCapabilities(active);
            return caps!=null&&caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    &&caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } catch (RuntimeException ignored) { return false; }
    }
}
