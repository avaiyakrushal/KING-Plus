package com.kingplus.social;

/** Pure-Java room transition gates; Android UI and network callbacks must obey them. */
public final class KingRoomLifecycle10006 {
    private int bannerEpoch;

    public int nextBanner() { return ++bannerEpoch; }
    public void invalidateBanners() { ++bannerEpoch; }
    public boolean ownsBanner(int token) { return token == bannerEpoch; }

    public static boolean acceptsRoomWrite(String expectedRoom, String currentRoom,
            String expectedUid, String currentUid, int expectedBinding,
            int currentBinding, boolean isActive) {
        return isActive && expectedRoom != null && expectedRoom.equals(currentRoom)
                && expectedUid != null && expectedUid.equals(currentUid)
                && expectedBinding == currentBinding;
    }
}
