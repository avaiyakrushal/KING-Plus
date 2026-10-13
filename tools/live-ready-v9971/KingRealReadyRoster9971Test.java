package com.kingplus.social;
public final class KingRealReadyRoster9971Test {
 static void ok(boolean x,String m){if(!x)throw new AssertionError(m);}
 static void no(boolean x,String m){ok(!x,m);}
 public static void main(String[]a){
  ok(KingRealReadyRoster9971.realReady("alice","alice",true,true),"live opted Ready");
  no(KingRealReadyRoster9971.realReady("alice","alice",false,true),"false-ready must be hidden");
  no(KingRealReadyRoster9971.realReady("alice","alice",null,true),"missing consent");
  no(KingRealReadyRoster9971.realReady("eve","eve",true,false),"departed ghost Ready must be hidden");
  no(KingRealReadyRoster9971.realReady("alice","eve",true,true),"spoofed document UID");
  no(KingRealReadyRoster9971.realReady("","",true,true),"empty UID not Ready");
  no(KingRealReadyRoster9971.realReady(null,null,true,true),"null UID not Ready");
  ok(KingRealReadyRoster9971.realReady("bob","bob",true,true),"separate real Party player can Ready");
  System.out.println("PASS real Party Ready: current signed-in member + genuine Ready true only");
 }
}