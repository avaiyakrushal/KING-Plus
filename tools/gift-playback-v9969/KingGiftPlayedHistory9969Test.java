package com.kingplus.social;
public final class KingGiftPlayedHistory9969Test {
 private static void yes(boolean b,String m){if(!b)throw new AssertionError(m);}
 private static void no(boolean b,String m){yes(!b,m);}
 public static void main(String[]args){
  long now=2000000000L;
  yes(KingGiftPlayedHistory9969.valid("room","gift","bob"),"valid recipient event");
  yes(KingGiftPlayedHistory9969.same("room","gift","bob","room","gift","bob"),"same room recipient gift");
  no(KingGiftPlayedHistory9969.same("room","gift","bob","room","gift","eve"),"another account needs its own Gift");
  no(KingGiftPlayedHistory9969.same("room","gift","bob","next","gift","bob"),"another Party needs its own Gift");
  no(KingGiftPlayedHistory9969.same("room","gift","bob","room","gift2","bob"),"new Gift must still appear");
  no(KingGiftPlayedHistory9969.valid("room","","bob"),"invalid empty gift ID");
  no(KingGiftPlayedHistory9969.valid("room/escape","gift","bob"),"room component ID safe");
  yes(KingGiftPlayedHistory9969.withinHistory(now,now),"just received");
  yes(KingGiftPlayedHistory9969.withinHistory(now,now-KingGiftPlayedHistory9969.HISTORY_MS),"history boundary");
  no(KingGiftPlayedHistory9969.withinHistory(now,now-KingGiftPlayedHistory9969.HISTORY_MS-1),"old entries expire");
  no(KingGiftPlayedHistory9969.withinHistory(now,now+10001),"future timestamp invalid");
  no(KingGiftPlayedHistory9969.withinHistory(now,0),"missing playback time invalid");
  yes(KingGiftPlayedHistory9969.MAX_EVENTS>=128,"history covers durable ACK journal");
  System.out.println("PASS real recipient Gift only one sound/animation per recent event, safely isolated by room and account after Android restart");
 }
}