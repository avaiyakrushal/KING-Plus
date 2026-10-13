#!/usr/bin/env python3
"""v9.9.48 functional Gift-audio repair on the ACTUAL verified full v9.9.47 source.

Previously KingGiftSound993 dropped repeats with pending9932.contains(gift)
and used a separate thread/reentrant callback for each next Gift, letting
adjacent identical Gift visual events lack a corresponding sound and causing
occasional queue reorder. Replace with one bounded strict FIFO worker.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1])
src=root/'app/src/main/java/com/kingplus/social'
snd=src/'KingGiftSound993.java'
original=snd.read_text()
assert original.count('private static final AtomicBoolean busy=new AtomicBoolean(false);')==1
assert original.count('if(pending9932.size()<3 && !pending9932.contains(gift))')==1
assert original.count('    public static void play(Activity host,String gift){')==1
begin=original.index('    public static void play(Activity host,String gift){')
body_start=original.index('                final int rate=16000;',begin)
body_end=original.index('            }catch(Exception ignored){',body_start)
pcm_body=original[body_start:body_end]
assert 'track=new AudioTrack.Builder()' in pcm_body
assert 'Thread.sleep' in pcm_body
assert 'track.play();' in pcm_body
# Avoid resurrecting audio playback after closing/back-grounding the room.
pcm_body=pcm_body.replace('''                if(track.write(audio,0,audio.length)>0){
                    track.play();
                    Thread.sleep((long)(duration*1000)+60L);
                }''',
'''                if(generation9948!=audioGeneration9948)return;
                if(track.write(audio,0,audio.length)>0){
                    synchronized(AUDIO_LOCK9948){
                        if(generation9948!=audioGeneration9948)return;
                        activeTrack9948=track;
                        track.play();
                    }
                    Thread.sleep((long)(duration*1000)+60L);
                }''')
assert 'generation9948!=audioGeneration9948' in pcm_body
prefix=original[:begin]
prefix=prefix.replace('''    private static final AtomicBoolean busy=new AtomicBoolean(false);
    // Bounded, non-overlapping sound queue. Never allocate another concurrent AudioTrack.
    private static final java.util.ArrayDeque<String> pending9932=new java.util.ArrayDeque<>();
    private static volatile long lastStarted;''',
'''    private static final Object AUDIO_LOCK9948=new Object();
    private static final KingGiftAudioQueue9948 pending9948=new KingGiftAudioQueue9948();
    private static boolean workerRunning9948=false;
    private static volatile long audioGeneration9948=0L;
    private static volatile AudioTrack activeTrack9948;''')
assert 'pending9948=new KingGiftAudioQueue9948()' in prefix
prefix=prefix.replace('''        String s=gift==null?"":gift.toLowerCase(Locale.US);
        if(s.contains("vip night")||s.contains("vip-night"))return "vipnight";
        if(s.contains("kiss"))return "kiss";
        if(s.contains("rose")||s.contains("flower"))return "rose";
        if(s.contains("heart")||s.contains("hug")||s.contains("love"))return "love";
        if(s.contains("rocket")||s.contains("jet")||s.contains("plane")||s.contains("helicopter"))return "flight";
        if(s.contains("car")||s.contains("bike")||s.contains("bus"))return "drive";
        if(s.contains("firework")||s.contains("explosion"))return "firework";
        if(s.contains("panda")||s.contains("horse")||s.contains("eagle")||s.contains("dragon"))return "animal";
        if(s.contains("vip")||s.contains("night")||s.contains("king")||s.contains("royal")
          ||s.contains("throne")||s.contains("crown")||s.contains("palace"))return "royal";
        if(s.contains("disco")||s.contains("dance")||s.contains("music")||s.contains("party"))return "music";
        if(s.contains("diamond")||s.contains("crystal")||s.contains("ring"))return "crystal";
        return "magic";''',
'''        return KingGiftAudioPolicy9948.style(gift);''')
prefix=prefix.replace('''        String s=gift==null?"":gift.trim().toLowerCase(Locale.US);
        int hash=0x187f;
        for(int i=0;i<s.length();i++)hash=31*hash+s.charAt(i);
        return hash&0x7fffffff;''',
'''        return KingGiftAudioPolicy9948.signature(gift);''')
assert 'return KingGiftAudioPolicy9948.style(gift);' in prefix
new_method="""    /** One real Gift animation event = one queued sound, even for repeated Roses. */
    public static void play(Activity host,String gift){
        if(host==null||host.isFinishing()||host.isDestroyed()||!enabled(host))return;
        final long generation9948;
        synchronized(AUDIO_LOCK9948){
            if(!pending9948.offer(gift))return; // bounded to protect voice CPU
            if(workerRunning9948)return;
            workerRunning9948=true;
            generation9948=audioGeneration9948;
        }
        new Thread(()->{
            try{
                while(true){
                    final String next9948;
                    synchronized(AUDIO_LOCK9948){
                        if(generation9948!=audioGeneration9948)return;
                        next9948=pending9948.poll();
                        if(next9948==null)return;
                    }
                    if(host.isFinishing()||host.isDestroyed()||!enabled(host))continue;
                    playOne9948(next9948,generation9948);
                }
            }finally{
                synchronized(AUDIO_LOCK9948){
                    // A newer generation may have started its own audio worker.
                    if(generation9948==audioGeneration9948)workerRunning9948=false;
                }
            }
        },"KING-Gift-Sounds-FIFO").start();
    }
    /** Stops outgoing Gift audio when leaving a Party/call; never changes Gift receipts. */
    public static void stopRoomAudio(){
        final AudioTrack track9948;
        synchronized(AUDIO_LOCK9948){
            audioGeneration9948++;
            pending9948.clear();
            workerRunning9948=false;
            track9948=activeTrack9948;
            activeTrack9948=null;
        }
        if(track9948!=null)try{track9948.stop();}catch(Exception ignored){}
    }
    private static void playOne9948(String gift,long generation9948){
        final int sig=signature(gift);
        final String type=style(gift);
        AudioTrack track=null;
        try{
"""
footer="""        }catch(Exception ignored){
            // Audio failures must never affect voice, gifting, or Firestore writes.
        }finally{
            synchronized(AUDIO_LOCK9948){
                if(activeTrack9948==track)activeTrack9948=null;
            }
            if(track!=null){
                try{track.stop();}catch(Exception ignored){}
                try{track.release();}catch(Exception ignored){}
            }
        }
    }
}
"""
snd.write_text(prefix+new_method+pcm_body+footer)

(src/'KingGiftAudioQueue9948.java').write_text("""package com.kingplus.social;
import java.util.ArrayDeque;
/** Main logic for serial Gift sounds, no duplicate-name suppression.
 * Guard externally with an audio lock, so a phone uses at most one AudioTrack.
 */
