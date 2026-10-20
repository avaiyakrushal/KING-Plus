package com.kingplus.social;

/** Free shared Party treasure-pick rules. No coin, diamond, or VIP payout. */
public final class KingTreasureHuntRules10100 {
    private KingTreasureHuntRules10100() {}
    public static boolean validChest(int chest) { return chest >= 1 && chest <= 3; }
    public static boolean validPick(String move) {
        return "Chest 1".equals(move) || "Chest 2".equals(move) || "Chest 3".equals(move);
    }
    public static boolean hit(String move, int treasureChest) {
        return validChest(treasureChest) && validPick(move) && move.equals("Chest " + treasureChest);
    }
}
