package com.kingplus.social;
public final class KingFreeGift973Test {
    static int checks;
    static void eq(boolean got,boolean want,String name){
        if(got!=want)throw new AssertionError(name);checks++;
    }
    public static void main(String[] args){
        eq(KingFreeGift973.allowed("alice","bob",1,0,10000,true),true,"room gift");
        eq(KingFreeGift973.allowed("alice","bob",9,0,10000,true),true,"max quantity");
        eq(KingFreeGift973.allowed("alice","bob",10,0,10000,true),false,"quantity limit");
        eq(KingFreeGift973.allowed("alice","bob",0,0,10000,true),false,"zero qty");
        eq(KingFreeGift973.allowed("alice","alice",1,0,10000,true),false,"no self");
        eq(KingFreeGift973.allowed("alice","bob",1,9000,10000,true),false,"cooldown");
        eq(KingFreeGift973.allowed("alice","bob",1,8500,10000,true),true,"cooldown reached");
        eq(KingFreeGift973.allowed("alice","bob",1,0,10000,false),false,"not in room");
        eq(KingFreeGift973.allowed(null,"bob",1,0,10000,true),false,"guest blocked");
        eq(KingFreeGift973.validDirect("alice","bob",1),true,"direct free gift");
        eq(KingFreeGift973.validDirect("alice","bob",9),true,"direct max qty");
        eq(KingFreeGift973.validDirect("alice","bob",10),false,"direct spam");
        eq(KingFreeGift973.validDirect("alice","alice",1),false,"direct self");
        eq(KingFreeGift973.giftValue()==0,true,"no currency");
        eq(KingFreeGift973.paidTransferEnabled(),false,"never call paid transfer");
        System.out.println("PASS KING Plus No Billing Gift: "+checks+" cases");
    }
}
