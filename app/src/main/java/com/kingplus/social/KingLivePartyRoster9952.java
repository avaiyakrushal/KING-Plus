package com.kingplus.social;
/** Real SERVER-verified Room membership and Ready for a new multiplayer round. */
public final class KingLivePartyRoster9952 {
 private KingLivePartyRoster9952(){}
 public static boolean isActiveReady(String expected,boolean readyExists,String readyUid,
                                      Boolean readyFlag,boolean memberExists,String memberUid){
   return Boolean.TRUE.equals(readyFlag) &&
     isActiveReady(expected,readyExists,readyUid,memberExists,memberUid);
 }
 public static boolean isActiveReady(String expected,boolean readyExists,String readyUid,
                                      boolean memberExists,String memberUid){
   return expected!=null&&!expected.isEmpty()&&readyExists&&memberExists
       &&expected.equals(readyUid)&&expected.equals(memberUid);
 }
}
