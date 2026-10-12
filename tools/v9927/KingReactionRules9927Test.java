package com.kingplus.social;
public final class KingReactionRules9927Test {
  private static void yes(boolean okay,String msg){if(!okay)throw new AssertionError(msg);}
  private static void bad(long ms){boolean fail=false;try{KingReactionRules9927.score(ms);}catch(IllegalArgumentException x){fail=true;}yes(fail,"Invalid timestamp must fail: "+ms);}
  public static void main(String[] args){
    bad(-1);bad(0);bad(30001);
    yes(KingReactionRules9927.score(200)>KingReactionRules9927.score(400),"Fastest valid player wins");
    yes(KingReactionRules9927.score(500)==99500,"GO-to-tap elapsed scored");
    yes(KingReactionRules9927.FALSE_START==0,"False start is zero");
    yes(KingReactionRules9927.progress(5000)==100,"Progress capped");
    System.out.println("PASS reaction real latency, false start, elapsed score and upper bound");
  }
}