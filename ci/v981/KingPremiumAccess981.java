package com.kingplus.social;

/** Never treat device-local TEST points as entitlement to paid VIP cosmetics. */
public final class KingPremiumAccess981 {
    // PIN the production server and independently verify recharge before enabling.
    // This build cannot grant paid VIP from an owner-unconfigured cloud endpoint.
    public static final boolean PAID_VIP_ACTIVE = false;
    private KingPremiumAccess981(){}
    public static boolean allowedFrame(String frame,int ordinaryLevel,int verifiedVip){
        if("Minimal Frame".equals(frame))return true;
        if("Music Frame".equals(frame))return ordinaryLevel>=5;
        if("Heart Frame".equals(frame))return ordinaryLevel>=10;
        if(!PAID_VIP_ACTIVE)return false;
        if("Royal Frame".equals(frame))return verifiedVip>=4;
        if("Galaxy Frame".equals(frame))return verifiedVip>=7;
        if("Crown Frame".equals(frame))return verifiedVip>=10;
        return false;
    }
    public static boolean allowedEffect(String effect,int ordinaryLevel,int verifiedVip){
        if("None".equals(effect))return true;
        if("Welcome Sparkle".equals(effect))return ordinaryLevel>=3;
        if(!PAID_VIP_ACTIVE)return false;
        if("Crown Drop".equals(effect))return verifiedVip>=3;
        if("Rose Shower".equals(effect))return verifiedVip>=5;
        if("Galaxy Portal".equals(effect))return verifiedVip>=7;
        if("Royal Arrival".equals(effect))return verifiedVip>=10;
        return false;
    }
    public static String paidVipMessage(String label){
        return "Premium "+label+" is locked until Diamond Recharge verification is active. TEST points cannot unlock it.";
    }
}
