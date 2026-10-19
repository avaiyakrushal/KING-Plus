package com.kingplus.social;
/** Source of truth for genuine FREE Gift after Party connectivity returns. */
public final class KingGiftReturnGate9968 {
 private KingGiftReturnGate9968(){}
 public static boolean shouldRecover(boolean cloud,boolean member,
         boolean foreground,boolean db,boolean user,boolean room){
     return cloud&&member&&foreground&&db&&user&&room;
 }
 public static boolean validServerGift(String documentId,String giftId,String type,
     boolean free,long qty,String sender,String receiver,String signedIn){
     return documentId!=null&&!documentId.isEmpty()
       &&documentId.equals(giftId)&&"gift".equals(type)&&free
       &&qty>=1&&qty<=9&&sender!=null&&!sender.isEmpty()
       &&receiver!=null&&!receiver.isEmpty()
       &&receiver.equals(signedIn)&&!receiver.equals(sender);
 }
}
