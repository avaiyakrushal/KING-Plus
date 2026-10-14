package com.kingplus.social;
public final class KingMeHomePartyPolicy10010Test {
    private static int n;
    private static void eq(boolean expected,boolean actual,String name) {
        if(actual!=expected)throw new AssertionError(name);n++;
    }
    public static void main(String[] args) {
        eq(true,KingMeHomePartyPolicy10010.activeGuess(true,"guess","active",42),"active round");
        eq(false,KingMeHomePartyPolicy10010.activeGuess(false,"guess","active",42),"missing doc");
        eq(false,KingMeHomePartyPolicy10010.activeGuess(true,"vote","active",42),"different game");
        eq(false,KingMeHomePartyPolicy10010.activeGuess(true,"guess","closed",42),"closed game");
        eq(false,KingMeHomePartyPolicy10010.activeGuess(true,"guess","finished",42),"finished game");
        eq(false,KingMeHomePartyPolicy10010.activeGuess(true,"guess","active",0),"missing round");
        eq(true,KingMeHomePartyPolicy10010.sameGuessRound(true,"guess","active",42,42),"current round");
        eq(false,KingMeHomePartyPolicy10010.sameGuessRound(true,"guess","active",43,42),"new game");
        eq(false,KingMeHomePartyPolicy10010.sameGuessRound(true,"guess","active",42,0),"unseen round");
        eq(false,KingMeHomePartyPolicy10010.sameGuessRound(true,"guess",null,42,42),"unknown state");
        System.out.println("PASS KING v10.0.10 game round guards: "+n+" cases");
    }
}
