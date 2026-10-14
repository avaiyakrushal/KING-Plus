package com.kingplus.social;
public final class KingMeHomePartyGuard10008Test {
    static int count=0;
    static void expect(boolean expected,boolean actual,String context){
        if(expected!=actual)throw new AssertionError(context);count++;
    }
    public static void main(String[] args){
        expect(true, KingMeHomePartyGuard10008.mayCloseCurrentGame("active",90L,90L),"active same round");
        expect(false,KingMeHomePartyGuard10008.mayCloseCurrentGame("closed",90L,90L),"closed");
        expect(false,KingMeHomePartyGuard10008.mayCloseCurrentGame("finished",90L,90L),"finished");
        expect(false,KingMeHomePartyGuard10008.mayCloseCurrentGame("active",91L,90L),"newer round");
        expect(false,KingMeHomePartyGuard10008.mayCloseCurrentGame("active",89L,90L),"older round");
        expect(false,KingMeHomePartyGuard10008.mayCloseCurrentGame("active",null,90L),"missing round");
        expect(false,KingMeHomePartyGuard10008.mayCloseCurrentGame("active",0L,90L),"zero server round");
        expect(false,KingMeHomePartyGuard10008.mayCloseCurrentGame("active",90L,0L),"no observed game");
        expect(false,KingMeHomePartyGuard10008.mayCloseCurrentGame(null,90L,90L),"no status");
        expect(true, KingMeHomePartyGuard10008.callbackCurrent(3,3,"uid1","uid1",true),"active user/view");
        expect(false,KingMeHomePartyGuard10008.callbackCurrent(2,3,"uid1","uid1",true),"prior listener");
        expect(false,KingMeHomePartyGuard10008.callbackCurrent(3,3,"uid1","uid2",true),"account switched");
        expect(false,KingMeHomePartyGuard10008.callbackCurrent(3,3,"uid1","uid1",false),"tab switched");
        expect(false,KingMeHomePartyGuard10008.callbackCurrent(3,3,null,"uid1",true),"null origin");
        System.out.println("PASS KING 10.0.8 Me Home Party policy: "+count+" cases");
    }
}