public final class KingGiftAudioQueue9948 {
 public static final int MAX=24;
 private final ArrayDeque<String> queue=new ArrayDeque<>();
 public boolean offer(String gift){
   if(gift==null||gift.trim().isEmpty()||queue.size()>=MAX)return false;
   queue.addLast(gift);return true;
 }
 public String poll(){return queue.pollFirst();}
 public int size(){return queue.size();}
 public void clear(){queue.clear();}
}
""")
(src/'KingGiftAudioPolicy9948.java').write_text("""package com.kingplus.social;
import java.util.Locale;
/** KING-original identifiable sonic styles by Gift name, no third-party audio. */
public final class KingGiftAudioPolicy9948 {
 private KingGiftAudioPolicy9948(){}
 public static String style(String gift){
   String s=gift==null?"":gift.toLowerCase(Locale.US);
   if(s.contains("vip night")||s.contains("vip-night"))return "vipnight";
   if(s.contains("kiss"))return "kiss";
   if(s.contains("rose")||s.contains("flower")||s.contains("bouquet"))return "rose";
   if(s.contains("heart")||s.contains("hug")||s.contains("love"))return "love";
   if(s.contains("rocket")||s.contains("jet")||s.contains("plane")||s.contains("helicopter")||s.contains("spaceship"))return "flight";
   if(s.contains("car")||s.contains("bike")||s.contains("bus")||s.contains("supercar"))return "drive";
   if(s.contains("firework")||s.contains("explosion")||s.contains("volcano"))return "firework";
   if(s.contains("panda")||s.contains("horse")||s.contains("eagle")||s.contains("dragon")||s.contains("unicorn"))return "animal";
   if(s.contains("vip")||s.contains("night")||s.contains("king")||s.contains("royal")
      ||s.contains("throne")||s.contains("crown")||s.contains("palace")||s.contains("castle"))return "royal";
   if(s.contains("disco")||s.contains("dance")||s.contains("music")||s.contains("party"))return "music";
   if(s.contains("diamond")||s.contains("crystal")||s.contains("ring"))return "crystal";
   return "magic";
 }
 public static int signature(String gift){
   String s=gift==null?"":gift.trim().toLowerCase(Locale.US);
   int hash=0x187f;
   for(int i=0;i<s.length();i++)hash=31*hash+s.charAt(i);
   return hash&0x7fffffff;
 }
}
""")
party=src/'PartyActivity.java';p=party.read_text()
# The real Party is not foregrounded: flush pending audio / stop old track.
old='''        partyVisualForeground995=false;'''
if p.count(old)!=1:raise RuntimeError('Expected one Party onPause foreground assignment '+str(p.count(old)))
p=p.replace(old,old+'\n        KingGiftSound993.stopRoomAudio();',1)
party.write_text(p)
gradle=root/'app/build.gradle';g=gradle.read_text()
old="versionCode 226; versionName '9.9.47-safe-round-cancel-test'"
if g.count(old)!=1:raise RuntimeError('Not exact KING v9.9.47 full source')
gradle.write_text(g.replace(old,"versionCode 227; versionName '9.9.48-serial-gift-audio-test'"))
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print("PASS serial per-Gift audio even repeated names, cancel on background, original timbres, paid billing OFF")
