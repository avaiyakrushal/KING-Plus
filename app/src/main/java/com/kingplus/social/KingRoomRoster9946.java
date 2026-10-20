package com.kingplus.social;
import java.util.*;
/** Pure-Java, deterministic Room party participant/settlement consistency.
 * Ready UIDs are frozen at the moment the round is started.
 */
public final class KingRoomRoster9946 {
 private KingRoomRoster9946(){}
 public static boolean lockedGame(String type){
   return "rps".equals(type)||"dice".equals(type)||"coin".equals(type);
 }
 public static boolean valid(List<?> players){
   if(players==null||players.size()<2||players.size()>50)return false;
   Set<String> uids=new HashSet<>();
   for(Object player:players){
     if(!(player instanceof String)||((String)player).trim().isEmpty()
        ||!uids.add((String)player))return false;
   }
   return true;
 }
 public static boolean complete(List<?> ready,List<String> submitted){
   if(!valid(ready)||submitted==null||submitted.size()!=ready.size())return false;
   Set<String> submittedSet=new HashSet<>();
   for(String uid:submitted)
     if(uid==null||!submittedSet.add(uid))return false;
   return submittedSet.equals(new HashSet<>(ready));
 }
}
