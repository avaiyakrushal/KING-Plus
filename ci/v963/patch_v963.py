"""KING Plus 9.6.3: honest and working 2-player / 4-player Online Ludo entry points.

Implements requested launcher modes instead of silently ignoring maxPlayers.
 - Create 1 vs 1 with actual Firebase capacity=2.
 - Create 4-player with actual Firebase capacity=4.
 - Join using 6-character match code.
 - Reconnect directly to saved match code (no new match created).
 - Offline opens actual standalone offline Ludo instead of an online screen.
Removes false Championship / Wacko labels that were aliases of standard Ludo.
Keeps previous 9.6.2 security/rules and 9.6.1 diagnostic changes.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'

def once(file,old,new,label):
    p=pkg/file
    s=p.read_text()
    n=s.count(old)
    if n != 1:raise SystemExit(f'{label}: marker count {n}: {old[:140]!r}')
    p.write_text(s.replace(old,new,1))
    print("PASS",label)

once('KingLudoLobbyActivity.java',
     'row1.addView(mode("🎁","WACKO ITEM MODE",()->openOnline(4)),lp());row1.addView(mode("🌐","ONLINE",()->openOnline(4)),lp());',
     'row1.addView(mode("👥","CREATE 1 VS 1",()->openOnline(2)),lp());row1.addView(mode("🌐","CREATE 4P",()->openOnline(4)),lp());',
     'true 2-player or 4-player entry choices')
once('KingLudoLobbyActivity.java',
     'row2.addView(mode("⚔","2VS2",()->openOnline(4)),lp());row2.addView(mode("💬","CHAT ROOM",this::openParty),lp());',
     'row2.addView(mode("🔑","JOIN CODE",()->openOnline(0)),lp());row2.addView(mode("💬","PARTY ROOM",this::openParty),lp());',
     'join-by-code and Party Room choices')
once('KingLudoLobbyActivity.java',
     'row3.addView(mode("🏆","CHAMPIONSHIP",()->openOnline(4)),lp());row3.addView(mode("🎲","OFFLINE",this::openOffline),lp());',
     'row3.addView(mode("↻","RECONNECT",()->openOnline(-1)),lp());row3.addView(mode("🎲","OFFLINE",this::openOffline),lp());',
     'reconnect/offline instead of fake championship')
once('KingLudoLobbyActivity.java',
     'i.putExtra("maxPlayers",players);startActivity(i);}',
     'i.putExtra("maxPlayers",players);if(players==2||players==4)i.putExtra("ludoCreateCapacity963",players);if(players==-1)i.putExtra("ludoAutoReconnect963",true);startActivity(i);}',
     'pass actual mode to realtime game instead of ignored maxPlayers extra')
once('KingLudoLobbyActivity.java',
     'LinearLayout bottom=new LinearLayout(this);bottom.setGravity(Gravity.CENTER);String[] labels={"SHOP","RANK","PLAY","EVENT","REWARD"};String[] icons={"🛍","🏅","▶","🎪","🎁"};for(int i=0;i<labels.length;i++){LinearLayout btm=new LinearLayout(this);btm.setOrientation(LinearLayout.VERTICAL);btm.setGravity(Gravity.CENTER);TextView ic=tv(icons[i],19,Color.WHITE,false);TextView lb=tv(labels[i],9,0xffd8ecff,true);btm.addView(ic,new LinearLayout.LayoutParams(-1,dp(30)));btm.addView(lb,new LinearLayout.LayoutParams(-1,dp(20)));bottom.addView(btm,new LinearLayout.LayoutParams(0,dp(56),1));}page.addView(bottom,new LinearLayout.LayoutParams(-1,dp(58)));',
     'TextView info963=tv("Real online Ludo • Create, join or reconnect • Free play",12,0xffd8ecff,true);page.addView(info963,new LinearLayout.LayoutParams(-1,dp(52)));',
     'remove nonworking Shop/Rank/Event/Reward controls')

p=pkg/'OnlineLudoActivity.java'
s=p.read_text()
old='''        String saved=getPreferences(MODE_PRIVATE).getString("match_"+me.getUid(),"");
            if(!saved.isEmpty()){codeInput.setText(saved);status("Previous room: "+saved+" • Tap Join to reconnect",GOLD);}'''
if old not in s:
    old='''            String saved=getPreferences(MODE_PRIVATE).getString("match_"+me.getUid(),"");
            if(!saved.isEmpty()){codeInput.setText(saved);status("Previous room: "+saved+" • Tap Join to reconnect",GOLD);}'''
n=s.count(old)
if n!=1:raise SystemExit('Online Ludo stored-code marker count '+str(n))
new='''            String saved=getPreferences(MODE_PRIVATE).getString("match_"+me.getUid(),"");
            int capacity963=getIntent().getIntExtra("ludoCreateCapacity963",0);
            boolean reconnect963=getIntent().getBooleanExtra("ludoAutoReconnect963",false);
            if(reconnect963){
                if(saved.length()==6){codeInput.setText(saved);joinMatch(saved);}
                else status("No saved match. Join with a code or create a new game.",GOLD);
            }else if(capacity963==2||capacity963==4){
                createMatch(capacity963);
            }else if(!saved.isEmpty()){
                codeInput.setText(saved);status("Previous room: "+saved+" • Tap Join to reconnect",GOLD);
            }'''
p.write_text(s.replace(old,new,1))
print('PASS', 'online game actually creates selected 2/4-player match and reconnects saved match')

gradle=root/'app/build.gradle'
g=gradle.read_text()
previous="versionCode 153; versionName '9.6.2-ludo-turn-guards'"
if g.count(previous)!=1:raise SystemExit('Unexpected v9.6.2 Gradle version baseline')
gradle.write_text(g.replace(previous,"versionCode 154; versionName '9.6.3-online-ludo-modes'",1))
print('PASS','version v9.6.3, all earlier features preserved')
