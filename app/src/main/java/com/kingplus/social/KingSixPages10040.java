package com.kingplus.social;
/** Small, Android-free guards for six-page account and live-data reliability. */
public final class KingSixPages10040 {
    private KingSixPages10040() {}
    public static boolean sameAccount(String expected, String current) {
        return expected != null && !expected.isEmpty() && expected.equals(current);
    }
    public static boolean safePeer(String owner, String peer) {
        return sameAccount(owner, owner) && peer != null && !peer.isEmpty()
            && peer.equals(peer.trim()) && !peer.equals(owner) && peer.indexOf('/') < 0;
    }
    public static boolean canEnterRoom(boolean exists, Boolean closed, String roomOwner, String owner) {
        return exists && !Boolean.TRUE.equals(closed) && sameAccount(roomOwner, owner);
    }
    public static String safePreview(String text, int maxChars) {
        if(text == null) return "";
        String clean = text.replace('\r',' ').replace('\n',' ').trim();
        int limit=Math.max(0, Math.min(2000,maxChars));
        return clean.length() <= limit ? clean : clean.substring(0,limit)+"…";
    }
    public static boolean currentGameRound(int expected, int active) {return expected==active;}
}
