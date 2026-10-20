package com.kingplus.social;

/** Guards delayed Firebase snapshots from an abandoned Party visit.
 * A new visit to the SAME Room with the SAME Firebase user must also
 * invalidate callbacks belonging to the previous visit generation.
 */
public final class KingRoomCallback982 {
    private KingRoomCallback982(){}

    public static boolean current(String expectedRoom,String expectedUid,int expectedVisit,
                                  String currentRoom,String signedUid,int currentVisit,
                                  boolean cloudRoom,boolean alive){
        return cloudRoom && alive && expectedVisit>0 && expectedVisit==currentVisit
            && expectedRoom!=null && !expectedRoom.trim().isEmpty()
            && expectedRoom.equals(currentRoom)
            && expectedUid!=null && !expectedUid.isEmpty()
            && expectedUid.equals(signedUid);
    }

    public static String supportReport(String version,String code,String uidPrefix,
                                       String failure,boolean internetOk,boolean signedIn) {
        String v=clean(version,70),room=clean(code,30),
               uid=clean(uidPrefix,30),why=clean(failure,240);
        return "KING Plus Party support report\n"
            +"Build: "+v+"\n"
            +"Room code: "+room+"\n"
            +"Firebase UID prefix: "+uid+"\n"
            +"Internet validated: "+internetOk+"\n"
            +"Firebase signed in: "+signedIn+"\n"
            +"Error: "+why+"\n"
            +"Use 2 distinct Google/Firebase accounts with the same exact Room invitation.";
    }

    private static String clean(String text,int limit){
        if(text==null)return "unknown";
        String s=text.replaceAll("[\\r\\n\\t\\p{Cntrl}]"," ").trim();
        return s.length()<=limit?s:s.substring(0,limit);
    }
}
