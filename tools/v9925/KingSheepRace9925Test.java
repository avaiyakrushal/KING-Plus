package com.kingplus.social;
public class KingSheepRace9925Test {
  private static void yes(boolean v,String s){if(!v)throw new AssertionError(s);}
  public static void main(String[] args){
    yes(KingSheepRace9925.nextTap(0)==1,"one tap counts");
    yes(KingSheepRace9925.nextTap(200)==200,"no fake unlimited score");
    yes(KingSheepRace9925.progress(12)==12,"progress is measured, not random");
    yes(KingSheepRace9925.score(43,8000)==43,"same true score for same taps");
    yes(KingSheepRace9925.score(43,9000)==43,"delayed UI does not grant bonus");
    boolean forbidden=false;try{KingSheepRace9925.score(43,7500);}catch(IllegalArgumentException e){forbidden=true;}
    yes(forbidden,"cannot submit 7.5s as a full eight-second sprint");
    System.out.println("PASS sheep genuine taps, anti-fake timer guard and score bounds");
  }
}