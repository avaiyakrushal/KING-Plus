package com.kingplus.social;
/** Bounded recent genuinely completed receiver-side Gift visual records.
 * This is not a wallet, payment validation, or proof for another account.
 */
public final class KingGiftPlayedHistory9969 {
 private KingGiftPlayedHistory9969(){}
 public static final int MAX_EVENTS=512;
 public static final long HISTORY_MS=6L*60L*60L*1000L;
 private static boolean validPart(String x){
     return x!=null&&!x.isEmpty()&&x.length()<=256&&x.indexOf('/')<0;
 }
 public static boolean valid(String room,String gift,String uid){
     return validPart(room)&&validPart(gift)&&validPart(uid);
 }
 public static boolean same(String room,String gift,String uid,
                            String otherRoom,String otherGift,String otherUid){
     return valid(room,gift,uid)&&room.equals(otherRoom)&&
            gift.equals(otherGift)&&uid.equals(otherUid);
 }
 public static boolean withinHistory(long now,long at){
     return now>0&&at>0&&at<=now+10000L&&now-at<=HISTORY_MS;
 }
}
