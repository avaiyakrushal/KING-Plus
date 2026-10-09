package com.kingplus.social;
public class KingPartyMemory969Test {
    public static void main(String[] args){
        int cases=0;
        if(KingPartyMemory969.MAX_ANIMATED_REACTIONS!=4)throw new AssertionError();cases++;
        if(!KingPartyMemory969.overEffectLimit(5))throw new AssertionError();cases++;
        if(KingPartyMemory969.overEffectLimit(4))throw new AssertionError();cases++;
        if(KingPartyMemory969.overEffectLimit(0))throw new AssertionError();cases++;
        if(!KingPartyMemory969.trimVisuals(10))throw new AssertionError();cases++;
        if(KingPartyMemory969.trimVisuals(5))throw new AssertionError();cases++;
        if(!KingPartyMemory969.mayPauseIdleVoice(15,false,true))throw new AssertionError();cases++;
        if(KingPartyMemory969.mayPauseIdleVoice(10,false,true))throw new AssertionError();cases++;
        if(KingPartyMemory969.mayPauseIdleVoice(15,true,true))throw new AssertionError();cases++;
        if(KingPartyMemory969.mayPauseIdleVoice(15,false,false))throw new AssertionError();cases++;
        System.out.println("KING Plus v9.6.9 Party memory guards: "+cases+" tests PASS");
    }
}
