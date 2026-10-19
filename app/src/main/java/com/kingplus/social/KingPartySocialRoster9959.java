package com.kingplus.social;
import java.security.SecureRandom;
import java.util.List;
/** Only real explicitly Ready Party members can join hidden-role games. */
public final class KingPartySocialRoster9959 {
 private KingPartySocialRoster9959(){}
 public static boolean isEligible(String uid,boolean readyExists,String readyUid,
                                   Boolean readyFlag,boolean memberExists,String memberUid){
   return uid!=null&&!uid.isEmpty()&&readyExists&&Boolean.TRUE.equals(readyFlag)
       &&memberExists&&uid.equals(readyUid)&&uid.equals(memberUid);
 }
 /** Uniform choice from ALL locked Party players, including Host. */
 public static String pickHiddenRole(List<String> activePartyUids,SecureRandom random){
   if(activePartyUids==null||activePartyUids.isEmpty()||random==null)
       throw new IllegalArgumentException("Need active Party players");
   return activePartyUids.get(random.nextInt(activePartyUids.size()));
 }
}
