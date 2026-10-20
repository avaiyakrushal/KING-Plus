package com.kingplus.social;
public final class KingTreasureHuntRules10100Test {
    private static void check(boolean ok) { if (!ok) throw new AssertionError(); }
    public static void main(String[] args) {
        check(KingTreasureHuntRules10100.validChest(1));
        check(KingTreasureHuntRules10100.validChest(3));
        check(!KingTreasureHuntRules10100.validChest(0));
        check(!KingTreasureHuntRules10100.validChest(4));
        check(KingTreasureHuntRules10100.validPick("Chest 2"));
        check(!KingTreasureHuntRules10100.validPick("Chest 9"));
        check(KingTreasureHuntRules10100.hit("Chest 2",2));
        check(!KingTreasureHuntRules10100.hit("Chest 1",2));
        check(!KingTreasureHuntRules10100.hit("Chest 2",4));
        System.out.println("PASS free live Treasure Hunt chest validation and result rules");
    }
}
