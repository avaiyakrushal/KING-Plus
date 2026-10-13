package com.kingplus.social;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
public final class KingPartyHiddenWord9965Test {
 static void check(boolean v,String msg){if(!v)throw new AssertionError(msg);}
 static SecureRandom at(final int item){
   return new SecureRandom(){
     @Override public int nextInt(int bound){
       if(bound!=12||item<0||item>=bound)
         throw new AssertionError("Expected known 12-word private catalog");
       return item;
     }
   };
 }
 public static void main(String[]args){
   check("Airport".equals(KingPartyHiddenWord9965.spy(at(0))),"private Spy location");
   check("Temple".equals(KingPartyHiddenWord9965.spy(at(11))),"last Spy location");
   check("TIGER".equals(KingPartyHiddenWord9965.draw(at(0))),"secret draw word");
   check("STAR".equals(KingPartyHiddenWord9965.draw(at(11))),"last draw word");
   boolean rejected=false;
   try{KingPartyHiddenWord9965.spy(null);}catch(IllegalArgumentException ok){rejected=true;}
   check(rejected,"No deterministic/null RNG fallback");
   SecureRandom rng=new SecureRandom();
   Set<String> spy=new HashSet<>(),draw=new HashSet<>();
   for(int i=0;i<240;i++){
     spy.add(KingPartyHiddenWord9965.spy(rng));
     draw.add(KingPartyHiddenWord9965.draw(rng));
   }
   check(spy.size()>1&&draw.size()>1,
       "Actual each new round must have randomly sampled private secret");
   System.out.println("PASS secure generated Spy and Draw secrets; no public roomId/roundId hash");
 }
}