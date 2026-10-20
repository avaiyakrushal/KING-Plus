package com.kingplus.social;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

public final class KingSoundFx {
    private static SoundPool pool;
    private static int gift, join, level, win;
    private KingSoundFx() {}
    private static synchronized void ensure(Context c) {
        if (pool != null) return;
        AudioAttributes a = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        pool = new SoundPool.Builder().setMaxStreams(4).setAudioAttributes(a).build();
        gift = pool.load(c, R.raw.king_gift, 1);
        join = pool.load(c, R.raw.king_join, 1);
        level = pool.load(c, R.raw.king_level, 1);
        win = pool.load(c, R.raw.king_win, 1);
    }
    private static void play(Context c, int which) { try { ensure(c.getApplicationContext()); int id=which==1?gift:which==2?join:which==3?level:win; if(id!=0)pool.play(id,1f,1f,1,0,1f); } catch (Throwable ignored) {} }
    public static void gift(Context c){ play(c,1); }
    public static void join(Context c){ play(c,2); }
    public static void level(Context c){ play(c,3); }
    public static void win(Context c){ play(c,4); }
}
