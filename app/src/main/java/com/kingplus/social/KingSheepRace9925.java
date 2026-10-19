package com.kingplus.social;
/** Pure deterministic physics/validation for original KING 8s Sheep Sprint. */
public final class KingSheepRace9925 {
    private KingSheepRace9925(){}
    public static final int MAX_TAPS=200;
    public static int nextTap(int taps){return Math.min(MAX_TAPS,Math.max(0,taps)+1);}
    public static int progress(int taps){return Math.min(100,Math.max(0,taps));}
    /** A score exists only after a complete measured 8-second round. */
    public static int score(int taps,long elapsedMs){
        if(elapsedMs<8000)throw new IllegalArgumentException("Race not complete");
        return Math.max(0,Math.min(MAX_TAPS,taps));
    }
}
