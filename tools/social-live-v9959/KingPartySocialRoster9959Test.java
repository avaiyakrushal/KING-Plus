package com.kingplus.social;
import java.security.SecureRandom;
import java.util.*;
public final class KingPartySocialRoster9959Test {
 static void yes(boolean ok,String msg){if(!ok)throw new AssertionError(msg);}
 static void no(boolean ok,String msg){yes(!ok,msg);}
 public static void main(String[] args){
  yes(KingPartySocialRoster9959.isEligible("alice",true,"alice",true,true,"alice"),"real Ready");
  no(KingPartySocialRoster9959.isEligible("alice",true,"alice",false,true,"alice"),"unreadied");
  no(KingPartySocialRoster9959.isEligible("alice",true,"alice",null,true,"alice"),"missing ready flag");
  no(KingPartySocialRoster9959.isEligible("alice",true,"alice",true,false,""),"ghost member");
  no(KingPartySocialRoster9959.isEligible("alice",true,"bob",true,true,"alice"),"forged Ready");
  no(KingPartySocialRoster9959.isEligible("alice",true,"alice",true,true,"bob"),"forged member");
  no(KingPartySocialRoster9959.isEligible("",true,"",true,true,""),"missing UID");
  List<String> players=Arrays.asList("alice","bob","host");
  SecureRandom hostPick=new SecureRandom(){@Override public int nextInt(int bound){return bound-1;}};
  SecureRandom guestPick=new SecureRandom(){@Override public int nextInt(int bound){return 0;}};
  yes("host".equals(KingPartySocialRoster9959.pickHiddenRole(players,hostPick)),"Host role eligible");
  yes("alice".equals(KingPartySocialRoster9959.pickHiddenRole(players,guestPick)),"Guest role eligible");
  boolean failed=false;
  try{KingPartySocialRoster9959.pickHiddenRole(Collections.emptyList(),new SecureRandom());}
  catch(IllegalArgumentException expected){failed=true;}
  yes(failed,"empty active Party cannot pick a role");
  System.out.println("PASS social live Ready, ghost/forgery rejection, fair Host roles");
 }
}
