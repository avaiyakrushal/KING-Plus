package com.kingplus.social;
/** Local free challenge. No paid rewards; results are untrusted client display only. */
public final class KingTapRace9932 {
  public static final long DURATION_MS=8000L;
  private KingTapRace9932(){}
  public static int score(int taps,long elapsedMs){
    if(elapsedMs<DURATION_MS)return 0;
    return Math.min(500,Math.max(0,taps))*2;
  }
}
