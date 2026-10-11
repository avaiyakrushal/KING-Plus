#!/usr/bin/env python3
"""KING Plus 9.9.3: preserve old 64 free gifts, add 12 sound/free gifts,
12 VIP Night locked preview-only gifts. No coins, paid VIP or billing bypass.
"""
from pathlib import Path
import re,shutil,sys
root=Path(sys.argv[1]);pkg=root/'app/src/main/java/com/kingplus/social'
party=pkg/'PartyActivity.java'
gradle=root/'app/build.gradle'
shutil.copy2(Path(__file__).with_name('KingGiftSound993.java'),pkg/'KingGiftSound993.java')
p=party.read_text()
GIFTS=[
("Flying Kiss","💋","Voice",0),("Kiss Sound","😘","Voice",0),
("Royal Horn","📯","Voice",0),("Heart Pulse","💓","Voice",0),
("Rose Rain","🌹","Relationship",0),("Balloon Sky","🎈","Flying",0),
("Flying Hearts","💖","Flying",0),("Firework Music","🎆","Voice",0),
("Rocket Parade","🚀","Flying",0),("Disco Dance","🪩","Voice",0),
("Panda Dance","🐼","Voice",0),("Golden Confetti","🎊","Flying",0),
("VIP Night","🌃","VIP Night",1),("Royal Kiss Show","💋","VIP Night",1),
("Royal Car Show","🏎️","VIP Night",2),("Moon Palace Night","🌙","VIP Night",3),
("Royal Castle","🏰","VIP Night",4),("Golden Dragon Sky","🐉","VIP Night",5),
("Galaxy Kingdom","🌌","VIP Night",6),("Private Jet Parade","✈️","VIP Night",7),
("Crown Storm","👑","VIP Night",8),("Kiss Kingdom","💝","VIP Night",9),
("Flying Throne","🪑","VIP Night",10),("Universe Royal Entry","✨","VIP Night",11)
]
assert len(GIFTS)==24
for arr,idx in [('giftNames',0),('giftIcons',1),('giftCosts',None),
                 ('giftCategories',2),('giftVipRequired',3)]:
    m=re.search(r'(private final (?:String|int)\[\] '+arr+r'\s*=\s*\{)(.*?)(\};)',p,re.S)
    if not m:raise SystemExit('Missing gift array '+arr)
    original=m.group(2)
    oldCount=len(re.findall(r'"[^"]*"',original)) if arr in ('giftNames','giftIcons','giftCategories') else len(re.findall(r'\b\d+\b',original))
    assert oldCount==64,(arr,oldCount)
    newData=['0' if idx is None else (str(g[idx]) if arr=='giftVipRequired' else '"'+g[idx]+'"') for g in GIFTS]
    p=p[:m.end(2)]+',\n        '+','.join(newData)+'\n    '+p[m.end(2):]
    print('PASS array '+arr+':88')
def once(old,new,label):
    global p
    count=p.count(old)
    if count!=1:raise SystemExit(f'{label}: expected 1 anchor, saw {count}')
    p=p.replace(old,new,1)
    print('PASS',label)
once('''TextView title=tv("Gift Shop • 64 gifts",19''',
     '''TextView title=tv("Gift Shop • "+giftNames.length+" gifts",19''',
     'dynamic gift count')
once('''String[] cs={"All","Relationship","Activity","Classic","Flying","Fame","Privilege","Filters","Parcel"};''',
     '''String[] cs={"All","Voice","VIP Night","Flying","Relationship","Activity","Classic","Fame","Privilege","Filters","Parcel"};''',
     'gift category Voice + VIP Night')
once('''final int idx=i;final int vipNeed=giftVipRequired[i];final boolean unlocked=true;''',
     '''final int idx=i;final int vipNeed=giftVipRequired[i];
            final boolean unlocked=idx<64||vipNeed==0;''',
     'original gifts stay free; new VIP gifts not available for sending')
once('''TextView nw=tv(selected?"✓ SELECTED":"FREE",9''',
     '''TextView nw=tv(selected?"✓ SELECTED":unlocked?"FREE":"🔒 VIP "+vipNeed,9''',
     'locked tier on cards')
once('''TextView cost=tv("FREE",9,selected?0xffffd92e:0xffaaa5b4,true);''',
     '''TextView cost=tv(unlocked?"FREE":"PREVIEW",9,
                selected?0xffffd92e:0xffaaa5b4,true);''',
     'locked gifts are explicitly previews')
once('''card.setOnClickListener(v->{if(!unlocked){toast("Unlocks at VIP "+vipNeed);return;}giftSelectedIndex=idx;refreshGiftSelection867();rebuildGiftGrid();});''',
     '''card.setOnClickListener(v->{
                if(!unlocked){
                    showGiftEffect("VIP PREVIEW",safeName(),giftNames[idx],giftIcons[idx],1,0);
                    toast("VIP "+vipNeed+" preview only • Verified recharge is OFF");
                    return;
                }
                giftSelectedIndex=idx;refreshGiftSelection867();rebuildGiftGrid();
            });''',
     'local VIP preview no server spend')
once('''sendGiftQuantity(giftTargetUid,giftTargetName,giftNames[giftSelectedIndex],giftIcons[giftSelectedIndex],0,giftQuantity,0);''',
     '''if(giftSelectedIndex>=64&&giftVipRequired[giftSelectedIndex]>0){
                    toast("VIP gift locked • Payments and verified entitlements are OFF");
                    return;
                }
                sendGiftQuantity(giftTargetUid,giftTargetName,giftNames[giftSelectedIndex],giftIcons[giftSelectedIndex],0,giftQuantity,0);''',
     'guard SEND button against locked VIP')
# User-facing sound preference, low-volume sound is default ON.
once('''giftSelectionLabel=tv(giftSelectionText867(),13,0xffffe38a,true);''',
     '''TextView soundToggle993=pill(
            KingGiftSound993.enabled(this)?"🔊 Gift Sound ON":"🔇 Gift Sound OFF",
            0xff342c55,null);
        soundToggle993.setTextSize(11);
        soundToggle993.setOnClickListener(v->{
            boolean on=!KingGiftSound993.enabled(this);
            KingGiftSound993.setEnabled(this,on);
            soundToggle993.setText(on?"🔊 Gift Sound ON":"🔇 Gift Sound OFF");
        });
        root.addView(soundToggle993,new LinearLayout.LayoutParams(-1,dp(34)));
        giftSelectionLabel=tv(giftSelectionText867(),13,0xffffe38a,true);''',
     'voice gifts toggle')
# Effect is also called when live receiver gets event => one short effect
# per device (and controlled by receiver local mute).
m=re.search(r'private void showGiftEffect\s*\(([^)]*)\)\s*\{',p)
assert m,'Gift effect definition missing'
arglist=[x.strip() for x in m.group(1).split(',')]
assert len(arglist)>=3,arglist
giftVariable=arglist[2].split()[-1]
p=p[:m.end()]+f'\n        KingGiftSound993.play(this,{giftVariable});'+p[m.end():]
party.write_text(p)
g=gradle.read_text()
old="versionCode 181; versionName '9.9.2-id-follow-chat-party-social'"
new="versionCode 182; versionName '9.9.3-action-emoji-vip-voice-gifts'"
assert g.count(old)==1
gradle.write_text(g.replace(old,new,1))
print('DONE 88 gift catalogue, VIP preview, 6 synthetic sound effects and safe updater version')
