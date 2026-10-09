package com.kingplus.social;
public final class KingSocialGift972Test {
  private static int n=0;
  private static void check(boolean actual,boolean expected,String why) {
    if(actual!=expected)throw new AssertionError(why);n++;
  }
  public static void main(String[] args) {
    check(KingSocialGift972.valid("alice","bob",50,2,100),true,"valid paid gift");
    check(KingSocialGift972.valid("alice","alice",50,2,100),false,"self gift");
    check(KingSocialGift972.valid(null,"bob",50,2,100),false,"guest");
    check(KingSocialGift972.valid("alice",null,50,2,100),false,"no recipient");
    check(KingSocialGift972.valid("alice","bob",50,2,99),false,"forged amount");
    check(KingSocialGift972.valid("alice","bob",100001,1,100001),false,"server bound");
    check(KingSocialGift972.valid("alice","bob",10,0,0),false,"zero quantity");
    check(KingSocialGift972.valid("alice","bob",10,101,1010),false,"large quantity");
    check(KingSocialGift972.valid("alice","bob",1,1,1),true,"minimum amount");
    check(KingSocialGift972.verifiedVip(0,0),true,"guest starting VIP");
    check(KingSocialGift972.verifiedVip(1,500),true,"VIP1 threshold");
    check(KingSocialGift972.verifiedVip(0,500),false,"forged VIP 0");
    check(KingSocialGift972.verifiedVip(1,499),false,"forged VIP 1");
    check(KingSocialGift972.verifiedVip(50,1000000000),true,"VIP max");
    check(KingSocialGift972.verifiedVip(51,1000000000),false,"invalid level");
    check(KingSocialGift972.verifiedVip(1,-1),false,"invalid points");
    System.out.println("PASS KING v9.7.2 secure direct gift/VIP: "+n+" tests");
  }
}
