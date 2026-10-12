package com.kingplus.social;
public final class KingNewVisualAndGame9932Test {
 private static void ok(boolean yes,String message){if(!yes)throw new AssertionError(message);}
 public static void main(String[] args){
   float p=-4.4f;
   for(int i=0;i<120;i++){float next=KingAvatarAnimation9932.smooth(p,4.4f);
     ok(next>=p && next<=4.4f,"Must move toward target without overshoot");p=next;}
   ok(Math.abs(p-4.4f)<.02f,"Walk interpolation converges");
   ok(KingAvatarAnimation9932.smooth(1f,1.01f)==1.01f,"Small movement snaps to target");
   ok(!KingAvatarAnimation9932.isMoving(0,0,0,0),"Still pose idle");
   ok(KingAvatarAnimation9932.isMoving(0,0,1,0),"Moved pose animates");
   ok(KingAvatarAnimation9932.lift(0f,0)>=0f && KingAvatarAnimation9932.lift(0f,0)<=.11f,"Foot never below floor");
   boolean rejected=false;
   try{KingAvatarAnimation9932.smooth(Float.NaN,1f);}catch(IllegalArgumentException expected){rejected=true;}
   ok(rejected,"Invalid pose must be rejected");
   ok(KingTapRace9932.score(50,7000)==0,"No partial scores");
   ok(KingTapRace9932.score(50,8000)==100,"Measured 8s race");
   ok(KingTapRace9932.score(-10,8000)==0,"No negative score");
   ok(KingTapRace9932.score(9999,9000)==1000,"Score bounded at 500 taps");
   System.out.println("PASS KING v9.9.32 smooth live avatar and real tap sprint rules");
 }
}
