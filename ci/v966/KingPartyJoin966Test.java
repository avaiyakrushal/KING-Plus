package com.kingplus.social;

public final class KingPartyJoin966Test {
    private static int checks;
    private static void eq(boolean result, boolean expected, String label) {
        if(result != expected) throw new AssertionError(label + " expected " + expected + " got " + result);
        checks++;
    }

    public static void main(String[] args) {
        eq(KingPartyJoin966.current("roomA","uidA",8,"roomA","uidA",8,true,true),true,"valid room join");
        eq(KingPartyJoin966.current("roomA","uidA",8,null,"uidA",8,false,true),false,"lobby null room");
        eq(KingPartyJoin966.current("roomA","uidA",8,null,"uidA",8,true,true),false,"null room even when cloud flag stale");
        eq(KingPartyJoin966.current("roomA","uidA",8,"roomB","uidA",8,true,true),false,"different room");
        eq(KingPartyJoin966.current("roomA","uidA",8,"roomA","uidB",8,true,true),false,"different account");
        eq(KingPartyJoin966.current("roomA","uidA",8,"roomA","uidA",9,true,true),false,"new join invalidates old callback");
        eq(KingPartyJoin966.current("roomA","uidA",8,"roomA","uidA",8,false,true),false,"local preview");
        eq(KingPartyJoin966.current("roomA","uidA",8,"roomA","uidA",8,true,false),false,"destroyed activity");
        eq(KingPartyJoin966.current(null,"uidA",8,"roomA","uidA",8,true,true),false,"null requested room");
        eq(KingPartyJoin966.current("","uidA",8,"roomA","uidA",8,true,true),false,"empty requested room");
        eq(KingPartyJoin966.current("  ","uidA",8,"roomA","uidA",8,true,true),false,"blank requested room");
        eq(KingPartyJoin966.current("roomA",null,8,"roomA","uidA",8,true,true),false,"null requested user");
        eq(KingPartyJoin966.current("roomA","",8,"roomA","uidA",8,true,true),false,"empty requested user");
        eq(KingPartyJoin966.current("roomA","uidA",0,"roomA","uidA",0,true,true),false,"invalid request token");
        eq(KingPartyJoin966.current("roomA","uidA",8,"roomA","uidA",8,true,true),true,"duplicate read callback belongs to current session");
        System.out.println("KING Plus Party join v9.6.6: " + checks + " tests PASS");
    }
}
