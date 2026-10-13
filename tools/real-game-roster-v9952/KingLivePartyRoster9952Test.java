package com.kingplus.social;
public final class KingLivePartyRoster9952Test {
 private static void yes(boolean x,String m){if(!x)throw new AssertionError(m);}
 private static void no(boolean x,String m){if(x)throw new AssertionError(m);}
 public static void main(String[] args){
  yes(KingLivePartyRoster9952.isActiveReady("alice",true,"alice",true,"alice"),"real Ready Party member");
  no(KingLivePartyRoster9952.isActiveReady("alice",true,"alice",false,""),"ghost Ready after exit");
  no(KingLivePartyRoster9952.isActiveReady("alice",false,"",true,"alice"),"un-readied user");
  no(KingLivePartyRoster9952.isActiveReady("alice",true,"mallory",true,"alice"),"forged Ready");
  no(KingLivePartyRoster9952.isActiveReady("alice",true,"alice",true,"mallory"),"forged member");
  no(KingLivePartyRoster9952.isActiveReady("",true,"",true,""),"empty");
  System.out.println("PASS live Party roster gate, departed Ready players excluded");
 }
}
