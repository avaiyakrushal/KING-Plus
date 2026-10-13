package com.kingplus.social;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
public final class KingRoomAutoFinish9953Test {
 private static void yes(boolean condition,String message){
   if(!condition)throw new AssertionError(message);
 }
 private static void no(boolean condition,String message){yes(!condition,message);}
 private static boolean ready(String kind,boolean host,boolean member,
      boolean server,long round,List<?> expected,List<String> actual){
   return KingRoomAutoFinish9953.shouldFinish(kind,"active",host,member,server,round,expected,actual);
 }
 public static void main(String[] unused){
  List<String> players=Arrays.asList("alice","bob");
  yes(ready("rps",true,true,true,53,players,Arrays.asList("bob","alice")),
       "RPS auto-finishes when real complete");
  yes(ready("dice",true,true,true,53,players,Arrays.asList("alice","bob")),
       "Dice auto-finishes when complete");
  yes(ready("coin",true,true,true,53,players,Arrays.asList("alice","bob")),
       "Coin auto-finishes when complete");
  no(ready("rps",false,true,true,53,players,players),"only host settles");
  no(ready("rps",true,false,true,53,players,players),"host removed from Party");
  no(ready("rps",true,true,false,53,players,players),"cache not enough");
  no(ready("rps",true,true,true,53,players,Collections.singletonList("alice")),
     "partial game must not finish");
  no(ready("dice",true,true,true,53,players,Arrays.asList("alice","alice")),
     "duplicate response must not count as second player");
  no(ready("coin",true,true,true,53,players,Arrays.asList("alice","charlie")),
     "spectator not in locked roster");
  no(ready("coin",true,true,true,0,players,players),"no active round");
  no(ready("ttt",true,true,true,53,players,players),"TTT uses turn mechanics");
  no(ready("ludo",true,true,true,53,players,players),"Ludo unaffected");
  no(KingRoomAutoFinish9953.shouldFinish("rps","closed",true,true,true,53,
       players,players),"never settle completed round");
  System.out.println("PASS v9.9.53 automatic real Party result gates: RPS/Dice/Coin");
 }
}
