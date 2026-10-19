package com.kingplus.social;
/** Exact-bound event replay policy; independent of Android and Firebase classes. */
public final class KingGiftReplayGuard9942 {
 private KingGiftReplayGuard9942(){}
 public static boolean allow(long now,long created,String sender,String ownUid,
                             boolean replay,boolean recipient){
   if(now<=0||created<=0||sender==null||ownUid==null
       ||sender.isEmpty()||ownUid.isEmpty()||sender.equals(ownUid))return false;
   if(replay&&!recipient)return false;
   long age=now-created;
   return age>=-10000L && age<=120000L;
 }
}
