package com.kingplus.social;
public final class KingPartyLiveSeats9970Test {
 private static void yes(boolean value,String why){if(!value)throw new AssertionError(why);}
 private static void no(boolean value,String why){yes(!value,why);}
 public static void main(String[]args){
   for(int count:new int[]{8,10,12}){
     for(int i=1;i<=12;i++)
       yes(KingPartyLiveSeats9970.allowed(count,i)==(i<=count),
         "stage layout count "+count+" seat "+i);
   }
   no(KingPartyLiveSeats9970.allowed(8,9),"no hidden seat 9 on an eight seat stage");
   no(KingPartyLiveSeats9970.allowed(10,11),"10-seat Party rejects seat 11");
   no(KingPartyLiveSeats9970.allowed(12,13),"no seat 13");
   no(KingPartyLiveSeats9970.allowed(8,0),"no seat zero");
   no(KingPartyLiveSeats9970.allowed(6,6),"unsupported layout is not silently permissive");
   yes(KingPartyLiveSeats9970.owned("alice","alice"),"own prior seat");
   no(KingPartyLiveSeats9970.owned("alice","bob"),"never remove another user seat");
   no(KingPartyLiveSeats9970.owned("",""),"no empty account owner");
   System.out.println("PASS real Party 8/10/12 stage capacity, no invalid seats, no other-user kick");
 }
}