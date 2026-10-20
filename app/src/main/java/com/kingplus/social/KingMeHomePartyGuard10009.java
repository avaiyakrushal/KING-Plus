package com.kingplus.social;
/** Pure Java rules for Family creation collision and room-game optimistic updates. */
public final class KingMeHomePartyGuard10009 {
    private KingMeHomePartyGuard10009(){}
    public static boolean isFamilyCodeCollision(String message) {
        return message!=null && message.contains("KING_FAMILY_CODE_COLLISION_10009");
    }
    public static boolean canPublishGame(boolean exists,String status,long serverRound,long viewedRound){
        if(!exists)return viewedRound==0L;
        if(!"active".equals(status))return true;
        return serverRound>0 && serverRound==viewedRound;
    }
    public static long nextGameRound(long proposed,long oldRound) {
        if(oldRound==Long.MAX_VALUE)throw new IllegalArgumentException("Invalid round");
        return Math.max(Math.max(proposed,1L),oldRound+1L);
    }
}
