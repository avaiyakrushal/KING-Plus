package com.kingplus.social;
public final class KingPartyMoveSubscription9979Test {
 static void ok(boolean b,String msg){if(!b)throw new AssertionError(msg);}
 public static void main(String[] args){
  ok(KingPartyMoveSubscription9979.needsNewListener(false,-1L,"",800,"dice"),
      "first party join starts stream");
  ok(!KingPartyMoveSubscription9979.needsNewListener(true,800,"dice",800,"dice"),
      "repeat same server state/metadata must NOT drop ready moves");
  ok(KingPartyMoveSubscription9979.needsNewListener(true,800,"dice",801,"dice"),
      "new rematch must stream new round");
  ok(KingPartyMoveSubscription9979.needsNewListener(true,800,"dice",800,"wheel"),
      "switch game type must stream new game");
  ok(KingPartyMoveSubscription9979.needsNewListener(false,800,"dice",800,"dice"),
      "member rejoin must resubscribe even to same match");
  ok(KingPartyMoveSubscription9979.needsNewListener(true,800,"dice",0,""),
      "room exit must clear move listener");
  ok(!KingPartyMoveSubscription9979.needsNewListener(true,900,"slot",900,"slot"),
      "receiver must preserve live turn data under repeated state timestamps");
  System.out.println("PASS 7 real Party two-phone listener lifecycle assertions");
 }
}
