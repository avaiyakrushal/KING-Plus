#!/usr/bin/env python3
"""Stage 4: original cinematic VIP Night sound (distinct 1.45-second melody)
and accessible Gift Audio ON/OFF control. This is audio UX; paid VIP/Noble and
Diamond Recharge remain server-locked. No Bolo Hi copyrighted files copied.
"""
from pathlib import Path
import sys
p=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social'
f=p/'KingGiftSound993.java'
s=f.read_text()
def one(a,b,label):
 global s
 n=s.count(a)
 assert n==1,f'{label} anchor {n}'
 s=s.replace(a,b,1)
 print('PASS',label)
one('''if(s.contains("kiss"))return "kiss";''','''if(s.contains("vip night")||s.contains("vip-night"))return "vipnight";
        if(s.contains("kiss"))return "kiss";''','dedicated VIP Night category')
one('''final int frames=10400; // 650 ms, ~20 KB/channel''','''final int frames="vipnight".equals(type)?23200:10400; // VIP Night 1.45s; others 650ms
                final double duration=frames/(double)rate;''','longer cinematic VIP Night sound without external assets')
one('''Math.min(1.0,(.65-t)/.14)''','''Math.min(1.0,(duration-t)/.19)''','duration-aware sound envelope')
one('''case "royal":
                            value=''','''case "vipnight":
                            // Original royal-night fanfare: warm bass + staggered crystal bells.
                            value=.40*Math.sin(2*Math.PI*(115+35*Math.floor(t*3))*t)
                                +.28*Math.sin(2*Math.PI*(freq*1.2)*t)*Math.exp(-phase*2.8)
                                +.16*Math.sin(2*Math.PI*(freq*2.9)*t)*Math.exp(-phase*4.4);
                            break;
                        case "royal":
                            value=''','vipnight three-voice timbre')
one('''Thread.sleep(710);''','''Thread.sleep((long)(duration*1000)+60L);''','release AudioTrack at sound end')
f.write_text(s)

pfile=p/'PartyActivity.java';s=pfile.read_text()
menu='''items.add("📹 Multi Video");'''
assert s.count(menu)==1
s=s.replace(menu,menu+''' items.add(KingGiftSound993.enabled(this)?"🔊 Gift Audio ON":"🔇 Gift Audio OFF");''',1)
handler='''else if(x.contains("Multi Video"))openVideoRoom();'''
assert s.count(handler)==1
s=s.replace(handler,handler+'''
        else if(x.contains("Gift Audio")){
            boolean nextAudio=!KingGiftSound993.enabled(this);
            KingGiftSound993.setEnabled(this,nextAudio);
            toast(nextAudio?"Gift sounds enabled":"Gift sounds muted on this device");
        }''',1)
pfile.write_text(s)
print('PASS toggle gift sound in Party menu, saved per phone; gifting/mic untouched')
print('NOTE VIP Night/paid noble/recharge still locked without verified backend')
