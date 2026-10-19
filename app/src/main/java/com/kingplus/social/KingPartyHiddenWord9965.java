package com.kingplus.social;
import java.security.SecureRandom;
/** Generate private server-readable role invites; never derive a secret
 * from public roomId, roundId or a predictable hash.
 * Honest-client secrecy only: trusted backend needed for high-value games.
 */
public final class KingPartyHiddenWord9965 {
 private KingPartyHiddenWord9965(){}
 private static final String[] SPY={
   "Airport","Beach","Hospital","School","Cinema","Hotel",
   "Market","Train Station","Restaurant","Zoo","Office","Temple"};
 private static final String[] DRAW={
   "TIGER","CROWN","DIAMOND","ROCKET","HEART","CASTLE",
   "FLOWER","MOON","PHONE","LUDO","LION","STAR"};
 public static String spy(SecureRandom rng){return select(SPY,rng);}
 public static String draw(SecureRandom rng){return select(DRAW,rng);}
 private static String select(String[] choices,SecureRandom rng){
   if(rng==null)throw new IllegalArgumentException("Secure random required");
   return choices[rng.nextInt(choices.length)];
 }
}
