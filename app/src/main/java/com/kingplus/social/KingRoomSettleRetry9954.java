package com.kingplus.social;
/** Bounded host-side reconnect retries; no client-authoritative award. */
public final class KingRoomSettleRetry9954 {
 private KingRoomSettleRetry9954(){}
 public static final int MAX_RETRIES=4;
 public static boolean allowRetry(int failures,boolean sameRound,
                            boolean moderator,boolean liveMember,boolean active){
   return failures>=0&&failures<MAX_RETRIES&&sameRound&&moderator&&liveMember&&active;
 }
 public static long delayMs(int attempt){
   if(attempt<=0)return 0L;
   return 1000L*(1L<<Math.min(4,attempt));
 }
}
