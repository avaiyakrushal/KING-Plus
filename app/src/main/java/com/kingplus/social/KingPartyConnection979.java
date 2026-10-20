package com.kingplus.social;

/** Distinguish Firestore transport failures from denied/invalid room access.
 * Retries never bypass server member/ban/private-room rules. */
public final class KingPartyConnection979 {
  private KingPartyConnection979(){}
  public static boolean isRetryable(String code,String message){
    String c=code==null?"":code.trim().toLowerCase(java.util.Locale.US);
    String m=message==null?"":message.toLowerCase(java.util.Locale.US);
    return c.equals("unavailable")||c.equals("deadline_exceeded")
        ||m.contains("client is offline")||m.contains("network is unavailable")
        ||m.contains("failed to get document because the client is offline")
        ||m.contains("unable to resolve host")||m.contains("network failure");
  }
  public static boolean canRejoin(String expectedRoom,String room,
      String expectedUid,String signedUid,int generation,int activeGeneration,
      boolean activityAlive,boolean liveRoom){
    return activityAlive&&liveRoom&&generation>0&&generation==activeGeneration
        &&expectedRoom!=null&&!expectedRoom.isEmpty()&&expectedRoom.equals(room)
        &&expectedUid!=null&&!expectedUid.isEmpty()&&expectedUid.equals(signedUid);
  }
  public static String status(boolean validatedInternet,boolean account,boolean sameProject){
    if(!validatedInternet)return "No validated Internet connection (check mobile data / Wi-Fi / VPN).";
    if(!account)return "Firebase session expired. Sign in again on this phone.";
    if(!sameProject)return "Different Firebase projects. Install the same KING Plus APK on both phones.";
    return "Internet detected but Firebase Firestore connection is not responding.";
  }
  public static boolean distinctMembers(String firstUid,String secondUid){
    return firstUid!=null&&secondUid!=null&&!firstUid.isEmpty()&&!secondUid.isEmpty()
       &&!firstUid.equals(secondUid);
  }
}
