package com.kingplus.social;

/** Conservative visual/native pressure responses that do not interrupt a live mic. */
public final class KingPartyMemory969 {
    public static final int MAX_ANIMATED_REACTIONS = 4;
    private KingPartyMemory969(){}
    public static boolean trimVisuals(int level){
        return level>=android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW;
    }
    public static boolean mayPauseIdleVoice(int level,boolean microphoneOn,boolean voiceConnected){
        return level>=android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL
            && !microphoneOn && voiceConnected;
    }
    public static boolean overEffectLimit(int active){return active>MAX_ANIMATED_REACTIONS;}
}
