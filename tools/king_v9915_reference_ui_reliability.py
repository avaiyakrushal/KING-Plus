#!/usr/bin/env python3
"""KING Plus ref parity pass: fix small-screen 3D, honest live UI and playable-game routing.
Bolo Hi APK is a comparison reference only; no proprietary assets/source copied.
"""
from pathlib import Path
import sys,re
root=Path(sys.argv[1])
sources=root/'app/src/main/java/com/kingplus/social'
def replace_once(text,needle,replacement,label):
    count=text.count(needle)
    if count!=1: raise AssertionError(f'{label}: expected one source anchor, got {count}')
    return text.replace(needle,replacement,1)

world=sources/'KingNative3DRoom.java'
s=world.read_text(encoding='utf-8')
s=replace_once(s,'import android.widget.LinearLayout;',
               'import android.widget.LinearLayout;\nimport android.widget.ScrollView;', 'scroll import')
s=replace_once(s,'        content.addView(stage,new LinearLayout.LayoutParams(-1,360*d));',
'''        // Prior fixed 360dp room + controls could clip the bottom of the dialog
        // on lower-height Android devices. Fit the stage and allow scrolling.
        final float roomDensity=Math.max(1f,host.getResources().getDisplayMetrics().density);
        final int availableDp=(int)(host.getResources().getDisplayMetrics().heightPixels/roomDensity);
        final int stageHeightDp=Math.max(180,Math.min(360,availableDp-335));
        content.addView(stage,new LinearLayout.LayoutParams(-1,stageHeightDp*d));''','responsive 3D viewport')
s=replace_once(s,'''        final AlertDialog dialog=new AlertDialog.Builder(host)
                .setTitle("🌐 3D World • Live Seats")
                .setView(content)''',
'''        final ScrollView responsiveWorld9915=new ScrollView(host);
        responsiveWorld9915.setFillViewport(false);
        responsiveWorld9915.setVerticalScrollBarEnabled(false);
        responsiveWorld9915.addView(content,new ScrollView.LayoutParams(-1,-2));
        final AlertDialog dialog=new AlertDialog.Builder(host)
                .setTitle("🌐 KING 3D World • Live Seats")
                .setView(responsiveWorld9915)''','accessible 3D actions')
world.write_text(s,encoding='utf-8')

party=sources/'PartyActivity.java'
p=party.read_text(encoding='utf-8')
p=replace_once(p,'''viewerLabel = pill("👤 1",0x33000000,this::membersDialog);''',
'''viewerLabel = pill("👥 …",0x33000000,this::membersDialog);''','no fake online count')
p=replace_once(p,'''roomLevelLabel=pill("💎 No."+compact,0xff8e24aa,()->openParity870("vip"));''',
'''roomLevelLabel=pill("👑 Room • syncing",0xff8e24aa,()->openParity870("vip"));''','no fake initial rank')
p=replace_once(p,'''heartLevelLabel=pill("💖 Lv. 1 Heart",0x26000000,()->openParity870("match"));''',
'''heartLevelLabel=pill("💖 Heart • syncing",0x26000000,()->openParity870("match"));''','no fake initial heart')
p=replace_once(p,'''else { viewerLabel.setText("👤 1"); fillLocalSeats();''',
'''else { viewerLabel.setText("👥 —"); fillLocalSeats();''','honest offline state')
p=replace_once(p,'''0x663e2b79,this::openInRoomDice997);''',
'''0x663e2b79,this::openPartyGameChooser9915);''','games choice affordance')
p=replace_once(p,'''    private void openInRoomDice997(){
''',
'''    // Distinguish a real Firestore room dice session from the separate game
    // catalog. Do not treat a decorative room button as an active multiplayer game.
    private void openPartyGameChooser9915(){
        if(!cloudRoom||!memberSeen||db==null||user==null||roomId==null){
            toast("Join a live Firebase Party to start room games");return;
        }
        new AlertDialog.Builder(this).setTitle("🎮 KING Party Games")
            .setItems(new String[]{"🎲 Live Room Dice","🎮 Multiplayer Games Catalog","🎯 Party Play Center"},
                (dialog,choice)->{
                    if(choice==0)openInRoomDice997();
                    else if(choice==1)openRoomGames740();
                    else toolsPanel();
                })
            .setNegativeButton("Close",null).show();
    }

    private void openInRoomDice997(){
''','room games routing')
# Remove random offline "PK scores" that looked like live multiplayer.
old='''if(db==null||!cloudRoom){int a=10+new java.util.Random().nextInt(91),b=10+new java.util.Random().nextInt(91);new AlertDialog.Builder(this).setTitle("⚔ Room Battle").setMessage("Team KING  "+a+"  vs  Guest  "+b+"\\n\\n"+(a>=b?"🏆 Team KING wins":"🏆 Guest wins")).setPositiveButton("Again",(d,w)->pkBattle()).setNegativeButton("Close",null).show();return;}'''
new='''if(db==null||!cloudRoom||!memberSeen||user==null||roomId==null){toast("Room Battle needs a joined live Firebase Party");return;}'''
p=replace_once(p,old,new,'no fabricated PK results')
party.write_text(p,encoding='utf-8')
gradle=root/'app/build.gradle'
g=gradle.read_text(encoding='utf-8')
g,n=re.subn(r"versionCode\s+193;\s*versionName\s+'9\.9\.14[^']*'",
            "versionCode 194; versionName '9.9.15-integrated-ui-reliability-test'",g)
if n!=1:raise AssertionError('Source must be verified 9.9.14 versionCode 193')
gradle.write_text(g,encoding='utf-8')
assert 'responsiveWorld9915' in s and 'openPartyGameChooser9915' in p
assert 'new java.util.Random().nextInt(91)' not in p
assert 'db.runTransaction(tx9912' in p and 'approveOrMoveMemberSeat9913' in p
print('PASS: responsive live 3D + real games navigation + no fabricated room scores + prior Party safety retained')
