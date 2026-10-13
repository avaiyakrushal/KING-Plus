package com.kingplus.social;
import java.util.*;
public final class KingMeRoomHistory9981Test {
 private static void ok(boolean success,String message){if(!success)throw new AssertionError(message);}
 public static void main(String[]unused){
  Set<String> f=new HashSet<>(Arrays.asList("a\u0001Music","b\u0001Friends"));
  Set<String> r=new HashSet<>(Arrays.asList("0000000001000::a::Music","0000000001200::c::Gaming","0000000001100::d::Night","bad","0000000001000::/unsafe::Room"));
  List<KingMeRoomHistory9981.Room> favorites=KingMeRoomHistory9981.from(f,r,"favorites");
  ok(favorites.size()==2&&favorites.stream().allMatch(x->x.favorite),"real favorites");
  List<KingMeRoomHistory9981.Room> recent=KingMeRoomHistory9981.from(f,r,"recent");
  ok(recent.size()==2&&recent.get(0).id.equals("c")&&recent.get(1).id.equals("d"),"sorted actual recent");
  ok(!KingMeRoomHistory9981.validId("a/b"),"no invalid Firestore document id");
  ok(KingMeRoomHistory9981.from(Collections.emptySet(),Collections.emptySet(),"recent").isEmpty(),"never invent rooms");
  System.out.println("PASS Me Profile favorites/recent dedupe and no fake rooms");
 }
}
