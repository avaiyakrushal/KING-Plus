package com.kingplus.social;
public final class KingGiftReceiptRetry9955Test {
 private static void yes(boolean x,String s){if(!x)throw new AssertionError(s);}
 private static void no(boolean x,String s){yes(!x,s);}
 public static void main(String[]args){
  yes(KingGiftReceiptRetry9955.allowEnqueue(0),"first receipt queued");
  yes(KingGiftReceiptRetry9955.allowEnqueue(63),"bounded pending capacity");
  no(KingGiftReceiptRetry9955.allowEnqueue(64),"full pending queue rejected");
  no(KingGiftReceiptRetry9955.allowEnqueue(-1),"invalid queue size");
  for(int attempt=0;attempt<4;attempt++)yes(KingGiftReceiptRetry9955.shouldRetry(attempt,true,true),"retry "+attempt);
  no(KingGiftReceiptRetry9955.shouldRetry(4,true,true),"never unlimited retries");
  no(KingGiftReceiptRetry9955.shouldRetry(-1,true,true),"invalid attempt");
  no(KingGiftReceiptRetry9955.shouldRetry(0,false,true),"removed user cannot acknowledge");
  no(KingGiftReceiptRetry9955.shouldRetry(0,true,false),"no invisible gift ACK");
  yes(KingGiftReceiptRetry9955.delayMs(1)==2000L,"2 seconds");
  yes(KingGiftReceiptRetry9955.delayMs(2)==4000L,"4 seconds");
  yes(KingGiftReceiptRetry9955.delayMs(3)==8000L,"8 seconds");
  yes(KingGiftReceiptRetry9955.delayMs(4)==16000L,"16 seconds");
  yes(KingGiftReceiptRetry9955.delayMs(90)==16000L,"bounded 16 second max");
  System.out.println("PASS gift displayed -> recipient ack retry bounded, anti-ghost and non-foreground guard");
 }
}
