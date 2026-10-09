package com.kingplus.social;

import java.util.Arrays;
import java.util.List;

public final class KingLudoTurn962Test {
    private static int checks = 0;
    private static void check(boolean actual, boolean expected, String label) {
        if (actual != expected) throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        checks++;
    }

    public static void main(String[] args) {
        List<String> two = Arrays.asList("host", "", "other", "");
        List<Boolean> twoReady = Arrays.asList(true, false, true, false);
        check(KingLudoTurn962.canStart("host","host","lobby",two,twoReady,2),true,"2p host all ready");
        check(KingLudoTurn962.canStart("other","host","lobby",two,twoReady,2),false,"2p nonhost cannot start");
        check(KingLudoTurn962.canStart("host","host","roll",two,twoReady,2),false,"cannot restart active game");
        check(KingLudoTurn962.canStart("host","host","lobby",two,Arrays.asList(true,false,false,false),2),false,"2p requires both ready");
        check(KingLudoTurn962.canStart("host","host","lobby",two,twoReady,4),false,"4p requires four");
        List<String> four = Arrays.asList("host","b","other","d");
        List<Boolean> fourReady = Arrays.asList(true,true,true,true);
        check(KingLudoTurn962.canStart("host","host","lobby",four,fourReady,4),true,"4p ready");
        check(KingLudoTurn962.canStart("host","host","lobby",four,Arrays.asList(true,true,true,false),4),false,"4p not all ready");

        long now=90_000L, last=30_000L;
        check(KingLudoTurn962.canSkip("other","roll",0,two,last,now),true,"skip after exactly 60s");
        check(KingLudoTurn962.canSkip("other","move",0,two,last,now-1),false,"59.999s not enough");
        check(KingLudoTurn962.canSkip("host","roll",0,two,last,now),false,"active player cannot skip own turn");
        check(KingLudoTurn962.canSkip("stranger","roll",0,two,last,now),false,"outsider cannot skip");
        check(KingLudoTurn962.canSkip("other","lobby",0,two,last,now),false,"no lobby skip");
        check(KingLudoTurn962.canSkip("other","roll",0,two,0,now),false,"no server timestamp");
        check(KingLudoTurn962.canSkip("other","roll",4,two,last,now),false,"invalid turn");
        check(KingLudoTurn962.canSkip("host","roll",2,two,last,now),true,"host may skip other's idle turn");
        check(KingLudoTurn962.canSkip("host","resolve",2,two,last,now),true,"stalled dice resolve can recover");
        check(KingLudoTurn962.canSkip("host","finished",2,two,last,now),false,"finished game cannot skip");
        System.out.println("KING Plus Ludo Turn v9.6.2: " + checks + " tests PASS");
    }
}
