package com.kingplus.social;
public final class KingPremiumAccess981Test{
    static int checked;
    static void eq(boolean got,boolean want,String why){if(got!=want)throw new AssertionError(why);checked++;}
    public static void main(String[]args){
        eq(KingPremiumAccess981.PAID_VIP_ACTIVE,false,"payments remain off");
        eq(KingPremiumAccess981.allowedFrame("Minimal Frame",0,0),true,"starter frame");
        eq(KingPremiumAccess981.allowedFrame("Music Frame",5,0),true,"earned level frame");
        eq(KingPremiumAccess981.allowedFrame("Music Frame",4,0),false,"level not reached");
        eq(KingPremiumAccess981.allowedFrame("Heart Frame",10,0),true,"heart level");
        eq(KingPremiumAccess981.allowedFrame("Heart Frame",9,0),false,"heart locked");
        eq(KingPremiumAccess981.allowedFrame("Royal Frame",99,50),false,"never grant paid VIP before verifier");
        eq(KingPremiumAccess981.allowedFrame("Galaxy Frame",99,50),false,"no local TEST VIP");
        eq(KingPremiumAccess981.allowedFrame("Crown Frame",99,50),false,"no paid crown");
        eq(KingPremiumAccess981.allowedFrame("unknown",99,99),false,"unknown frame");
        eq(KingPremiumAccess981.allowedEffect("None",0,0),true,"no effect");
        eq(KingPremiumAccess981.allowedEffect("Welcome Sparkle",3,0),true,"earned sparkle");
        eq(KingPremiumAccess981.allowedEffect("Welcome Sparkle",2,0),false,"unearned sparkle");
        eq(KingPremiumAccess981.allowedEffect("Crown Drop",99,50),false,"no fake crown");
        eq(KingPremiumAccess981.allowedEffect("Rose Shower",99,50),false,"no fake rose");
        eq(KingPremiumAccess981.allowedEffect("Galaxy Portal",99,50),false,"no fake galaxy");
        eq(KingPremiumAccess981.allowedEffect("Royal Arrival",99,50),false,"no fake arrival");
        System.out.println("PASS KING Plus v9.8.1 verified cosmetic policy: "+checked);
    }
}
