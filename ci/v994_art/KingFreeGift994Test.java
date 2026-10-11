package com.kingplus.social;
public final class KingFreeGift994Test {
 private static int tests;
 private static void check(boolean condition,String name){
  if(!condition)throw new AssertionError(name);
  tests++;
 }
 public static void main(String[] args){
  String me="uid_Me_1",other="uid_friend_2";long now=8000L;
  check(KingFreeGift973.allowed(me,me,1,0L,now,true),
   "FREE self-gift is permitted after real room member join");
  check(KingFreeGift973.allowed(me,other,3,0L,now,true),
   "FREE gift to another member still works");
  check(!KingFreeGift973.allowed(me,me,1,0L,now,false),
   "Self gift cannot bypass room presence");
  check(!KingFreeGift973.allowed(me,me,10,0L,now,true),
   "Quantity still limited to nine");
  check(!KingFreeGift973.allowed(me,me,1,7800L,now,true),
   "Self gift cannot bypass rate limiting");
  check(!KingFreeGift973.allowed("", "",1,0L,now,true),
   "Blank identities still disallowed");
  check(!KingFreeGift973.validDirect(me,me,1),
   "Direct monetary/social self-gift guard remains unchanged");
  check(KingFreeGift973.giftValue()==0 && !KingFreeGift973.paidTransferEnabled(),
   "Never mint or spend currency for FREE gift");
  System.out.println("PASS "+tests+" safe free self gifting tests");
 }
}
