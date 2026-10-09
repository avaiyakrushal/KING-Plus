package com.kingplus.social;

/** Client-side guardrails; a real coin transfer must also pass the Cloud Function. */
public final class KingGiftSafety971 {
    public static final long SERVER_GIFT_LIMIT = 100000L;
    private KingGiftSafety971() {}

    public static boolean validCloudGift(String senderUid, String recipientUid, String roomId,
        boolean joined, int unitPrice, int quantity, long chargedAmount) {
        return joined && senderUid != null && !senderUid.trim().isEmpty()
            && recipientUid != null && !recipientUid.trim().isEmpty()
            && !senderUid.equals(recipientUid)
            && roomId != null && !roomId.trim().isEmpty()
            && unitPrice > 0 && quantity >= 1 && quantity <= 99
            && chargedAmount == (long)unitPrice * quantity
            && chargedAmount >= 1 && chargedAmount <= SERVER_GIFT_LIMIT;
    }
    public static boolean validLiveEmoji(String emoji,boolean memberJoined) {
        return memberJoined && emoji!=null
            && !emoji.trim().isEmpty() && emoji.length()<=200;
    }
    public static String giftMessage(String name,String icon,int quantity) {
        String n=name==null?"Gift":name.trim();
        if(n.length()>55)n=n.substring(0,55);
        String e=icon==null||icon.isEmpty()?"🎁":icon;
        return e+" "+n+" x"+Math.max(1,Math.min(99,quantity));
    }
}
