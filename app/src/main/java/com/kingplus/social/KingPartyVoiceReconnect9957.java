package com.kingplus.social;
/** An Internet reconnect is not consent to open Jitsi voice or the microphone. */
public final class KingPartyVoiceReconnect9957 {
 private KingPartyVoiceReconnect9957(){}
 public static boolean eligible(boolean pending,boolean optedIn,boolean foreground,
    boolean online,boolean cloudRoom,boolean memberSeen,
    String expectedRoom,String actualRoom,String expectedUid,String actualUid,
    boolean generationMatches,boolean partyViewVisible){
  return pending&&optedIn&&foreground&&online&&cloudRoom&&memberSeen
      &&expectedRoom!=null&&!expectedRoom.isEmpty()
      &&expectedRoom.equals(actualRoom)
      &&expectedUid!=null&&!expectedUid.isEmpty()
      &&expectedUid.equals(actualUid)
      &&generationMatches&&partyViewVisible;
 }
}
