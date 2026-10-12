package com.kingplus.social;
/** A reaction score must be measured from GO, not from view creation. */
public final class KingReactionRules9927 {
    private KingReactionRules9927() {}
    public static final int FALSE_START=0;
    public static final long MAX_VALID_MS=30000L;
    public static int score(long elapsedMs){
        if(elapsedMs<1||elapsedMs>MAX_VALID_MS)throw new IllegalArgumentException("Invalid GO latency");
        return (int)Math.max(1,100000-Math.min(99999,elapsedMs));
    }
    public static int progress(long elapsedMs){
        return (int)Math.min(100,Math.max(0,elapsedMs*100/5000));
    }
}