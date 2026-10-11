package com.kingplus.social;

/** Original KING Plus action/live emoji access map. No paid VIP entitlement spoofing.
 * 0..49 original, 50..99 action and celebration FREE, 100..109 level cosmetics,
 * 110..119 VIP previews only until verified billing/server entitlement is deployed.
 */
public final class KingEmojiAccess993 {
    private KingEmojiAccess993(){}
    public static final int CATALOG_SIZE=120;
    public static int requiredLevel(int id){
        if(id<100||id>109)return 0;
        final int[] req={3,5,7,10,12,15,18,20,25,30};
        return req[id-100];
    }
    public static int requiredVip(int id){
        if(id<110||id>119)return 0;
        final int[] req={1,1,2,3,4,5,6,7,8,10};
        return req[id-110];
    }
    public static boolean canSend(int id,int ordinaryLevel,int verifiedVip){
        if(id<0||id>=CATALOG_SIZE)return false;
        if(requiredVip(id)>0){
            return KingPremiumAccess981.PAID_VIP_ACTIVE &&
                verifiedVip>=requiredVip(id);
        }
        return Math.max(0,ordinaryLevel)>=requiredLevel(id);
    }
    public static boolean isVipPreview(int id){
        return requiredVip(id)>0 && !KingPremiumAccess981.PAID_VIP_ACTIVE;
    }
    public static String lockMessage(int id){
        if(id<0||id>=CATALOG_SIZE)return "Emoji not available";
        if(requiredVip(id)>0)
            return "VIP "+requiredVip(id)+" preview only: verified Diamond/VIP billing is not enabled. No payment or unlock is claimed.";
        if(requiredLevel(id)>0)return "Unlock this action emoji at level "+requiredLevel(id)+".";
        return "FREE";
    }
    /** Groups are capped to 20-30 preview views to avoid low-memory process exits. */
    public static int[] range(int start,int end){
        int lo=Math.max(0,start),hi=Math.min(CATALOG_SIZE,end);
        int[] arr=new int[Math.max(0,hi-lo)];
        for(int n=0;n<arr.length;n++)arr[n]=lo+n;
        return arr;
    }
}
