package com.kingplus.social;
/** Bounded local journal for genuinely displayed Party Gift receiver ACKs.
 * The journal is NOT a balance or server authorization, and every restored
 * gift event + room membership must be re-read online before acknowledgement.
 */
public final class KingGiftReceiptDurable9962 {
 private KingGiftReceiptDurable9962(){}
 public static final int MAX_PENDING=128;
 public static boolean valid(String room,String id,String uid){
   return validPart(room)&&validPart(id)&&validPart(uid);
 }
 private static boolean validPart(String str){
   return str!=null&&!str.isEmpty()&&str.length()<=256
       &&str.indexOf('/')<0&&str.indexOf('\\')<0;
 }
 public static boolean canAdd(int count){
   return count>=0&&count<MAX_PENDING;
 }
 public static boolean same(String r,String id,String u,String er,String eid,String eu){
   return valid(r,id,u)&&r.equals(er)&&id.equals(eid)&&u.equals(eu);
 }
}
