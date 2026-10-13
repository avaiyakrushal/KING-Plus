package com.kingplus.social;
public final class KingAgencyPolicy9982Test {
 private static void check(boolean v,String msg){if(!v)throw new AssertionError(msg);}
 public static void main(String[] args){
  check(KingAgencyPolicy9982.validUid("agentuid123"),"valid Firebase uid");
  check(!KingAgencyPolicy9982.validUid("abc"),"very short invalid uid");
  check(!KingAgencyPolicy9982.validUid("user/name"),"no slash in Firestore UID");
  check(!KingAgencyPolicy9982.validUid(" uidvalid"),"leading spaces rejected");
  check(KingAgencyPolicy9982.validPack(100)&&KingAgencyPolicy9982.validPack(600)&&KingAgencyPolicy9982.validPack(1300),"approved packs");
  check(!KingAgencyPolicy9982.validPack(0)&&!KingAgencyPolicy9982.validPack(-100)&&!KingAgencyPolicy9982.validPack(1000000),"no fabricated credits");
  check(!KingAgencyPolicy9982.canSubmit(false,true,false),"suspended parent invalidates all Agent rights");
  check(KingAgencyPolicy9982.canSubmit(false,true,true),"active Agent can submit with active parent");
  check(!KingAgencyPolicy9982.canSubmit(false,false,true),"unapproved regular user forbidden");
  check(KingAgencyPolicy9982.canManageAgents(false,true)&&!KingAgencyPolicy9982.canManageAgents(false,false),"only approved Agency manages Agents");
  check(!KingAgencyPolicy9982.couldCreditDiamondsFromClient(),"no unverified recharge mint");
  System.out.println("PASS Agency entitlement/pack policy with no client-side diamond minting");
 }
}
