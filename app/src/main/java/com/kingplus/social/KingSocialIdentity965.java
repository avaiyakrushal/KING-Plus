package com.kingplus.social;

/** Single stable format for published KING ID and Firebase follow document IDs. */
public final class KingSocialIdentity965 {
    private KingSocialIdentity965() {}
    public static String publicId(String firebaseUid){
        if(firebaseUid==null||firebaseUid.isEmpty())return "000000";
        long value=Integer.toUnsignedLong(firebaseUid.hashCode());
        return String.valueOf((value%900000L)+100000L);
    }
    public static String followId(String followerUid,String targetUid){
        if(followerUid==null||followerUid.isEmpty()||targetUid==null||targetUid.isEmpty())return "";
        return followerUid+"__"+targetUid;
    }
    public static String legacyFollowId(String followerUid,String targetUid){
        if(followerUid==null||followerUid.isEmpty()||targetUid==null||targetUid.isEmpty())return "";
        return followerUid+"_"+targetUid;
    }
    public static boolean validMember(String authUid,String memberUid){
        return authUid!=null&&!authUid.isEmpty()&&authUid.equals(memberUid);
    }
}