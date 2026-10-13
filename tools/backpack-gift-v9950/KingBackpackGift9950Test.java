package com.kingplus.social;
public final class KingBackpackGift9950Test {
 private static void ok(boolean p,String msg){if(!p)throw new AssertionError(msg);}
 public static void main(String[] a){
  ok(KingBackpackGift9950.canBegin(1,false),"available");
  ok(!KingBackpackGift9950.canBegin(0,false),"no inventory");
  ok(!KingBackpackGift9950.canBegin(9,true),"duplicate in-flight click");
  ok(KingBackpackGift9950.afterCommitted(3)==2,"one spent after commit");
  ok(KingBackpackGift9950.afterCommitted(1)==0,"last item");
  ok(KingBackpackGift9950.afterCommitted(0)==0,"no negative stock");
  System.out.println("PASS free Backpack commit-only consume and duplicate-send guard");
 }
}
