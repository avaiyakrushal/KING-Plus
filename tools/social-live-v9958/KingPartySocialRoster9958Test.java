package com.kingplus.social;
import java.util.*;
public final class KingPartySocialRoster9958Test{
 static void yes(boolean x,String m){if(!x)throw new AssertionError(m);}
 static void no(boolean x,String m){yes(!x,m);}
 public static void main(String[] args){
  yes(KingPartySocialRoster9958.isLive("alice",true,"alice",true,"alice"),"real player");
  no(KingPartySocialRoster9958.isLive("alice",true,"alice",false,""),"ghost ready");
  no(KingPartySocialRoster9958.isLive("alice",false,"",true,"alice"),"removed ready");
  no(KingPartySocialRoster9958.isLive("alice",true,"bob",true,"alice"),"forged ready");
  no(KingPartySocialRoster9958.isLive("alice",true,"alice",true,"bob"),"forged member");
  no(KingPartySocialRoster9958.isLive("",true,"",true,""),"empty");
  yes(KingPartySocialRoster9958.valid("werewolf",Arrays.asList("a","b","c","d")),"four Werewolf");
  no(KingPartySocialRoster9958.valid("werewolf",Arrays.asList("a","b","c")),"three Werewolf invalid");
  yes(KingPartySocialRoster9958.valid("spy",Arrays.asList("a","b","c")),"three Spy");
  no(KingPartySocialRoster9958.valid("spy",Arrays.asList("a","b")),"two Spy invalid");
  yes(KingPartySocialRoster9958.valid("draw",Arrays.asList("a","b")),"two Draw");
  no(KingPartySocialRoster9958.valid("draw",Arrays.asList("a","a")),"no duplicate");
  no(KingPartySocialRoster9958.valid("rps",Arrays.asList("a","b","c")),"unknown");
  System.out.println("PASS live Werewolf Spy Draw roster, ghost Ready and forged identity rejected");
 }
}