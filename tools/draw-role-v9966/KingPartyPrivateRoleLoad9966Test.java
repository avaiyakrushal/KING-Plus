package com.kingplus.social;
public final class KingPartyPrivateRoleLoad9966Test {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 public static void main(String[]args){
   check(KingPartyPrivateRoleLoad9966.needsRequest(19,18,false,"","","draw"),
     "First real drawer fetch after Activity recreation");
   check(!KingPartyPrivateRoleLoad9966.needsRequest(19,19,false,"","","draw"),
     "Never start duplicate network fetch after failed/empty result");
   check(!KingPartyPrivateRoleLoad9966.needsRequest(19,18,true,"","","draw"),
     "Never start concurrent Firebase role requests");
   check(!KingPartyPrivateRoleLoad9966.needsRequest(19,18,false,"Drawer","TIGER","draw"),
     "Confirmed existing word needs no server retry");
   check(KingPartyPrivateRoleLoad9966.needsRequest(20,19,false,"","", "draw"),
     "New active round must fetch its own new private word");
   check(!KingPartyPrivateRoleLoad9966.needsRequest(0,-1,false,"","","draw"),
     "No private role request for missing game round");
   check(!KingPartyPrivateRoleLoad9966.needsRequest(19,18,false,"","", "coin"),
     "No private roles for spectator Coin game");
   check(KingPartyPrivateRoleLoad9966.needsRequest(19,18,false,"","", "spy"),
     "Spy private role can be recovered from server");
   check(!KingPartyPrivateRoleLoad9966.needsRequest(19,18,false,"Spy","", "spy"),
     "Spy deliberately has no secret and should NOT spam retried reads");
   check(KingPartyPrivateRoleLoad9966.needsRequest(19,18,false,"","", "werewolf"),
     "Werewolf needs role");
   check(!KingPartyPrivateRoleLoad9966.needsRequest(19,18,false,"Villager","", "werewolf"),
     "Villager role alone sufficient, no fetch loop");
   check(KingPartyPrivateRoleLoad9966.needsRequest(19,-1,false,"Role unavailable","", "draw"),
     "Explicit Retry resets prior attempt and allows fresh fetch");
   System.out.println("PASS actual Draw private word once-per-round recovery and manual retry; no Firebase render request loop");
 }
}