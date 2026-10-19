package com.kingplus.social;
import java.util.List;
/** All actual Ready participants must play before a Party mini-game result.
 * Never declare a winner with only 2 of 4 real players submitting.
 */
public final class KingPartyAllPlayed9973 {
 private KingPartyAllPlayed9973(){}
 public static boolean complete(String game,List<?> frozen,List<String> submitted){
     return KingPartyRoundRoster9972.isOrdinaryGame(game)
         &&KingRoomRoster9946.complete(frozen,submitted);
 }
 public static boolean shouldFinish(String game,String status,boolean host,
     boolean member,boolean serverVerified,long roundId,
     List<?> frozen,List<String> submitted){
     return host&&member&&serverVerified&&roundId>0
       &&"active".equals(status)&&complete(game,frozen,submitted);
 }
}
