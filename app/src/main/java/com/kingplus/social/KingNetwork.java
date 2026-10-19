package com.kingplus.social;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

public final class KingNetwork {
    public interface Listener { void onChanged(boolean online); }
    private KingNetwork() {}

    public static boolean online(Context context) {
        try {
            ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(cm==null)return false;
            Network n=cm.getActiveNetwork(); if(n==null)return false;
            NetworkCapabilities c=cm.getNetworkCapabilities(n); if(c==null)return false;
            return c.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && c.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } catch(Throwable ignored){ return false; }
    }

    public static ConnectivityManager.NetworkCallback watch(Context context, Listener listener) {
        try {
            ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(cm==null)return null;
            ConnectivityManager.NetworkCallback cb=new ConnectivityManager.NetworkCallback(){
                @Override public void onAvailable(Network network){listener.onChanged(true);}
                @Override public void onLost(Network network){listener.onChanged(online(context));}
                @Override public void onCapabilitiesChanged(Network network,NetworkCapabilities caps){listener.onChanged(caps!=null&&caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)&&caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));}
            };
            cm.registerNetworkCallback(new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(),cb);
            return cb;
        } catch(Throwable ignored){return null;}
    }

    public static void unwatch(Context context, ConnectivityManager.NetworkCallback cb){
        if(cb==null)return;try{ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);if(cm!=null)cm.unregisterNetworkCallback(cb);}catch(Throwable ignored){}
    }
}
