package com.kingplus.social;

/** Session-safe Party Mic and Seat validation. All trust-sensitive writes
 * still require authoritative Firebase Security Rules and transactions. */
public final class KingPartyMic968 {
    private KingPartyMic968() {}
    public static boolean canToggle(String room,String uid,String seatUid,int seat,
            int expectedSeat,boolean connected,boolean alive,boolean hostMuted,boolean moderator) {
        return connected && alive && room!=null && !room.trim().isEmpty()
            && uid!=null && !uid.isEmpty() && seat>0 && seat==expectedSeat
            && uid.equals(seatUid) && (!hostMuted || moderator);
    }
    public static boolean validRequest(String room,String uid,int seat,int maxSeats,
            boolean connected,boolean alive) {
        return connected && alive && room!=null && !room.trim().isEmpty()
            && uid!=null && !uid.isEmpty() && seat>=1 && seat<=maxSeats && maxSeats<=12;
    }
    public static String requestId(String uid){return uid==null?"":uid;}
}