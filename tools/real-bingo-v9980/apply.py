#!/usr/bin/env python3
"""Install genuine live 5x5 Bingo in existing signed KING Party APK source."""
from pathlib import Path
import re,shutil,sys
root=Path(sys.argv[1])
src=root/'app/src/main/java/com/kingplus/social'
here=Path(__file__).resolve().parent
room=(src/'RoomGameActivity.java');s=room.read_text()
anchor='''        controls.removeAllViews();'''
assert s.count(anchor)==1,'RoomGameActivity controls insertion changed'
button='''        // REAL multiplayer 5x5 Bingo, not legacy one-tap Pick Number 1..9.
        Button realBingo9980=button("🎱 LIVE BINGO 5×5 • Real Party Match");
        realBingo9980.setOnClickListener(v->{
            android.content.Intent play=new android.content.Intent(
                this, KingLiveBingo9980Activity.class);
            play.putExtra("roomId",roomId);play.putExtra("roomName",roomName);
            startActivity(play);
        });
        controls.addView(realBingo9980,new LinearLayout.LayoutParams(-1,dp(52)));
'''
s=s.replace(anchor,anchor+'\n'+button,1)
room.write_text(s)
manifest=root/'app/src/main/AndroidManifest.xml'
m=manifest.read_text()
anchor='''<activity android:name=".RoomGameActivity" android:exported="false" />'''
assert m.count(anchor)==1,'Expected existing RoomGame Activity manifest'
m=m.replace(anchor,anchor+'\n        <activity android:name=".KingLiveBingo9980Activity" android:exported="false" />',1)
manifest.write_text(m)
for name in ('KingBingoRules9980.java','KingLiveBingo9980Activity.java'):
    shutil.copyfile(here/name,src/name)
rules=root/'firestore.rules';x=rules.read_text()
anchor='''      match /game_moves/{playerUid} {'''
assert x.count(anchor)==1,'Expected live_rooms game_moves match anchor'
x=x.replace(anchor,(here/'bingo-firestore.rules').read_text()+'\n'+anchor,1)
rules.write_text(x)
gradle=root/'app/build.gradle';g=gradle.read_text()
g,n=re.subn(r"versionCode\s+258\s*;\s*versionName\s+'[^']+'",
    "versionCode 259; versionName '9.9.80-real-multiplayer-bingo-test'",g)
assert n==1,'Start only from signed full v9.9.79 Android source'
gradle.write_text(g)
assert 'KingPartyMoveSubscription9979.needsNewListener' in s
assert 'KingPartyAllPlayed9973.complete' in s
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
assert 'KingGiftPlayedHistory9969' in (src/'PartyActivity.java').read_text()
print('PASS genuine 5x5 Bingo full Android activity + Firestore room rules, preserving existing Voice and Gifts')
