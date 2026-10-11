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
    private static final AtomicBoolean busy=new AtomicBoolean(false);
    private static volatile long lastStarted;
    public static boolean enabled(Activity a){
        return a!=null&&a.getSharedPreferences("MainActivity",Activity.MODE_PRIVATE)
            .getBoolean("gift_audio_993",true);
    }
    public static void setEnabled(Activity a,boolean enabled){
        if(a!=null)a.getSharedPreferences("MainActivity",Activity.MODE_PRIVATE)
          .edit().putBoolean("gift_audio_993",enabled).apply();
    }
    public static String style(String gift){
        String s=gift==null?"":gift.toLowerCase(Locale.US);
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
        return "magic";
    }
    /** Distinct pitches/patterns even for two gifts in the SAME category. */
    public static int signature(String gift){
        String s=gift==null?"":gift.trim().toLowerCase(Locale.US);
        int hash=0x187f;
        for(int i=0;i<s.length();i++)hash=31*hash+s.charAt(i);
        return hash&0x7fffffff;
    }
    public static void play(Activity host,String gift){
        if(host==null||host.isFinishing()||host.isDestroyed()||!enabled(host))return;
        final long now=SystemClock.elapsedRealtime();
        if(now-lastStarted<480||!busy.compareAndSet(false,true))return;
        lastStarted=now;
        final int sig=signature(gift);
        final String type=style(gift);
        new Thread(()->{
            AudioTrack track=null;
            try{
                final int rate=16000;
                final int frames=10400; // 650 ms, ~20 KB/channel
                byte[] audio=new byte[frames*2];
                long noise=(sig|1);
                double base=260+(sig%420);
                double step=((sig/13)%9)+3;
                double pulseRate=3+(sig%6);
                for(int i=0;i<frames;i++){
                    double t=i/(double)rate;
                    double release=Math.min(1.0,t/.014)*Math.min(1.0,(.65-t)/.14);
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
                if(track.write(audio,0,audio.length)>0){
                    track.play();
                    Thread.sleep(710);
                }
            }catch(Exception ignored){
                // Audio failures never affect Firestore room state or mic.
            }finally{
                if(track!=null){
                    try{track.stop();}catch(Exception ignored){}
                    try{track.release();}catch(Exception ignored){}
                }
                busy.set(false);
            }
        },"KING-Distinct-Gift-Audio").start();
    }
}
