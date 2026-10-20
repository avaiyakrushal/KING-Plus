package com.kingplus.social;

/** Trusted-gift boundaries: TEST coins must never impersonate a paid wallet gift. */
public final class KingSocialGift972 {
    private KingSocialGift972(){}
    public static boolean valid(String sender,String receiver,int unit,int qty,int total) {
        return sender != null && !sender.isEmpty() && receiver != null && !receiver.isEmpty()
            && !sender.equals(receiver) && unit>0 && qty>=1 && qty<=100
            && (long)unit * qty == (long)total && total > 0 && total <= 100000;
    }
    public static boolean verifiedVip(int level,long points) {
        return level>=0 && level<=50 && points>=0 && points<=1000000000L
            && level == levelFromPoints(points);
    }
    private static long threshold(int level) {
        if(level<=0)return 0;
        int v=Math.min(level,50);
        long[] early={0,500,1500,3500,7000,13000,22000,36000,56000,85000,125000,180000,250000};
        if(v<=12)return early[v];
        long n=v-12L;return 250000L+80000L*n+120000L*n*n;
    }
    public static int levelFromPoints(long pts) {
        if(pts<0)return 0;
        int lvl=0;
        while(lvl<50 && pts>=threshold(lvl+1))lvl++;
        return lvl;
    }
}
