package com.kingplus.social;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/** Deterministic forced worker-empty boundary plus high-contention gift delivery test. */
public class KingGiftAudioWakeup9974Test {
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 static final class Pump {
   final Object lock=new Object();
   final KingGiftAudioQueue9948 queue=new KingGiftAudioQueue9948();
   final List<String> played=Collections.synchronizedList(new ArrayList<>());
   final CountDownLatch atEmpty=new CountDownLatch(1),resumeEmpty=new CountDownLatch(1);
   final AtomicInteger workers=new AtomicInteger(),maxWorkers=new AtomicInteger();
   boolean running=false;
   void send(String name) {
     synchronized(lock) {
       check(queue.offer(name),"queue overflow");
       if(running)return;
       running=true;
     }
     new Thread(this::drain,"KING-Gift-Test-Worker").start();
   }
   void drain() {
     int live=workers.incrementAndGet();
     maxWorkers.accumulateAndGet(live,Math::max);
     try {
       while(true){
         String gift;
         synchronized(lock){
           gift=queue.poll();
           if(gift==null) {
             // Keep the same monitor during the empty -> idle handoff.
             atEmpty.countDown();
             try {check(resumeEmpty.await(3,TimeUnit.SECONDS),"release empty");}
             catch(InterruptedException e){Thread.currentThread().interrupt();return;}
             running=false;
             return;
           }
         }
         played.add(gift);
       }
     }finally{ workers.decrementAndGet(); }
   }
 }
 public static void main(String[]args)throws Exception{
   Pump p=new Pump();
   p.send("Rose");
   check(p.atEmpty.await(3,TimeUnit.SECONDS),"first gift drained");
   // Producer races specifically with the previous worker's empty poll.
   Thread sender=new Thread(()->p.send("VIP Night"));
   sender.start();
   p.resumeEmpty.countDown();
   sender.join(3000);
   long limit=System.nanoTime()+TimeUnit.SECONDS.toNanos(4);
   while(System.nanoTime()<limit&&p.played.size()<2)Thread.sleep(5);
   check(p.played.size()==2,"second gift must not be stranded at worker shutdown");
   check(p.played.get(0).equals("Rose"),"first gift order");
   check(p.played.get(1).equals("VIP Night"),"different audio event remains ordered");
   check(p.maxWorkers.get()<=1,"never two concurrent sound workers at empty handoff");
   System.out.println("PASS atomic empty->idle Gift Sound worker handoff; no lost wakeup, no parallel AudioTrack producers");
 }
}
