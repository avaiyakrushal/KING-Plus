package com.kingplus.social;
public final class RoomInviteParserSelfTest {
    private static void check(boolean ok,String msg){if(!ok)throw new AssertionError(msg);}
    public static void main(String[] args){
        String id="FJ8aC3zYb49aBcD02468";
        check(KingRoomInvite959.parse(KingRoomInvite959.link(id)).equals(id),"link roundtrip");
        check(KingRoomInvite959.parse("Join Room\nKINGROOM:"+id+"\nHello").equals(id),"message parsing");
        check(KingRoomInvite959.parse("Room ID: "+id).equals(id),"room id label");
        check(KingRoomInvite959.parse(id).equals(id),"raw id parsing");
        check(KingRoomInvite959.parse("kingplus://party/"+id+"/evil").isEmpty(),"reject path suffix");
        check(KingRoomInvite959.parse("kingplus://party/../../x").isEmpty(),"reject traversal");
        check(KingRoomInvite959.parse("KINGROOM:abc").isEmpty(),"reject short id");
        check(KingRoomInvite959.link("../../private").isEmpty(),"reject invalid outbound link");
        check(KingRoomInvite959.parse(null).isEmpty(),"null");
        System.out.println("9 KING Plus room invite parser checks passed.");
    }
}
