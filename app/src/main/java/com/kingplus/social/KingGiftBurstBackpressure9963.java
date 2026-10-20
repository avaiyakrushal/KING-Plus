package com.kingplus.social;
/** In-memory delivery guard. Durable on-screen receipts are journaled
 * BEFORE this cap; overflow must NOT be dropped as a visual duplicate.
 */
public final class KingGiftBurstBackpressure9963 {
 private KingGiftBurstBackpressure9963(){}
 public static boolean canSendNow(int inMemory){
   return KingGiftReceiptRetry9955.allowEnqueue(inMemory);
 }
 public static boolean mustRetainOnDisk(int inMemory){
   return inMemory>=KingGiftReceiptRetry9955.MAX_PENDING;
 }
}
