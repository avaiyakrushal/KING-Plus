package com.kingplus.social;
public final class KingMeHomePartyGuard10009Test {
    private static int count;
    private static void expect(boolean wanted,boolean got,String label){if(wanted!=got)throw new AssertionError(label);count++;}
    private static void round(long want,long proposed,long old,String label){if(KingMeHomePartyGuard10009.nextGameRound(proposed,old)!=want)throw new AssertionError(label);count++;}
    public static void main(String[] args){
        expect(true,KingMeHomePartyGuard10009.isFamilyCodeCollision("KING_FAMILY_CODE_COLLISION_10009"),"collision retry");
        expect(false,KingMeHomePartyGuard10009.isFamilyCodeCollision("PERMISSION_DENIED"),"permission denied not a collision");
        expect(false,KingMeHomePartyGuard10009.isFamilyCodeCollision(null),"missing exception");
        expect(true,KingMeHomePartyGuard10009.canPublishGame(false,null,0,0),"empty game");
        expect(false,KingMeHomePartyGuard10009.canPublishGame(false,null,0,12),"stale deleted game");
        expect(true,KingMeHomePartyGuard10009.canPublishGame(true,"closed",123,0),"closed game replaced");
        expect(true,KingMeHomePartyGuard10009.canPublishGame(true,"finished",123,0),"finished game replaced");
        expect(true,KingMeHomePartyGuard10009.canPublishGame(true,"active",123,123),"same observed round");
        expect(false,KingMeHomePartyGuard10009.canPublishGame(true,"active",124,123),"new active game protected");
        expect(false,KingMeHomePartyGuard10009.canPublishGame(true,"active",123,0),"cannot overwrite without seeing active game");
        expect(false,KingMeHomePartyGuard10009.canPublishGame(true,"active",0,0),"malformed active round");
        round(200,200,100,"normal wall clock");
        round(101,100,100,"same tick");
        round(110,90,109,"clock skew");
        round(1,0,0,"zero proposed time");
        System.out.println("PASS KING v10.0.9 Family and game start guards ("+count+" cases)");
    }
}
