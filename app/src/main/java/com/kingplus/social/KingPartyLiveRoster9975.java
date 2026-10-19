package com.kingplus.social;
import java.util.*;
/** A participant who already submitted a score still cannot win after leaving. */
public final class KingPartyLiveRoster9975 {
 private KingPartyLiveRoster9975(){}
 public static boolean member(String uid,boolean exists,String storedUid){
   return uid!=null&&!uid.trim().isEmpty()&&exists&&uid.equals(storedUid);
 }
 public static boolean allPresent(Collection<String> frozen,Collection<String> present){
   if(frozen==null||present==null||frozen.size()<2||frozen.size()>50)return false;
   Set<String> unique=new HashSet<>();
   for(String uid:frozen){
     if(uid==null||uid.isEmpty()||!unique.add(uid)||!present.contains(uid))return false;
   }
   return true;
 }
}
