package com.kingplus.social;

import android.app.Activity;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.SystemClock;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/** Original, short synthetic gift SFX (kiss pop, whoosh, party, crown, chime).
 * No copied sound recordings, assets, voice capture, billing or file storage.
 * Single audio stream max: low-memory devices and microphone audio stay stable.
 */
public final class KingGiftSound993 {
    private KingGiftSound993(){}
    private static final AtomicBoolean busy=new AtomicBoolean(false);
    private static volatile long lastStarted;
    public static boolean enabled(Activity host){
        return host!=null && host.getSharedPreferences("MainActivity",Activity.MODE_PRIVATE)
            .getBoolean("gift_audio_993",true);
    }
    public static void setEnabled(Activity host,boolean enabled){
        if(host!=null)host.getSharedPreferences("MainActivity",Activity.MODE_PRIVATE).edit()
            .putBoolean("gift_audio_993",enabled).apply();
    }
    public static String style(String gift){
        String s=gift==null?"":gift.toLowerCase(Locale.US);
        if(s.contains("kiss")||s.contains("hug")||s.contains("love")||
           s.contains("heart")||s.contains("rose"))return "kiss";
        if(s.contains("car")||s.contains("bike")||s.contains("horse"))return "drive";
        if(s.contains("jet")||s.contains("rocket")||s.contains("plane")||
           s.contains("helicopter")||s.contains("flying"))return "whoosh";
        if(s.contains("firework")||s.contains("party")||s.contains("festival")||
           s.contains("confetti")||s.contains("dance"))return "party";
        if(s.contains("vip")||s.contains("royal")||s.contains("king")||
           s.contains("crown")||s.contains("throne")||s.contains("night"))return "royal";
        return "chime";
    }
    public static void play(Activity host,String gift){
        if(host==null||host.isFinishing()||host.isDestroyed()||!enabled(host))return;
        long now=SystemClock.elapsedRealtime();
        if(now-lastStarted<850L||!busy.compareAndSet(false,true))return;
        lastStarted=now;
        final String sound=style(gift);
        new Thread(()->{
            AudioTrack track=null;
            try{
                final int rate=16000,frames=11200; // 700ms, 22.4 KB
                short[] pcm=new short[frames];
                long seed=0x9e3779b97f4a7c15L;
                for(int i=0;i<frames;i++){
                    double t=i/(double)rate;
                    double envelope=Math.min(1,t/.028)*Math.min(1,(.70-t)/.13);
                    double val=0;
                    switch(sound){
                    case "kiss": {
                        // Gentle plop / sparkling lip pop, not human speech.
                        double f=310+1750*Math.exp(-t*23);
                        double pop=Math.sin(2*Math.PI*f*t)*Math.exp(-t*16);
                        double sparkle=Math.sin(2*Math.PI*(860+200*t)*t)*Math.exp(-Math.max(0,t-.17)*8);
                        val=pop*.94+sparkle*.25;
                        break;
                    }
                    case "drive":
                        val=.5*Math.sin(2*Math.PI*(95+140*t)*t)
                            +.24*Math.sin(2*Math.PI*(190+160*t)*t);
                        break;
                    case "whoosh":
                        seed^=(seed<<13);seed^=(seed>>>7);seed^=(seed<<17);
                        double noise=((seed & 0xffff)/32768.0-1.0);
                        val=.54*noise*Math.sin(Math.PI*t/.7)
                             +.32*Math.sin(2*Math.PI*(120+1600*t*t)*t);
                        break;
                    case "party":
                        double phase=(t*4)%1.0;
                        val=.62*Math.sin(2*Math.PI*(350+phase*390)*t)
                             +.25*Math.sin(2*Math.PI*980*t);
                        break;
                    case "royal":
                        double tone=t<.22?523.25:t<.44?659.25:783.99;
                        val=.73*Math.sin(2*Math.PI*tone*t)
                             +.20*Math.sin(2*Math.PI*tone*2*t);
                        break;
                    default:
                        val=.65*Math.sin(2*Math.PI*(790+240*t)*t)
                            +.24*Math.sin(2*Math.PI*1160*t);
                    }
                    double s=Math.max(-1,Math.min(1,val*envelope*.24));
                    pcm[i]=(short)(s*32767);
                }
                byte[] bytes=new byte[frames*2];
                for(int i=0;i<frames;i++){
                    bytes[i*2]=(byte)(pcm[i]&255);
                    bytes[i*2+1]=(byte)((pcm[i]>>>8)&255);
                }
                track=new AudioTrack.Builder()
                  .setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                  .setAudioFormat(new AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(rate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                  .setTransferMode(AudioTrack.MODE_STATIC)
                  .setBufferSizeInBytes(bytes.length).build();
                int wrote=track.write(bytes,0,bytes.length);
                if(wrote>0){track.play();Thread.sleep(740);}
            }catch(Exception ignored){
                // Never crash or disrupt voice chat if audio output is unavailable.
            }finally{
                if(track!=null){
                    try{track.stop();}catch(Exception ignored){}
                    try{track.release();}catch(Exception ignored){}
                }
                busy.set(false);
            }
        },"KING-Gift-SFX-993").start();
    }
}
