package com.kingplus.social;
public final class KingPartyConnection979Test {
  private static int n;
  private static void assertThat(boolean actual,boolean want,String label){
    if(actual!=want)throw new AssertionError(label);n++;
  }
  public static void main(String[] args){
    assertThat(KingPartyConnection979.isRetryable("UNAVAILABLE","Failed to get document because the client is offline."),true,"offline");
    assertThat(KingPartyConnection979.isRetryable("DEADLINE_EXCEEDED","timed out"),true,"timeout");
    assertThat(KingPartyConnection979.isRetryable(null,"The client is offline"),true,"SDK offline");
    assertThat(KingPartyConnection979.isRetryable("PERMISSION_DENIED","denied"),false,"never retry permission bypass");
    assertThat(KingPartyConnection979.isRetryable("NOT_FOUND","no room"),false,"room removed");
    assertThat(KingPartyConnection979.isRetryable(null,null),false,"unknown error");
    assertThat(KingPartyConnection979.canRejoin("room1","room1","uid1","uid1",4,4,true,true),true,"current room");
    assertThat(KingPartyConnection979.canRejoin("room1","room2","uid1","uid1",4,4,true,true),false,"different room");
    assertThat(KingPartyConnection979.canRejoin("room1","room1","uid1","uid2",4,4,true,true),false,"account changed");
    assertThat(KingPartyConnection979.canRejoin("room1","room1","uid1","uid1",4,5,true,true),false,"late callback");
    assertThat(KingPartyConnection979.canRejoin("room1","room1","uid1","uid1",4,4,false,true),false,"destroyed");
    assertThat(KingPartyConnection979.canRejoin("room1","room1","uid1","uid1",4,4,true,false),false,"lobby");
    assertThat(KingPartyConnection979.distinctMembers("alice","bob"),true,"two users");
    assertThat(KingPartyConnection979.distinctMembers("alice","alice"),false,"same account same member");
    assertThat(KingPartyConnection979.distinctMembers(null,"bob"),false,"no guest UID");
    assertThat(KingPartyConnection979.status(false,true,true).contains("Internet"),true,"transport issue");
    assertThat(KingPartyConnection979.status(true,false,true).contains("Sign in"),true,"sign-in issue");
    assertThat(KingPartyConnection979.status(true,true,false).contains("same KING"),true,"project mismatch");
    System.out.println("PASS KING Plus v9.7.9 Firestore recovery and two-phone safeguards: "+n+" tests");
  }
}
