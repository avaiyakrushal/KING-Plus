package com.kingplus.social;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Pure deterministic Ludo die derived from Firestore server-generated timestamp.
 *
 * Previous implementation: (server milliseconds % 6) + 1, which is biased and
 * trivially predictable from the clock. This implementation hashes the full
 * seconds + nanoseconds, unique room code and revision and uses rejection
 * sampling to remove modulo bias.
 *
 * IMPORTANT: deterministic / improved unpredictability != cryptographic fairness.
 * Firestore validates this deterministic result and every board transition.
 * This is a free game, not a cryptographic random beacon or a paid prize game.
 */
public final class KingLudoDiceEntropy999 {
    private KingLudoDiceEntropy999(){}
    public static int dice(long seconds,int nanos,String roomCode,long revision){
        if(seconds<=0 || nanos<0 || nanos>=1_000_000_000
          ||roomCode==null||roomCode.isEmpty()||revision<0)
            throw new IllegalArgumentException("Invalid server dice entropy");
        final String payload="KING-PLUS-LUDO-FAIR-V2|"+seconds+"|"+nanos+
                "|"+roomCode+"|"+revision;
        try {
            MessageDigest digest=MessageDigest.getInstance("SHA-256");
            byte[] bytes=digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            // Read nibbles in order; reject c..f. The first 0..b nibble
            // supplies one of six equally sized outcomes and can be verified
            // directly by Firestore hashing/regex rules without trusting a client die.
            for(byte item:bytes){
                int high=(item>>>4)&15,low=item&15;
                if(high<12)return high%6+1;
                if(low<12)return low%6+1;
            }
            throw new IllegalStateException("No unbiased die nibble available");
        }catch(NoSuchAlgorithmException e){
            throw new IllegalStateException("SHA-256 is unavailable",e);
        }
    }
}
