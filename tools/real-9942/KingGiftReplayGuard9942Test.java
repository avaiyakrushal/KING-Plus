package com.kingplus.social;

public final class KingGiftReplayGuard9942Test {
 private static void good(boolean value,String name){if(!value)throw new AssertionError(name);}
 private static void bad(boolean value,String name){if(value)throw new AssertionError(name);}
 public static void main(String[] args){
   long now=1000000L;
   good(KingGiftReplayGuard9942.allow(now,now-2000,"alice","bob",false,true),"realtime recipient");
   good(KingGiftReplayGuard9942.allow(now,now-105000,"alice","bob",true,true),"background catch-up 105s");
   good(KingGiftReplayGuard9942.allow(now,now-120000,"alice","bob",true,true),"at maximum 120s");
   bad(KingGiftReplayGuard9942.allow(now,now-120001,"alice","bob",true,true),"too old to reanimate");
   bad(KingGiftReplayGuard9942.allow(now,now+11000,"alice","bob",true,true),"future event");
   bad(KingGiftReplayGuard9942.allow(now,now-500,"alice","alice",false,true),"own gift echo suppressed");
   bad(KingGiftReplayGuard9942.allow(now,now-500,"alice","charlie",true,false),"no historic spectator replay");
   good(KingGiftReplayGuard9942.allow(now,now-500,"alice","charlie",false,false),"new realtime Party spectator");
   bad(KingGiftReplayGuard9942.allow(now,now-500,null,"bob",false,true),"missing sender");
   bad(KingGiftReplayGuard9942.allow(now,0,"alice","bob",true,true),"missing server time");
   System.out.println("PASS Gift realtime, background/rejoin replay, 120s TTL, spectator and own-echo suppression");
 }
}
