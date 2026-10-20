package com.kingplus.social;
import java.util.*;
/** Genuine Party round cancellation; no winner or diamond award. */
public final class KingRoomCancel9947 {
 private KingRoomCancel9947(){}
 public static final String RESULT="🚫 Round cancelled • no winner";
 public static boolean canCancel(boolean moderator,boolean member,
                                 boolean serverVerified,String status,long round){
   return moderator&&member&&serverVerified&&"active".equals(status)&&round>0;
 }
 public static int countValid(List<?> frozen,List<String> submitted){
   if(frozen==null||submitted==null)return 0;
   Set<String> present=new HashSet<>();
   for(String uid:submitted)
     if(uid!=null&&frozen.contains(uid))present.add(uid);
   return present.size();
 }
}
