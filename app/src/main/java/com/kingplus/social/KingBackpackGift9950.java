package com.kingplus.social;
/** A locally earned FREE gift is not paid Diamonds or a server wallet. */
public final class KingBackpackGift9950 {
 private KingBackpackGift9950(){}
 public static boolean canBegin(int available,boolean sendPending){
  return available>0&&!sendPending;
 }
 /** Invoke only after a confirmed atomic Firestore Gift commit. */
 public static int afterCommitted(int currentlyAvailable){
  return Math.max(0,currentlyAvailable-1);
 }
}
