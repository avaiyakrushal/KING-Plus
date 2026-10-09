package com.kingplus.social;

/** Conservative visual/native pressure responses that do not interrupt a live mic. */
public final class KingPartyMemory969 {
    public static final int MAX_ANIMATED_REACTIONS = 4;
    // Android ComponentCallbacks2 levels; literal constants keep this helper JVM-testable.
    public static final int RUNNING_LOW = 10;
    public static final int RUNNING_CRITICAL = 15;
    private KingPartyMemory969(){}
    public static boolean trimVisuals(int level){
        return level>=RUNNING_LOW;
    }
    public static boolean mayPauseIdleVoice(int level,boolean microphoneOn,boolean voiceConnected){
        return level>=RUNNING_CRITICAL
            && !microphoneOn && voiceConnected;
    }
    public static boolean overEffectLimit(int active){return active>MAX_ANIMATED_REACTIONS;}
}
