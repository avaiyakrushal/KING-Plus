package com.kingplus.social;
import java.util.*;
public final class KingRoomCancel9947Test {
 private static void yes(boolean c,String message){if(!c)throw new AssertionError(message);}
 private static void no(boolean c,String message){if(c)throw new AssertionError(message);}
 public static void main(String[] ignored) {
  yes(KingRoomCancel9947.canCancel(true,true,true,"active",9947),"verified Host active round can cancel");
  no(KingRoomCancel9947.canCancel(false,true,true,"active",9947),"ordinary member cannot cancel");
  no(KingRoomCancel9947.canCancel(true,false,true,"active",9947),"kicked moderator cannot cancel");
  no(KingRoomCancel9947.canCancel(true,true,false,"active",9947),"offline/cache cannot cancel");
  no(KingRoomCancel9947.canCancel(true,true,true,"closed",9947),"finished/cancelled round cannot cancel");
  no(KingRoomCancel9947.canCancel(true,true,true,"active",0),"round id required");
  List<String> roster=Arrays.asList("alice","bob","charlie");
  yes(KingRoomCancel9947.countValid(roster,Arrays.asList("alice","bob"))==2,"two of three");
  yes(KingRoomCancel9947.countValid(roster,Arrays.asList("alice","bob","eve"))==2,"spectator cannot fill place");
  yes(KingRoomCancel9947.countValid(roster,Arrays.asList("alice","alice","bob"))==2,"duplicate cannot fill place");
  yes(KingRoomCancel9947.countValid(roster,Arrays.asList("alice","bob","charlie"))==3,"all ready");
  yes(KingRoomCancel9947.RESULT.contains("no winner"),"cancel never names a champion");
  System.out.println("PASS Host Cancel, kicked/late/offline guard, verified 2-of-3 missing player count, no-winner settlement");
 }
}
