package com.kingplus.social;
public final class KingGiftAudio9948Test {
 static void good(boolean yes,String message){if(!yes)throw new AssertionError(message);}
 static void bad(boolean yes,String message){if(yes)throw new AssertionError(message);}
 public static void main(String[] args){
   KingGiftAudioQueue9948 q=new KingGiftAudioQueue9948();
   good(q.size()==0,"empty");
   bad(q.offer(null),"null excluded");
   bad(q.offer(" "),"blank excluded");
   // The real regression: simultaneous repeated Gift names MUST play 3 times.
   good(q.offer("Rose"),"first Rose");
   good(q.offer("Rose"),"second Rose");
   good(q.offer("Rose"),"third Rose");
   good(q.offer("VIP Night"),"VIP Night after Rose");
   good("Rose".equals(q.poll()),"first sound");
   good("Rose".equals(q.poll()),"repeat sound 2");
   good("Rose".equals(q.poll()),"repeat sound 3");
   good("VIP Night".equals(q.poll()),"distinct gift sequence");
   good(q.poll()==null,"no stray tone");
   for(int i=0;i<KingGiftAudioQueue9948.MAX;i++)
     good(q.offer("Gift-"+i),"queue capacity "+i);
   bad(q.offer("Gift-overflow"),"bounded backlog");
   for(int i=0;i<KingGiftAudioQueue9948.MAX;i++)
     good(("Gift-"+i).equals(q.poll()),"FIFO order "+i);
   good(q.size()==0,"all sounds consumed");
   good(q.offer("Rocket"),"add for background");
   good(q.offer("Rocket"),"second background");
   q.clear();
   good(q.poll()==null,"background sound queue cancelled");
   good(q.offer("Diamond"),"resumed queue supports real new Gifts");
   good("Diamond".equals(q.poll()),"resume");
   good("vipnight".equals(KingGiftAudioPolicy9948.style("VIP Night")),"VIP Night has distinct fanfare");
   good("rose".equals(KingGiftAudioPolicy9948.style("Rose bouquet")),"Rose bouquet melody");
   good("flight".equals(KingGiftAudioPolicy9948.style("Spaceship")),"Spaceship sound");
   good("firework".equals(KingGiftAudioPolicy9948.style("Fireworks")),"Fireworks");
   good("drive".equals(KingGiftAudioPolicy9948.style("Supercar")),"Supercar");
   good("crystal".equals(KingGiftAudioPolicy9948.style("Diamond ring")),"Diamond");
   good("magic".equals(KingGiftAudioPolicy9948.style("Unknown new gift")),"unknown gift still audible");
   good(KingGiftAudioPolicy9948.signature("Rose") == KingGiftAudioPolicy9948.signature("rose"),"stable case");
   bad(KingGiftAudioPolicy9948.signature("Rose")==KingGiftAudioPolicy9948.signature("Heart"),"distinct named tones");
   System.out.println("PASS repeat Gift=repeat sound, original order, bounded 24, pause cancel, per-Gift melody classification");
 }
}
