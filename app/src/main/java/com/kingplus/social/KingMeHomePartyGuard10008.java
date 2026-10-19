package com.kingplus.social;
/** Pure-Java policies for atomic Party game close and lifecycle validity. */
public final class KingMeHomePartyGuard10008 {
    private KingMeHomePartyGuard10008() {}
    public static boolean mayCloseCurrentGame(String status,Long serverRound,long expectedRound) {
        return "active".equals(status) && serverRound!=null
            && serverRound.longValue()>0L && expectedRound>0L
            && serverRound.longValue()==expectedRound;
    }
    public static boolean callbackCurrent(int callbackEpoch,int currentEpoch,
            String requestedUid,String activeUid,boolean sameView) {
        return callbackEpoch==currentEpoch && requestedUid!=null
            && requestedUid.equals(activeUid) && sameView;
    }
}
