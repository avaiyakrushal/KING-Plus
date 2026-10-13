package com.kingplus.social;
import java.util.*;
public final class KingRoomRoster9946Test {
 static void ok(boolean v,String s){if(!v)throw new AssertionError(s);}
 static void no(boolean v,String s){if(v)throw new AssertionError(s);}
 public static void main(String[] args){
   ok(KingRoomRoster9946.lockedGame("rps"),"RPS locked");
   ok(KingRoomRoster9946.lockedGame("dice"),"Dice locked");
   ok(KingRoomRoster9946.lockedGame("coin"),"Coin locked");
   no(KingRoomRoster9946.lockedGame("ttt"),"TTT uses own turn engine");
   List<String> roster=Arrays.asList("alice","bob","charlie");
   ok(KingRoomRoster9946.valid(roster),"three distinct players");
   no(KingRoomRoster9946.complete(roster,Arrays.asList("alice","bob")),"2 of 3 cannot finish");
   ok(KingRoomRoster9946.complete(roster,Arrays.asList("charlie","alice","bob")),"all 3 can finish regardless order");
   no(KingRoomRoster9946.complete(roster,Arrays.asList("alice","bob","eve")),"not-ready spectator cannot substitute");
   no(KingRoomRoster9946.complete(roster,Arrays.asList("alice","alice","bob")),"duplicate score rejected");
   no(KingRoomRoster9946.complete(roster,Arrays.asList("alice","bob","charlie","eve")),"extra forged move blocks finish");
   no(KingRoomRoster9946.valid(Arrays.asList("alice","alice")),"duplicate ready UID rejected");
   no(KingRoomRoster9946.valid(Arrays.asList("alice","")),"blank ready UID rejected");
   no(KingRoomRoster9946.valid(Arrays.asList("solo")),"needs two");
   ArrayList<String> many=new ArrayList<>();for(int i=0;i<50;i++)many.add("player-"+i);
   ok(KingRoomRoster9946.valid(many),"50 player round allowed");
   many.add("overflow");no(KingRoomRoster9946.valid(many),"51 player round denied");
   System.out.println("PASS Frozen RPS/Dice/Coin Ready roster: 2/3 premature blocked, all ready wins, spoofed/replayed moves denied");
 }
}
