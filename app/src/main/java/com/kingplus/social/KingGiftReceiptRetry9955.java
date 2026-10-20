package com.kingplus.social;
/** Live real-gift recipient receipt retry. Never confuses a sent Gift with delivery. */
public final class KingGiftReceiptRetry9955 {
 private KingGiftReceiptRetry9955(){}
 public static final int MAX_PENDING=64, MAX_RETRIES=4;
 public static boolean allowEnqueue(int pending){return pending>=0&&pending<MAX_PENDING;}
 public static boolean shouldRetry(int failures,boolean liveMember,boolean foreground){
   return failures>=0&&failures<MAX_RETRIES&&liveMember&&foreground;
 }
 public static long delayMs(int attempt){return 1000L*(1L<<Math.min(4,Math.max(1,attempt)));}
}
