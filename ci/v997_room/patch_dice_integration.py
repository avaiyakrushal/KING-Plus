#!/usr/bin/env python3
from pathlib import Path
import sys,shutil
base=Path(sys.argv[1])
pkg=base/'app/src/main/java/com/kingplus/social'
shutil.copyfile(Path(__file__).with_name('KingRoomDice997.java'),pkg/'KingRoomDice997.java')
f=pkg/'PartyActivity.java'
s=f.read_text()
anchor='    private void openRoomGames740(){'
assert s.count(anchor)==1
s=s.replace(anchor,'''    private void openInRoomDice997(){
        if(!cloudRoom||!memberSeen||db==null||user==null||roomId==null){
            toast("Join a live Firebase Party before playing");return;
        }
        KingRoomDice997.open(this,db,user,roomId,safeName(),this::openRoomGames740);
    }

'''+anchor,1)
old='''        roomRoot.addView(badges,new LinearLayout.LayoutParams(-1,dp(36)));
        if(!requestedGame940.isEmpty()){'''
new='''        roomRoot.addView(badges,new LinearLayout.LayoutParams(-1,dp(36)));
        TextView play997=pill("🎲 Play in Room  •  🎮 More Games",
            0x663e2b79,this::openInRoomDice997);
        play997.setTextSize(12);play997.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams playLp997=new LinearLayout.LayoutParams(-1,dp(34));
        playLp.setMargins(dp(16),dp(2),dp(16),dp(2));
        roomRoot.addView(play997,playLp997);
        if(!requestedGame940.isEmpty()){'''
assert s.count(old)==1
s=s.replace(old,new,1)
f.write_text(s)
g=base/'app/build.gradle'
code=g.read_text()
old="versionCode 185; versionName '9.9.6-people-follow-room-gifts'"
new="versionCode 186; versionName '9.9.7-room-admin-user-profile-live-dice'"
assert code.count(old)==1
g.write_text(code.replace(old,new,1))
print("PASS full Room Dice panel connected, 9.9.7 upgrade version, no Firebase Rule changes")
