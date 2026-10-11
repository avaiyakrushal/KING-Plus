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
 * Full anti-cheat gaming needs trusted server-side rules or approved Cloud
 * Function with verifiable random beacon (and provider/onboarding).
 */
public final class KingLudoDiceEntropy999 {
    private KingLudoDiceEntropy999(){}
    public static int dice(long seconds,int nanos,String roomCode,long revision){
        if(seconds<=0 || nanos<0 || nanos>=1_000_000_000
          ||roomCode==null||roomCode.isEmpty()||revision<0)
            throw new IllegalArgumentException("Invalid server dice entropy");
        final String payload="KING-PLUS-LUDO-FAIR-V1|"+seconds+"|"+nanos+
                "|"+roomCode+"|"+revision;
        try {
            MessageDigest digest=MessageDigest.getInstance("SHA-256");
            byte[] bytes=digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            // sample uniform unsigned 32-bit blocks; reject 4 values out of 2^32.
            final long max=0x1_0000_0000L;
            final long limit=max-(max%6);
            for(int i=0;i+4<=bytes.length;i+=4){
                long val=((bytes[i]&255L)<<24)
                        |((bytes[i+1]&255L)<<16)
                        |((bytes[i+2]&255L)<<8)
                        |(bytes[i+3]&255L);
                if(val<limit)return (int)(val%6)+1;
            }
            throw new IllegalStateException("No unbiased die block available");
        }catch(NoSuchAlgorithmException e){
            throw new IllegalStateException("SHA-256 is unavailable",e);
        }
    }
}
