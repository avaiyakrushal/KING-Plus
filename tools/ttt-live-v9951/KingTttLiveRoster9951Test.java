package com.kingplus.social;
public final class KingTttLiveRoster9951Test {
  static void yes(boolean x,String m){if(!x)throw new AssertionError(m);}
  static void no(boolean x,String m){if(x)throw new AssertionError(m);}
  public static void main(String[] args){
    yes(KingTttLiveRoster9951.valid("alice",true,"alice",true,"alice"),"genuine live ready player");
    no(KingTttLiveRoster9951.valid("alice",true,"alice",false,""),"left room ghost READY");
    no(KingTttLiveRoster9951.valid("alice",false,"",true,"alice"),"unreadied player");
    no(KingTttLiveRoster9951.valid("alice",true,"eve",true,"alice"),"forged Ready user");
    no(KingTttLiveRoster9951.valid("alice",true,"alice",true,"eve"),"forged member identity");
    no(KingTttLiveRoster9951.valid("",true,"",true,""),"empty UID rejected");
    System.out.println("PASS live TTT Ready+Member gate and removed-user rejection");
  }
}
