package com.kingplus.social;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;

/** Lightweight local feedback tone for completed mini-game rounds. */
public final class KingSoundFx {
    private KingSoundFx() {}
    public static void win(Context context) {
        final ToneGenerator tone;
        try { tone = new ToneGenerator(AudioManager.STREAM_MUSIC, 65); }
        catch (RuntimeException unavailable) { return; }
        tone.startTone(ToneGenerator.TONE_PROP_ACK, 180);
        new Handler(Looper.getMainLooper()).postDelayed(tone::release, 240);
    }
    public static void join(Context context) { win(context); }
    public static void gift(Context context) { win(context); }
}
