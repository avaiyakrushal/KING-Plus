package com.kingplus.social;
public final class KingLudoServerDice10069Test {
 public static void main(String[] args){
  if(KingLudoDiceEntropy999.dice(6L,0,"ABC234",0L)!=2)throw new AssertionError("Server/client SHA256 vector mismatch");
  if(KingLudoDiceEntropy999.dice(1700000000L,123456789,"FOUR22",4L)!=3)throw new AssertionError("Server/client SHA256 vector mismatch");
  if(KingLudoDiceEntropy999.dice(1792260000L,999999999,"RACE22",987L)!=4)throw new AssertionError("Server/client SHA256 vector mismatch");
  int[] counts=new int[6];
  for(int i=0;i<6000;i++){int d=KingLudoDiceEntropy999.dice(1700000000L,i*100003,"ABC234",i);counts[d-1]++;}
  for(int count:counts)if(count<850||count>1150)throw new AssertionError("Unexpected die distribution");
  System.out.println("PASS server/client dice vectors and six-sided distribution");
 }
}
