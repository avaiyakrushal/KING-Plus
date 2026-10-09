package com.kingplus.social;
public final class KingGiftSafety971Test {
    private static int n;
    private static void chk(boolean b, boolean want, String why){if(b!=want)throw new AssertionError(why);n++;}
    public static void main(String[] args){
        chk(KingGiftSafety971.validCloudGift("a","b","room",true,100,1,100),true,"normal");
        chk(KingGiftSafety971.validCloudGift("a","b","room",true,100,99,9900),true,"bundle");
        chk(KingGiftSafety971.validCloudGift("a","b","room",true,100001,1,100001),false,"backend cap");
        chk(KingGiftSafety971.validCloudGift("a","b","room",false,100,1,100),false,"not member");
        chk(KingGiftSafety971.validCloudGift("a","a","room",true,100,1,100),false,"self gift");
        chk(KingGiftSafety971.validCloudGift(null,"b","room",true,100,1,100),false,"guest");
        chk(KingGiftSafety971.validCloudGift("a",null,"room",true,100,1,100),false,"blank receiver");
        chk(KingGiftSafety971.validCloudGift("a","b",null,true,100,1,100),false,"blank room");
        chk(KingGiftSafety971.validCloudGift("a","b","room",true,100,100,10000),false,"bundle >99");
        chk(KingGiftSafety971.validCloudGift("a","b","room",true,100,1,99),false,"wrong total");
        chk(KingGiftSafety971.validCloudGift("a","b","room",true,Integer.MAX_VALUE,99,-5),false,"overflow");
        chk(KingGiftSafety971.validCloudGift("a","b","room",true,0,1,0),false,"zero price");
        chk(KingGiftSafety971.validLiveEmoji("✨",true),true,"emoji event");
        chk(KingGiftSafety971.validLiveEmoji("",true),false,"empty");
        chk(KingGiftSafety971.validLiveEmoji("✨",false),false,"not joined");
        chk(KingGiftSafety971.validLiveEmoji("a".repeat(201),true),false,"oversized");
        chk(KingGiftSafety971.giftMessage("Rose","🌹",5).contains("x5"),true,"canonical receipt");
        System.out.println("PASS KING Plus v9.7.1 live Emoji/Gift validation: "+n+" tests");
    }
}
