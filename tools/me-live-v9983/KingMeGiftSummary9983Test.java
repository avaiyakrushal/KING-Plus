package com.kingplus.social;
import java.util.*;
public final class KingMeGiftSummary9983Test {
 private static void ok(boolean x,String m){if(!x)throw new AssertionError(m);}
 public static void main(String[] args){
  List<KingMeGiftSummary9983.Gift> v=Arrays.asList(
   new KingMeGiftSummary9983.Gift("one","alice","me","Rose",0,10),
   new KingMeGiftSummary9983.Gift("two","me","bob","Royal Heart",50,20),
   new KingMeGiftSummary9983.Gift("one","alice","me","Rose",0,10),
   new KingMeGiftSummary9983.Gift("three","eve","other","Golden Car",100,40),
   new KingMeGiftSummary9983.Gift("four","eve","me","Rose",10,30),
   new KingMeGiftSummary9983.Gift("five","eve","me","",100,44));
  List<KingMeGiftSummary9983.Gift> all=KingMeGiftSummary9983.forAccount(v,"me",false);
  ok(all.size()==3,"unique only current account's real receipts");
  ok(all.get(0).id.equals("four")&&all.get(1).id.equals("two"),"recent first");
  ok(KingMeGiftSummary9983.forAccount(v,"me",true).size()==2,"only received wall");
  Map<String,Integer> wall=KingMeGiftSummary9983.receivedWall(v,"me");
  ok(wall.size()==1&&wall.get("Rose")==2,"real received Rose count; no duplicates");
  ok(KingMeGiftSummary9983.forAccount(v,"",false).isEmpty(),"signed-out history empty");
  ok(KingMeGiftSummary9983.validWish("King Dragon"),"known wish accepted");
  ok(!KingMeGiftSummary9983.validWish("1000000 Diamonds"),"no fabricated currencies");
  System.out.println("PASS Me Gift Wall/account History: 6 records 3 true unique receipts, no others, 2 received + allowed wishes");
 }
}
