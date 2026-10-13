#!/usr/bin/env python3
"""Repair a real KING Party Gift audio lost-wakeup race on full signed v9.9.73.

Old worker polled empty under AUDIO_LOCK then cleared workerRunning in finally
after releasing the lock. A new gift could be enqueued during that gap, see
workerRunning=true and never start the sound worker. Also the old finally could
clear a newer worker's flag. The handoff must be atomic under the SAME lock.
"""
from pathlib import Path
import re,sys
root=Path(sys.argv[1])
src=root/'app/src/main/java/com/kingplus/social/KingGiftSound993.java'
s=src.read_text()
begin=s.index('        new Thread(()->{',s.index('    public static void play(Activity host,String gift){'))
needle='},"KING-Gift-Sounds-FIFO").start();'
end=s.index(needle,begin)+len(needle)
old=s[begin:end]
for required in ('next9948=pending9948.poll();','if(next9948==null)return;',
                 'if(generation9948==audioGeneration9948)workerRunning9948=false;',
                 'playOne9948(next9948,generation9948);'):
    assert required in old, 'Unexpected updated audio source: '+required
replacement='''        new Thread(()->{
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
        },"KING-Gift-Sounds-FIFO").start();'''
s=s[:begin]+replacement+s[end:]
assert s.count('workerRunning9948=false;')>=2
assert 'if(next9948==null){\n                        workerRunning9948=false;' in s
assert 'if(generation9948==audioGeneration9948)workerRunning9948=false;' not in s
src.write_text(s)
gradle=root/'app/build.gradle';g=gradle.read_text()
p=r"versionCode\s+252\s*;\s*versionName\s+'[^']+'"
g,count=re.subn(p,"versionCode 253; versionName '9.9.74-gift-audio-race-fix-test'",g)
assert count==1,'Requires exact verified 9.9.73 baseline'
gradle.write_text(g)
party=(root/'app/src/main/java/com/kingplus/social/PartyActivity.java').read_text()
assert 'KingGiftSound993.stopRoomAudio();' in party
assert 'KingGiftPlayedHistory9969' in party
assert 'KingPartyMicConsent9956' in party
assert 'LIVE_RECHARGE = false' in (root/'app/src/main/java/com/kingplus/social/KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (root/'app/src/main/java/com/kingplus/social/KingPremiumAccess981.java').read_text()
print('PASS gift audio drain atomic ownership; same Party/Gameplay source; paid wallet/VIP remain disabled')
