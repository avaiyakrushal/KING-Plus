package com.kingplus.social;

/** Durable identity and session guards for multi-phone Party presence. */
public final class KingRoomPresence967 {
    private KingRoomPresence967() {}

    public static boolean isActive(String requestedRoom, String requestedUid,
                                   int requestedGeneration, String actualRoom,
                                   String actualUid, int actualGeneration,
                                   boolean cloudRoom, boolean alive) {
        return alive && cloudRoom && requestedRoom != null
            && !requestedRoom.trim().isEmpty()
            && requestedUid != null && !requestedUid.isEmpty()
            && requestedGeneration > 0
            && requestedGeneration == actualGeneration
            && requestedRoom.equals(actualRoom)
            && requestedUid.equals(actualUid);
    }

    /** Never delete another phone's member record when both share a Firebase UID.
     * On legacy members without a session id, fall back to matching the exact device.
     */
    public static boolean canRemove(String storedSession, String leavingSession,
                                    String storedDevice, String leavingDevice) {
        if (storedSession != null && !storedSession.isEmpty()) {
            return leavingSession != null && !leavingSession.isEmpty()
                && storedSession.equals(leavingSession);
        }
        return storedDevice != null && !storedDevice.isEmpty()
            && leavingDevice != null && !leavingDevice.isEmpty()
            && storedDevice.equals(leavingDevice);
    }

    public static boolean isStale(long lastSeenMs, long nowMs, long ttlMs) {
        return lastSeenMs > 0 && nowMs >= lastSeenMs
            && ttlMs > 0 && (nowMs - lastSeenMs) > ttlMs;
    }
}
