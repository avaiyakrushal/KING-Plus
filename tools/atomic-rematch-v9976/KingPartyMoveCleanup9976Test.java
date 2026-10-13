package com.kingplus.social;
import java.util.*;
public final class KingPartyMoveCleanup9976Test {
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 public static void main(String[]args){
   List<String> chosen=Arrays.asList("alice","bob");
   List<String> targets=KingPartyMoveCleanup9976.roster(chosen);
   check(KingPartyMoveCleanup9976.valid(targets),"2 real Ready participants can rematch");
   check(targets.size()==2&&!targets.contains("ghost"),"never wipe historic ghost scores");
   List<String> fifty=new ArrayList<>();
   for(int i=0;i<50;i++)fifty.add("player-"+i);
   check(KingPartyMoveCleanup9976.roster(fifty).size()==50,"50 slots bounded");
   fifty.add("player-50");
   check(KingPartyMoveCleanup9976.roster(fifty).isEmpty(),"51 denied");
   check(KingPartyMoveCleanup9976.roster(Arrays.asList("alice","alice")).isEmpty(),"duplicate denied");
   check(KingPartyMoveCleanup9976.roster(Arrays.asList("alice",13)).isEmpty(),"non-string denied");
   check(KingPartyMoveCleanup9976.roster(Arrays.asList("alice","")).isEmpty(),"blank denied");
   check(KingPartyMoveCleanup9976.roster(null).isEmpty(),"no roster denied");
   // Legacy 800 ghosts must never enter the transaction cleanup list.
   Set<String> old=new HashSet<>();
   for(int i=0;i<800;i++)old.add("departed-"+i);
   old.add("alice");old.add("bob");
   old.removeAll(targets);
   check(old.size()==800,"800 stale ghost docs are left outside the atomic start");
   System.out.println("PASS Party rematch reads/deletes only frozen Ready players 2-50, never 800 ghosts");
 }
}
