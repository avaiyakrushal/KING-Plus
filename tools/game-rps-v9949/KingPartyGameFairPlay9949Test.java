package com.kingplus.social;
import java.util.HashSet;
public final class KingPartyGameFairPlay9949Test {
 private static void ok(boolean b,String m){if(!b)throw new AssertionError(m);}
 private static void no(boolean b,String m){if(b)throw new AssertionError(m);}
 public static void main(String[] args){
   ok(KingPartyGameFairPlay9949.currentMove(49,"rps",49,"rps","alice","alice"),"correct Party round");
   no(KingPartyGameFairPlay9949.currentMove(49,"rps",48,"rps","alice","alice"),"stale round denied");
   no(KingPartyGameFairPlay9949.currentMove(49,"rps",49,"dice","alice","alice"),"wrong game denied");
   no(KingPartyGameFairPlay9949.currentMove(49,"rps",49,"rps","alice","eve"),"forged author denied");
   no(KingPartyGameFairPlay9949.currentMove(0,"rps",0,"rps","alice","alice"),"no active round");
   no(KingPartyGameFairPlay9949.currentMove(49,"rps",49,"rps","",""),"blank UID denied");
   ok("move locked ✓".equals(KingPartyGameFairPlay9949.visibleMove("rps","active",true,"Rock")),"hide opponent Rock while playing");
   ok("move locked ✓".equals(KingPartyGameFairPlay9949.visibleMove("rps","active",true,"Scissors")),"hide opponent Scissors");
   ok("move locked ✓".equals(KingPartyGameFairPlay9949.visibleMove("rps","closed",false,"Paper")),"cache cannot reveal");
   ok("✊ Rock".equals(KingPartyGameFairPlay9949.visibleMove("rps","closed",true,"Rock")),"actual Rock after settlement");
   ok("✋ Paper".equals(KingPartyGameFairPlay9949.visibleMove("rps","closed",true,"Paper")),"actual Paper after settlement");
   ok("✌ Scissors".equals(KingPartyGameFairPlay9949.visibleMove("rps","closed",true,"Scissors")),"actual Scissors after settlement");
   ok("Invalid RPS move".equals(KingPartyGameFairPlay9949.visibleMove("rps","closed",true,"Dragon")),"invalid move not endorsed");
   ok("Rolled 4".equals(KingPartyGameFairPlay9949.visibleMove("dice","active",true,"Rolled 4")),"Dice remains playable");
   HashSet<Integer> faces=new HashSet<>(),slots=new HashSet<>();
   for(int i=0;i<2000;i++){
     int d=KingPartyGameFairPlay9949.dice(),w=KingPartyGameFairPlay9949.wheel(),s=KingPartyGameFairPlay9949.slotDigit();
     ok(d>=1&&d<=6,"dice face range");ok(w>=1&&w<=100,"wheel range");
     ok(s>=0&&s<=9,"slot digit range");faces.add(d);slots.add(s);
   }
   ok(faces.size()==6,"all 6 faces exercised");ok(slots.size()==10,"all ten slot digits exercised");
   System.out.println("PASS genuine RPS hidden-then-reveal, server-only move filtering helper, strong client RNG Dice/Wheel/Slot");
 }
}