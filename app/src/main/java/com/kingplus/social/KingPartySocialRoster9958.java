package com.kingplus.social;
import java.util.*;
/** Guard for authenticated in-room Party Werewolf, Spy and Draw participants. */
public final class KingPartySocialRoster9958 {
 private KingPartySocialRoster9958(){}
 public static boolean isLive(String uid,boolean ready,String readyUid,
                             boolean member,String memberUid){
  return uid!=null&&!uid.isEmpty()&&ready&&member
      &&uid.equals(readyUid)&&uid.equals(memberUid);
 }
 public static boolean valid(String game,List<String> live){
  int min="werewolf".equals(game)?4:("spy".equals(game)?3:("draw".equals(game)?2:999));
  if(live==null||live.size()<min||live.size()>40)return false;
  HashSet<String> seen=new HashSet<>();
  for(String uid:live)if(uid==null||uid.isEmpty()||!seen.add(uid))return false;
  return true;
 }
}
