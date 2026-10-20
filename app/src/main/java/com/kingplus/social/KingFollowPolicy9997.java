package com.kingplus.social;

/** Pure input and state policy shared by Me, Home and Party follow flows. */
public final class KingFollowPolicy9997 {
    private KingFollowPolicy9997(){}
    public static String key(String follower, String target) {
        if (follower==null||target==null||follower.trim().isEmpty()||target.trim().isEmpty()
            ||follower.contains("/")||target.contains("/")||follower.equals(target)) return null;
        return follower+"__"+target;
    }
    public static boolean nextFollowing(boolean alreadyFollowing) {return !alreadyFollowing;}
}
