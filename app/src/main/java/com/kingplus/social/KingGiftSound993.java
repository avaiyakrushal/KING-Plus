package com.kingplus.social;

import android.app.Activity;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.SystemClock;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/** Each gift gets its own repeatable melody/timbre.
 * Original synthetic tones, not proprietary Bolo Hi audio or voice recordings.
 * One tiny short 16-bit PCM stream at a time; no sound library / bitmap cache.
 */
public final class KingGiftSound993 {
    private KingGiftSound993(){}
    private static final Object AUDIO_LOCK9948=new Object();
    private static final KingGiftAudioQueue9948 pending9948=new KingGiftAudioQueue9948();
    private static boolean workerRunning9948=false;
    private static volatile long audioGeneration9948=0L;
    private static volatile AudioTrack activeTrack9948;
    public static boolean enabled(Activity a){
        return a!=null&&a.getSharedPreferences("MainActivity",Activity.MODE_PRIVATE)
            .getBoolean("gift_audio_993",true);
    }
    public static void setEnabled(Activity a,boolean enabled){
        if(a!=null)a.getSharedPreferences("MainActivity",Activity.MODE_PRIVATE)
          .edit().putBoolean("gift_audio_993",enabled).apply();
    }
    public static String style(String gift){
        return KingGiftAudioPolicy9948.style(gift);
    }
    /** Distinct pitches/patterns even for two gifts in the SAME category. */
    public static int signature(String gift){
        return KingGiftAudioPolicy9948.signature(gift);
    }
    /** One real Gift animation event = one queued sound, even for repeated Roses. */
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
            // No finally racing a newly started sound worker. The worker
            // releases ownership in the SAME critical section as empty poll.
            while(true){
                final String next9948;
                synchronized(AUDIO_LOCK9948){
                    if(generation9948!=audioGeneration9948)return;
                    next9948=pending9948.poll();
                    if(next9948==null){
                        workerRunning9948=false;
                        return;
                    }
                }
                try{
                    if(host.isFinishing()||host.isDestroyed()||!enabled(host))continue;
                    playOne9948(next9948,generation9948);
                }catch(RuntimeException ignored){
                    // Failed individual sound must not strand subsequent gifts.
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
                final int rate=16000;
                final int frames="vipnight".equals(type)?23200:10400; // VIP Night 1.45s; others 650ms
                final double duration=frames/(double)rate;
                byte[] audio=new byte[frames*2];
                long noise=(sig|1);
                double base=260+(sig%420);
                double step=((sig/13)%9)+3;
                double pulseRate=3+(sig%6);
                for(int i=0;i<frames;i++){
                    double t=i/(double)rate;
                    double release=Math.min(1.0,t/.014)*Math.min(1.0,(duration-t)/.19);
                    double phase=(t*pulseRate)%1.0;
                    double note=Math.floor(t*(type.equals("music")?8:5));
                    double freq=base*(1+step*.022*note);
                    double value;
                    switch(type){
                        case "kiss":
                            value=.6*Math.sin(2*Math.PI*(freq+1250*Math.exp(-t*38))*t)
                              *Math.exp(-t*12)+.28*Math.sin(2*Math.PI*(freq*2.8)*t);
                            break;
                        case "flight":
                            noise^=(noise<<13);noise^=(noise>>>7);noise^=(noise<<17);
                            value=.46*Math.sin(2*Math.PI*(95+freq*t*3)*t)
                                  +.45*((noise&65535)/32768.0-1.0)*Math.sin(Math.PI*t/.65);
                            break;
                        case "drive":
                            value=.45*Math.sin(2*Math.PI*(65+freq*t*.6)*t)
                                  +.31*Math.sin(2*Math.PI*(145+freq*t)*t);
                            break;
                        case "firework":
                            noise^=(noise<<13);noise^=(noise>>>7);noise^=(noise<<17);
                            value=.3*Math.sin(2*Math.PI*(freq+600*t)*t)
                               +.5*((noise&65535)/32768.0-1.0)*Math.exp(-phase*7);
                            break;
                        case "vipnight":
                            // Original royal-night fanfare: warm bass + staggered crystal bells.
                            value=.40*Math.sin(2*Math.PI*(115+35*Math.floor(t*3))*t)
                                +.28*Math.sin(2*Math.PI*(freq*1.2)*t)*Math.exp(-phase*2.8)
                                +.16*Math.sin(2*Math.PI*(freq*2.9)*t)*Math.exp(-phase*4.4);
                            break;
                        case "royal":
                            value=.64*Math.sin(2*Math.PI*freq*t)+.28*Math.sin(2*Math.PI*freq*2*t)
                                   +.20*Math.sin(2*Math.PI*(freq*.5)*t);
                            break;
                        case "crystal":
                            value=.6*Math.sin(2*Math.PI*freq*t)*Math.exp(-phase*3)
                                  +.29*Math.sin(2*Math.PI*(freq*2.4)*t);
                            break;
                        case "animal":
                            value=.52*Math.sin(2*Math.PI*(freq+90*Math.sin(t*27))*t)
                                  +.27*Math.sin(2*Math.PI*(freq*.65)*t);
                            break;
                        case "rose":
                            value=.54*Math.sin(2*Math.PI*(freq+100*Math.sin(t*6))*t)
                                  +.24*Math.sin(2*Math.PI*(freq*1.5)*t);
                            break;
                        case "love":
                            value=.5*Math.sin(2*Math.PI*freq*t)+.28*Math.sin(2*Math.PI*freq*.8*t);
                            value*=.72+.28*Math.sin(2*Math.PI*4*t);
                            break;
                        case "music":
                            value=.58*Math.sin(2*Math.PI*freq*t)*Math.exp(-phase*2.6)
                                  +.25*Math.sin(2*Math.PI*(freq*1.50)*t);
                            break;
                        default:
                            value=.6*Math.sin(2*Math.PI*freq*t)+.2*Math.sin(2*Math.PI*(freq*2.13)*t);
                    }
                    double gain=(.18+(sig%7)*.007)*Math.max(0,release);
                    int pcm=(int)(Math.max(-1,Math.min(1,value*gain))*32767);
                    audio[i*2]=(byte)(pcm&255);
                    audio[i*2+1]=(byte)((pcm>>>8)&255);
                }
                track=new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                    .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(rate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(audio.length).build();
                if(generation9948!=audioGeneration9948)return;
                if(track.write(audio,0,audio.length)>0){
                    synchronized(AUDIO_LOCK9948){
                        if(generation9948!=audioGeneration9948)return;
                        activeTrack9948=track;
                        track.play();
                    }
                    Thread.sleep((long)(duration*1000)+60L);
                }
        }catch(Exception ignored){
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
