package com.kingplus.social;
import java.util.*;
/** Server-frozen genuine Ready Party participants across ordinary shared games. */
public final class KingPartyRoundRoster9972 {
 private KingPartyRoundRoster9972(){}
 private static final Set<String> GAMES=new HashSet<>(Arrays.asList(
   "rps","dice","number","coin","wheel","bingo","slot",
   "reaction","highlow","sheep","zoo","memory","domino","treasure"));
 public static boolean isOrdinaryGame(String game){return GAMES.contains(game);}
 public static boolean realReady(String doc,String claimed,Boolean ready,boolean member){
   return doc!=null&&!doc.isEmpty()&&doc.equals(claimed)&&Boolean.TRUE.equals(ready)&&member;
 }
 public static List<String> asRoster(Object list){
   if(!(list instanceof List))return Collections.emptyList();
   List<String> out=new ArrayList<>();
   for(Object o:(List<?>)list){if(!(o instanceof String))return Collections.emptyList();out.add((String)o);}
   return out;
 }
 public static boolean valid(List<?> list){return KingRoomRoster9946.valid(list);}
 public static boolean canPlay(List<?> roster,String uid){return valid(roster)&&uid!=null&&roster.contains(uid);}
 public static boolean containsOnly(List<?> roster,List<String> submitted){
   if(!valid(roster)||submitted==null||submitted.size()<2)return false;
   Set<String> seen=new HashSet<>();
   for(String uid:submitted)if(!canPlay(roster,uid)||!seen.add(uid))return false;
   return true;
 }
}
