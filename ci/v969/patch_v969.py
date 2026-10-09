#!/usr/bin/env python3
"""KING Plus v9.6.9: resource-pressure protections after v9.6.8 Mic/Seat fix.
Maintains room audio on active microphones; only an idle/muted native Jitsi
instance is released when Android reports CRITICAL memory pressure.
"""
from pathlib import Path
import shutil,sys
root=Path(sys.argv[1]);pkg=root/'app/src/main/java/com/kingplus/social'
p=pkg/'PartyActivity.java';s=p.read_text()
shutil.copy2(Path(__file__).with_name('KingPartyMemory969.java'),pkg/'KingPartyMemory969.java')
def once(old,new,label):
 global s
 n=s.count(old)
 if n!=1:raise SystemExit(f'{label}: source marker count {n} expected 1: {old[:125]!r}')
 s=s.replace(old,new,1)
 print('PASS',label)
once('        activeReactions560.put(key,reaction);',
r'''        activeReactions560.put(key,reaction);
        // Cap simultaneous visual effects. A room of many members can otherwise
        // accumulate full-screen animated views, increasing CPU/GPU/native memory.
        if(KingPartyMemory969.overEffectLimit(activeReactions560.size())){
            for(Map.Entry<String,View> effect969:new HashMap<>(activeReactions560).entrySet()){
                if(!KingPartyMemory969.overEffectLimit(activeReactions560.size()))break;
                if(effect969.getValue()==reaction)continue;
                View old969=activeReactions560.remove(effect969.getKey());
                if(old969!=null){
                    old969.animate().cancel();
                    if(old969.getParent() instanceof android.view.ViewGroup)
                        ((android.view.ViewGroup)old969.getParent()).removeView(old969);
                }
            }
        }''','limit visual effects in crowded Party rooms')
anchor='    @Override protected void onStop(){if(inRoomVoiceJoined940)'
functions=r'''    private void reducePartyMemory969(int level969){
        if(!KingPartyMemory969.trimVisuals(level969))return;
        photoCache540.evictAll();
        // Visual effects are expendable; Mic/Seat and Firebase membership remain intact.
        for(View effect969:new ArrayList<>(activeReactions560.values())){
            if(effect969==null)continue;
            effect969.animate().cancel();
            if(effect969.getParent() instanceof android.view.ViewGroup)
                ((android.view.ViewGroup)effect969.getParent()).removeView(effect969);
        }
        activeReactions560.clear();
        if(KingPartyMemory969.mayPauseIdleVoice(level969,micOn,inRoomVoiceJoined940)){
            // Native Jitsi can retain substantially more memory than Java heap.
            // Do not interrupt actively transmitting microphones.
            roomVoiceOptedIn957=false;
            stopInRoomVoice940(true);
            KingCrashWatch958.mark(this,"party-memory-idle-voice-paused");
        }
    }
    @Override public void onTrimMemory(int level){
        super.onTrimMemory(level);
        reducePartyMemory969(level);
    }
    @Override public void onLowMemory(){
        super.onLowMemory();
        reducePartyMemory969(KingPartyMemory969.RUNNING_CRITICAL);
    }

'''
if s.count(anchor)!=1:raise SystemExit('onStop placement source changed')
s=s.replace(anchor,functions+anchor,1)
print('PASS', 'free expendable images/effects; release idle Jitsi only on Android CRITICAL memory pressure')

p.write_text(s)
gradle=root/'app/build.gradle';g=gradle.read_text()
old="versionCode 159; versionName '9.6.8-party-mic-seat-sync'"
if g.count(old)!=1:raise SystemExit('v9.6.8 baseline not found')
gradle.write_text(g.replace(old,"versionCode 160; versionName '9.6.9-party-memory-pressure'",1))
print('PASS', 'v9.6.9 Android code, all prior Mic/Seat and Social features preserved')
