package com.kingplus.social;
public final class KingRoomSettleRetry9954Test {
 static void ok(boolean x,String m){if(!x)throw new AssertionError(m);}
 static void no(boolean x,String m){ok(!x,m);}
 public static void main(String[]args){
  for(int i=0;i<KingRoomSettleRetry9954.MAX_RETRIES;i++)
   ok(KingRoomSettleRetry9954.allowRetry(i,true,true,true,true),"retry "+i);
  no(KingRoomSettleRetry9954.allowRetry(4,true,true,true,true),"bounded retry");
  no(KingRoomSettleRetry9954.allowRetry(-1,true,true,true,true),"negative counter");
  no(KingRoomSettleRetry9954.allowRetry(0,false,true,true,true),"stale round");
  no(KingRoomSettleRetry9954.allowRetry(0,true,false,true,true),"no Host role");
  no(KingRoomSettleRetry9954.allowRetry(0,true,true,false,true),"removed member");
  no(KingRoomSettleRetry9954.allowRetry(0,true,true,true,false),"closed round");
  ok(KingRoomSettleRetry9954.delayMs(1)==2000,"retry 2s");
  ok(KingRoomSettleRetry9954.delayMs(2)==4000,"retry 4s");
  ok(KingRoomSettleRetry9954.delayMs(3)==8000,"retry 8s");
  ok(KingRoomSettleRetry9954.delayMs(4)==16000,"retry 16s");
  ok(KingRoomSettleRetry9954.delayMs(100)==16000,"bounded delay");
  System.out.println("PASS bounded reconnect retry policy without stale or unauthorized round result");
 }
}
