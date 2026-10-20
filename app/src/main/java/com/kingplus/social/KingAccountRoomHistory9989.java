package com.kingplus.social;

/** Isolates room history on shared phones. Unowned legacy data is never assigned to an account. */
public final class KingAccountRoomHistory9989 {
    private KingAccountRoomHistory9989() {}
    public static String key(String kind,String uid) {
        if(!"favorite_rooms_940".equals(kind)&&!"recent_rooms_940".equals(kind))
            throw new IllegalArgumentException("Unknown room history kind");
        return kind+":account:"+(uid==null||uid.isEmpty()?"guest":"uid:"+uid);
    }
}
