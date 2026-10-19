package com.kingplus.social;

import android.content.Context;
import android.content.SharedPreferences;

/** KING Plus progression used by profile, rooms, chat and gifting. */
public final class LevelSystem {
    public static final int MAX_LEVEL = 99;
    public static final int MAX_VIP = 50;
    private static final String PREF = "king_progress_v7";
    private LevelSystem() {}

    public static final class Snapshot {
        public final int level;
        public final long xp;
        public final int vipLevel;
        public final long vipPoints;
        Snapshot(int level,long xp,int vipLevel,long vipPoints){this.level=level;this.xp=xp;this.vipLevel=vipLevel;this.vipPoints=vipPoints;}
        public long nextLevelXp(){return level>=MAX_LEVEL?xp:levelThreshold(level+1);}
        public long nextVipPoints(){return vipLevel>=MAX_VIP?vipPoints:vipThreshold(vipLevel+1);}
    }

    public static Snapshot read(Context c){
        SharedPreferences p=c.getSharedPreferences(PREF,Context.MODE_PRIVATE);
        long xp=Math.max(0,p.getLong("xp",0));
        // VIP is a paid entitlement and must come only from the verified wallet.
        // Ignore old device-local VIP counters left by earlier TEST builds.
        return new Snapshot(levelFor(xp),xp,0,0);
    }

    public static Snapshot add(Context c,long xpDelta,long vipDelta){
        SharedPreferences p=c.getSharedPreferences(PREF,Context.MODE_PRIVATE);
        long xp=Math.max(0,p.getLong("xp",0)+Math.max(0,xpDelta));
        // Device activity is a local TEST preview; never mint VIP points here.
        p.edit().putLong("xp",xp).apply();
        return new Snapshot(levelFor(xp),xp,0,0);
    }

    public static Snapshot gift(Context c,long giftValue){
        if(giftValue<=0)return add(c,2,0);
        long xp=Math.max(5,Math.min(5000,giftValue/5+5));
        return add(c,xp,0);
    }

    public static long levelThreshold(int level){
        if(level<=1)return 0;
        long n=level-1L;
        return 150L*n*n + 350L*n;
    }

    public static long vipThreshold(int vip){
        if(vip<=0)return 0;
        int v=Math.min(MAX_VIP,vip);
        long[] early={0,500,1500,3500,7000,13000,22000,36000,56000,85000,125000,180000,250000};
        if(v<=12)return early[v];
        long n=v-12L;
        return 250000L + 80000L*n + 120000L*n*n;
    }

    public static int levelFor(long xp){int l=1;while(l<MAX_LEVEL&&xp>=levelThreshold(l+1))l++;return l;}
    public static int vipFor(long p){int v=0;while(v<MAX_VIP&&p>=vipThreshold(v+1))v++;return v;}
    public static String levelTier(int l){if(l>=80)return "Legend";if(l>=60)return "Master";if(l>=40)return "Elite";if(l>=20)return "Rising";return "Starter";}
    public static String vipName(int v){int x=Math.max(0,Math.min(MAX_VIP,v));if(x==0)return "Free";if(x<=5)return new String[]{"","Bronze","Silver","Gold","Platinum","Diamond"}[x];if(x<=9)return "Royal";if(x<=14)return "Crown";if(x<=19)return "Emperor";if(x<=24)return "Mythic";if(x<=29)return "Legend";if(x<=34)return "Supreme";if(x<=39)return "KING Royal";if(x<=44)return "KING Mythic";if(x<=49)return "KING Legend";return "KING Supreme";}
}
