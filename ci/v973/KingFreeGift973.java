package com.kingplus.social;

/** No-billing gift validation. Cosmetics are never a wallet transfer. */
public final class KingFreeGift973 {
    public static final int MAX_QTY = 9;
    public static final long COOLDOWN_MS = 1500L;
    private KingFreeGift973() {}

    public static boolean allowed(String sender, String target, int qty,
            long lastSentMillis, long nowMillis, boolean roomReady) {
        return roomReady && sender!=null && !sender.isEmpty()
            && target!=null && !target.isEmpty()
            && !sender.equals(target) && qty>=1 && qty<=MAX_QTY
            && nowMillis>0L && (lastSentMillis<=0L || nowMillis-lastSentMillis>=COOLDOWN_MS);
    }

    public static boolean validDirect(String sender, String receiver, int qty) {
        return sender!=null && !sender.isEmpty() && receiver!=null && !receiver.isEmpty()
            && !sender.equals(receiver) && qty>=1 && qty<=MAX_QTY;
    }

    public static int giftValue(){return 0;}
    public static boolean paidTransferEnabled(){return false;}
}
