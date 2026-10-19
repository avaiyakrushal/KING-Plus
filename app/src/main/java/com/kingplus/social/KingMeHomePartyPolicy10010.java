package com.kingplus.social;
/** Pure-Java guards for server-verified Party Guess It rounds. */
public final class KingMeHomePartyPolicy10010 {
    private KingMeHomePartyPolicy10010(){}
    public static boolean activeGuess(boolean exists,String type,String status,long round){
        return exists && "guess".equals(type) && "active".equals(status) && round>0L;
    }
    public static boolean sameGuessRound(boolean exists,String type,String status,long round,long expected){
        return activeGuess(exists,type,status,round) && expected>0L && round==expected;
    }
}
