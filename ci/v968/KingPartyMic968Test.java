package com.kingplus.social;
public final class KingPartyMic968Test {
    private static int checks;
    private static void eq(boolean v,boolean want,String label){
        if(v!=want)throw new AssertionError(label);checks++;
    }
    public static void main(String[] args){
        eq(KingPartyMic968.canToggle("r","alice","alice",2,2,true,true,false,false),true,"own seat");
        eq(KingPartyMic968.canToggle("r","alice","bob",2,2,true,true,false,false),false,"different owner");
        eq(KingPartyMic968.canToggle("r","alice","alice",2,3,true,true,false,false),false,"stale seat");
        eq(KingPartyMic968.canToggle("r","alice","alice",0,0,true,true,false,false),false,"audience");
        eq(KingPartyMic968.canToggle(null,"alice","alice",2,2,true,true,false,false),false,"null room");
        eq(KingPartyMic968.canToggle("r","alice","alice",2,2,false,true,false,false),false,"offline");
        eq(KingPartyMic968.canToggle("r","alice","alice",2,2,true,false,false,false),false,"destroyed");
        eq(KingPartyMic968.canToggle("r","alice","alice",2,2,true,true,true,false),false,"mute all");
        eq(KingPartyMic968.canToggle("r","alice","alice",2,2,true,true,true,true),true,"moderator override");
        eq(KingPartyMic968.validRequest("r","alice",1,12,true,true),true,"request first seat");
        eq(KingPartyMic968.validRequest("r","alice",12,12,true,true),true,"request last seat");
        eq(KingPartyMic968.validRequest("r","alice",13,12,true,true),false,"too high");
        eq(KingPartyMic968.validRequest("r","alice",0,12,true,true),false,"invalid no");
        eq(KingPartyMic968.validRequest("r","alice",1,12,false,true),false,"offline request");
        eq(KingPartyMic968.validRequest(null,"alice",1,12,true,true),false,"null ID request");
        eq(KingPartyMic968.requestId("alice").equals("alice"),true,"Firestore rules member-owned request ID");
        eq(KingPartyMic968.requestId(null).isEmpty(),true,"reject null UID");
        System.out.println("KING Plus v9.6.8 Party Mic/Seat: "+checks+" tests PASS");
    }
}