package com.kingplus.social;
public final class KingPartyJoinTimeout977Test {
    private static int count;
    private static void yes(boolean x,String m){if(!x)throw new AssertionError(m);count++;}
    private static void no(boolean x,String m){yes(!x,m);}
    public static void main(String[] args){
        yes(KingPartyJoinTimeout977.JOIN_TIMEOUT_MS>=15000,"time for normal mobile latency");
        yes(KingPartyJoinTimeout977.waiting("r1","r1","u1","u1",3,3,true,false),"active wait");
        no(KingPartyJoinTimeout977.waiting("r1","r2","u1","u1",3,3,true,false),"changed room");
        no(KingPartyJoinTimeout977.waiting("r1","r1","u1","u2",3,3,true,false),"account switched");
        no(KingPartyJoinTimeout977.waiting("r1","r1","u1","u1",2,3,true,false),"old timeout");
        no(KingPartyJoinTimeout977.waiting("r1","r1","u1","u1",3,3,false,false),"Activity closed");
        no(KingPartyJoinTimeout977.waiting("r1","r1","u1","u1",3,3,true,true),"join completed");
        no(KingPartyJoinTimeout977.waiting(null,"r1","u1","u1",3,3,true,false),"null room");
        no(KingPartyJoinTimeout977.waiting("r1","r1",null,"u1",3,3,true,false),"null user");
        no(KingPartyJoinTimeout977.expired(1000,18999),"not quite elapsed");
        yes(KingPartyJoinTimeout977.expired(1000,19000),"watchdog expiry");
        no(KingPartyJoinTimeout977.expired(0,20000),"invalid initial timestamp");
        System.out.println("KING Plus v9.7.7 Party Join timeout: "+count+" guards PASS");
    }
}
