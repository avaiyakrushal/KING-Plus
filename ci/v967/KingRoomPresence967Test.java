package com.kingplus.social;

public final class KingRoomPresence967Test {
    private static int passed;
    private static void assertEq(boolean v, boolean expected, String name) {
        if (v != expected) throw new AssertionError(name+" expected="+expected+" got="+v);
        passed++;
    }
    public static void main(String[] args) {
        assertEq(KingRoomPresence967.isActive("r1","a",4,"r1","a",4,true,true),true,"same room and UID");
        assertEq(KingRoomPresence967.isActive("r1","a",4,null,"a",4,false,true),false,"back to lobby");
        assertEq(KingRoomPresence967.isActive("r1","a",4,"r2","a",4,true,true),false,"switched room");
        assertEq(KingRoomPresence967.isActive("r1","a",4,"r1","b",4,true,true),false,"switched Firebase user");
        assertEq(KingRoomPresence967.isActive("r1","a",4,"r1","a",5,true,true),false,"new presence generation");
        assertEq(KingRoomPresence967.isActive("r1","a",4,"r1","a",4,false,true),false,"offline Party");
        assertEq(KingRoomPresence967.isActive("r1","a",4,"r1","a",4,true,false),false,"Activity destroyed");
        assertEq(KingRoomPresence967.isActive(null,"a",4,"r1","a",4,true,true),false,"null room");
        assertEq(KingRoomPresence967.isActive(" ","a",4,"r1","a",4,true,true),false,"blank room");
        assertEq(KingRoomPresence967.isActive("r1",null,4,"r1","a",4,true,true),false,"null user");
        assertEq(KingRoomPresence967.isActive("r1","a",0,"r1","a",0,true,true),false,"invalid generation");
        assertEq(KingRoomPresence967.canRemove("sessionA","sessionA","deviceA","deviceA"),true,"same room session");
        assertEq(KingRoomPresence967.canRemove("sessionB","sessionA","deviceA","deviceA"),false,"newer same-device session");
        assertEq(KingRoomPresence967.canRemove("sessionB","sessionA","deviceB","deviceA"),false,"different session and device");
        assertEq(KingRoomPresence967.canRemove("sessionA",null,"deviceA","deviceA"),false,"cannot delete known session using missing token");
        assertEq(KingRoomPresence967.canRemove(null,"sessionA","deviceA","deviceA"),true,"legacy matching device");
        assertEq(KingRoomPresence967.canRemove(null,"sessionA","deviceB","deviceA"),false,"legacy different device");
        assertEq(KingRoomPresence967.canRemove(null,"sessionA",null,"deviceA"),false,"unknown legacy device");
        assertEq(KingRoomPresence967.isStale(1000,301001,300000),true,"older than 5min");
        assertEq(KingRoomPresence967.isStale(1000,301000,300000),false,"exactly 5min");
        assertEq(KingRoomPresence967.isStale(1000,2000,300000),false,"recent heartbeat");
        assertEq(KingRoomPresence967.isStale(3000,2000,300000),false,"clock skew or future timestamp");
        assertEq(KingRoomPresence967.isStale(0,3000000,300000),false,"no timestamp");
        System.out.println("KING Plus v9.6.7 Presence: "+passed+" tests PASS");
    }
}
