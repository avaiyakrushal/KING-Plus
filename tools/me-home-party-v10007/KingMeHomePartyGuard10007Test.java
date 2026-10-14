package com.kingplus.social;
public final class KingMeHomePartyGuard10007Test {
    private static int assertions=0;
    private static void ok(boolean actual,boolean expected,String name){
        if(actual!=expected)throw new AssertionError(name);assertions++;
    }
    public static void main(String[] args){
        ok(KingMeHomePartyGuard10007.visitorCountCurrent("A","A",2,2,true,true),true,"current count");
        ok(KingMeHomePartyGuard10007.visitorCountCurrent("A","A",1,2,true,true),false,"stale count");
        ok(KingMeHomePartyGuard10007.visitorCountCurrent("A","B",2,2,true,true),false,"account switch count");
        ok(KingMeHomePartyGuard10007.visitorCountCurrent("A","A",2,2,false,true),false,"not on profile");
        ok(KingMeHomePartyGuard10007.visitorCountCurrent("A","A",2,2,true,false),false,"old chip");
        ok(KingMeHomePartyGuard10007.momentCallbackCurrent("A","A",5,5,true,true),true,"active moment");
        ok(KingMeHomePartyGuard10007.momentCallbackCurrent("A","A",5,6,true,true),false,"changed tab");
        ok(KingMeHomePartyGuard10007.momentCallbackCurrent("A","B",5,5,true,true),false,"changed user");
        ok(KingMeHomePartyGuard10007.momentCallbackCurrent("A","A",5,5,false,true),false,"not moments tab");
        ok(KingMeHomePartyGuard10007.momentCallbackCurrent("A","A",5,5,true,false),false,"new host");
        ok(KingMeHomePartyGuard10007.gameIsActive("active",100),true,"active game");
        ok(KingMeHomePartyGuard10007.gameIsActive("closed",100),false,"closed game");
        ok(KingMeHomePartyGuard10007.gameIsActive("finished",100),false,"finished game");
        ok(KingMeHomePartyGuard10007.gameIsActive("active",0),false,"no round");
        ok(KingMeHomePartyGuard10007.gameIsActive(null,100),false,"no state");
        System.out.println("PASS KING v10.0.7 Me Home Party guards ("+assertions+" cases)");
    }
}
