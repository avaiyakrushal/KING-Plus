package com.kingplus.social;
public final class KingFamilyChat972Test {
    static int checks;
    static void eq(boolean got,boolean expected,String label){
        if(got!=expected)throw new AssertionError(label);checks++;
    }
    public static void main(String[] args){
        eq(KingFamilyChat972.validFamilyCode("AB12C3"),true,"six char");
        eq(KingFamilyChat972.validFamilyCode("abc123"),false,"uppercase required");
        eq(KingFamilyChat972.validFamilyCode("12345"),false,"too short");
        eq(KingFamilyChat972.validFamilyCode(null),false,"missing family");
        eq(KingFamilyChat972.validMessage("Hello KING"),true,"family chat");
        eq(KingFamilyChat972.validMessage("   "),false,"empty text");
        eq(KingFamilyChat972.validMessage("a".repeat(1000)),true,"1000 max");
        eq(KingFamilyChat972.validMessage("a".repeat(1001)),false,"oversized");
        String alice="A_user_with_verified_UID_123";
        String bob="B_user_with_verified_UID_456";
        eq(KingFamilyChat972.validPeer(alice,bob),true,"distinct user");
        eq(KingFamilyChat972.validPeer(alice,alice),false,"not self");
        eq(KingFamilyChat972.validPeer(alice,"123456"),false,"alias not Firebase UID");
        eq(KingFamilyChat972.validDirectGift(alice,bob,true,250,4,1000),true,"verified direct gift");
        eq(KingFamilyChat972.validDirectGift(alice,bob,true,250,4,999),false,"wrong total");
        eq(KingFamilyChat972.validDirectGift(alice,bob,false,250,4,1000),false,"offline can't send real gift");
        eq(KingFamilyChat972.validDirectGift(alice,bob,true,2000,99,198000),false,"server max");
        eq(KingFamilyChat972.validDirectGift(alice,bob,true,250,0,0),false,"zero quantity");
        System.out.println("PASS KING Plus v9.7.2 Family/Chat/Gifts: "+checks+" tests");
    }
}
