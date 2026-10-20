package com.kingplus.social;

/** Pure Java guards for Party join retries, cancellations and timeouts. */
public final class KingPartyJoinTimeout977 {
    private KingPartyJoinTimeout977(){}
    public static final long JOIN_TIMEOUT_MS=18000L;
    public static boolean waiting(String expectedRoom,String room,
        String expectedUid,String uid,int token,int currentToken,
        boolean active,boolean memberReady) {
        return active && !memberReady && token>0&&token==currentToken
            && expectedRoom!=null&&!expectedRoom.isEmpty()&&expectedRoom.equals(room)
            && expectedUid!=null&&!expectedUid.isEmpty()&&expectedUid.equals(uid);
    }
    public static boolean expired(long startedElapsedMs,long currentElapsedMs){
        return startedElapsedMs>0&&currentElapsedMs>=startedElapsedMs
            && currentElapsedMs-startedElapsedMs>=JOIN_TIMEOUT_MS;
    }
}
