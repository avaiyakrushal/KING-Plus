package com.kingplus.social;

/** Pure-Java checks for lifecycle-sensitive Me / Home / Party callbacks. */
public final class KingMeHomePartyGuard10007 {
    private KingMeHomePartyGuard10007() {}

    public static boolean visitorCountCurrent(String requestedUid, String activeUid,
            int requestedGeneration,int currentGeneration,boolean onProfile,boolean sameChip) {
        return requestedUid!=null && requestedUid.equals(activeUid)
            && requestedGeneration==currentGeneration && onProfile && sameChip;
    }

    public static boolean momentCallbackCurrent(String postingUid,String activeUid,
            int generation,int currentGeneration,boolean onMoments,boolean sameHost) {
        return postingUid!=null && postingUid.equals(activeUid)
            && generation==currentGeneration && onMoments && sameHost;
    }

    public static boolean gameIsActive(String status,long roundId) {
        return "active".equals(status) && roundId>0L;
    }
}
