package com.kingplus.social;

/** Prevents callbacks from an old Party room/session from writing to a new one. */
public final class KingPartyJoin966 {
    private KingPartyJoin966() {}

    public static boolean current(
            String requestedRoom, String requestedUid, int requestToken,
            String activeRoom, String activeUid, int activeToken,
            boolean cloudRoom, boolean activityAlive) {
        return activityAlive && cloudRoom
            && requestedRoom != null && !requestedRoom.trim().isEmpty()
            && requestedUid != null && !requestedUid.isEmpty()
            && requestToken > 0 && requestToken == activeToken
            && requestedRoom.equals(activeRoom)
            && requestedUid.equals(activeUid);
    }
}
