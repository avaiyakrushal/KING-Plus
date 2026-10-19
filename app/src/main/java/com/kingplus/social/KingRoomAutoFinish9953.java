package com.kingplus.social;
import java.util.List;
/** Use only SERVER-confirmed moves, already filtered by round/game/UID.
 * A Host must finish on the trusted SERVER snapshot, never on local cache.
 */
public final class KingRoomAutoFinish9953 {
 private KingRoomAutoFinish9953(){}
 public static boolean shouldFinish(String type,String status,boolean moderator,
        boolean liveMember,boolean serverVerified,long roundId,
        List<?> participants,List<String> submitted){
   return moderator&&liveMember&&serverVerified&&roundId>0
       &&"active".equals(status)&&KingRoomRoster9946.lockedGame(type)
       &&KingRoomRoster9946.complete(participants,submitted);
 }
}
