package com.kingplus.social;
public final class KingGiftDeliveryQueue9944Test {
 static void ok(boolean x,String s){if(!x)throw new AssertionError(s);}
 public static void main(String[] args){
  KingGiftDeliveryQueue9944<String> q=new KingGiftDeliveryQueue9944<>();
  ok(q.isEmpty(),"empty");
  ok(!q.offer("", "Gift"),"blank id");
  ok(!q.offer("null",null),"null gift");
  for(int i=0;i<KingGiftDeliveryQueue9944.LIMIT;i++)
      ok(q.offer("gift-"+i,"Animation "+i),"accept #"+i);
  ok(q.size()==24,"bounded capacity");
  ok(!q.offer("gift-overflow","Never shown"),"overflow cannot claim displayed");
  ok(!q.offer("gift-3","Duplicate"),"same Gift never repeated while queued");
  for(int i=0;i<24;i++){
    KingGiftDeliveryQueue9944.Entry<String> p=q.poll();
    ok(p!=null&&p.id.equals("gift-"+i)&&p.payload.equals("Animation "+i),"FIFO #"+i);
  }
  ok(q.isEmpty(),"all gifts consumed");
  ok(q.offer("gift-3","Retry on cancelled overlay"),"failed/cancelled receipt retryable");
  ok(q.clear().size()==1,"cancel returns pending to release displayed IDs");
  ok(q.offer("gift-3","Retry after Party rerender"),"clear makes gift retryable");
  ok(q.poll()!=null&&q.poll()==null,"poll removes single gift");
  System.out.println("PASS Gift FIFO/24 cap, no overflow acknowledgements, dedupe, cancelled/reconnect retry");
 }
}
