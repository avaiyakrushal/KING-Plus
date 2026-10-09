package com.kingplus.social;

/** Verified-chat recipient, family membership and non-cash VIP guardrails. */
public final class KingFamilyChat972 {
    private KingFamilyChat972() {}
    public static boolean validFamilyCode(String code){
        return code!=null && code.matches("[A-Z0-9]{6}");
    }
    public static boolean validMessage(String message){
        return message!=null&&!message.trim().isEmpty()
            && message.trim().length()<=1000;
    }
    public static boolean validPeer(String ownUid,String otherUid){
        return ownUid!=null&&!ownUid.isEmpty()
            && otherUid!=null&&otherUid.matches("[A-Za-z0-9_-]{18,128}")
            && !ownUid.equals(otherUid);
    }
    public static boolean validDirectGift(String ownUid,String otherUid,
                                          boolean cloud,int unit,int qty,long total){
        return cloud&&validPeer(ownUid,otherUid)
            && unit>0&&qty>=1&&qty<=99&&total==(long)unit*qty
            && total<=100000;
    }
}
