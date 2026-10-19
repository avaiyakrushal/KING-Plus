package com.kingplus.social;
/** A live Ready player must opt in and still be a verified Party member. */
public final class KingRealReadyRoster9971 {
 private KingRealReadyRoster9971(){}
 public static boolean realReady(String docUid,String claimedUid,Boolean ready,
                                 boolean actualMember){
   return docUid!=null&&!docUid.isEmpty()&&docUid.equals(claimedUid)
       &&Boolean.TRUE.equals(ready)&&actualMember;
 }
}
