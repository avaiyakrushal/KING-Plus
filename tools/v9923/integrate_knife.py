#!/usr/bin/env python3
"""KING Plus v9.9.23: integrate original interactive flying knife into actual Party."""
from pathlib import Path
import sys,re
root=Path(sys.argv[1]);src=root/"app/src/main/java/com/kingplus/social"
tools=Path("tools/v9923/java")
for name in ("KingKnifeRules9923.java","KingKnifeArena9923Activity.java"):
 p=tools/name
 assert p.is_file(),p
 (src/name).write_text(p.read_text(encoding="utf-8"),encoding="utf-8")
def once(path,old,new,label):
 s=path.read_text(encoding="utf-8");n=s.count(old)
 if n!=1:raise AssertionError(f"{label} anchor expected once, got {n}")
 path.write_text(s.replace(old,new,1),encoding="utf-8")
manifest=root/"app/src/main/AndroidManifest.xml"
once(manifest,'        <activity android:name=".KingLiveChessActivity" android:exported="false" />',
'''        <activity android:name=".KingLiveChessActivity" android:exported="false" />
        <activity android:name=".KingKnifeArena9923Activity" android:exported="false" />''',
"manifest knife screen")
party=src/"PartyActivity.java"
once(party,
  'new String[]{"🎲 Live Room Dice","♟ Live Room Chess","🎮 Multiplayer Games Catalog","🎯 Party Play Center"}',
  'new String[]{"🎲 Live Room Dice","♟ Live Room Chess","🔪 Flying Knife Arena","🎮 Multiplayer Games Catalog","🎯 Party Play Center"}',
  "party game chooser")
once(party,
'''                    if(choice==0)openInRoomDice997();
                    else if(choice==1)openLiveChess9917();
                    else if(choice==2)openRoomGames740();
                    else toolsPanel();''',
'''                    if(choice==0)openInRoomDice997();
                    else if(choice==1)openLiveChess9917();
                    else if(choice==2)openLiveKnife9923();
                    else if(choice==3)openRoomGames740();
                    else toolsPanel();''',"party game routing")
once(party,
'    private void openLiveChess9917(){',
'''    private void openLiveKnife9923(){
        if(!cloudRoom||!memberSeen||db==null||user==null||roomId==null){
            toast("Join a live Party to play Flying Knife");return;
        }
        Intent knife9923=new Intent(this,KingKnifeArena9923Activity.class);
        knife9923.putExtra("roomId",roomId);
        startActivity(knife9923);
    }

    private void openLiveChess9917(){''',"party knife launcher")
games=src/"RoomGameActivity.java"
once(games,
'controls.addView(chess9917,chessLp9917);',
'''controls.addView(chess9917,chessLp9917);
        Button flying9923=button("🔪 Flying Knife Arena • LIVE Party Score");
        flying9923.setOnClickListener(v->{
            if(!gameMemberVerified992||db==null||me==null||roomId.isEmpty()){
                toast("Join the Party to play Flying Knife");return;
            }
            Intent knife9923=new Intent(this,KingKnifeArena9923Activity.class);
            knife9923.putExtra("roomId",roomId);
            startActivity(knife9923);
        });
        LinearLayout.LayoutParams flp9923=new LinearLayout.LayoutParams(-1,dp(52));
        flp9923.setMargins(0,dp(4),0,dp(4));controls.addView(flying9923,flp9923);''',
"Room Game Center Knife")
firestore=root/"firestore.rules"
new_rules='''      // Original interactive KING Flying Knife Arena: private account-owned
      // high scores visible to authenticated live room members. Client reports
      // skill results. Competitive anti-cheat would need a trusted game server.
      match /knife_duel_scores/{playerUid} {
        allow read: if roomAccess(roomId) && roomMember(roomId);
        allow create: if roomAccess(roomId) && roomMember(roomId)
          && playerUid == request.auth.uid
          && request.resource.data.keys().hasOnly([
            'uid','name','bestScore','lastScore','plays','updatedAt'
          ])
          && request.resource.data.uid == playerUid
          && request.resource.data.name is string
          && request.resource.data.name.size() > 0
          && request.resource.data.name.size() <= 40
          && request.resource.data.bestScore is int
          && request.resource.data.bestScore >= 0
          && request.resource.data.bestScore <= 7
          && request.resource.data.bestScore == request.resource.data.lastScore
          && request.resource.data.plays == 1
          && request.resource.data.updatedAt == request.time;
        allow update: if roomAccess(roomId) && roomMember(roomId)
          && playerUid == request.auth.uid
          && resource.data.uid == playerUid
          && request.resource.data.uid == resource.data.uid
          && request.resource.data.diff(resource.data).affectedKeys().hasOnly([
            'name','bestScore','lastScore','plays','updatedAt'
          ])
          && request.resource.data.name is string
          && request.resource.data.name.size() > 0
          && request.resource.data.name.size() <= 40
          && request.resource.data.lastScore is int
          && request.resource.data.lastScore >= 0
          && request.resource.data.lastScore <= 7
          && request.resource.data.bestScore is int
          && request.resource.data.bestScore ==
            (request.resource.data.lastScore > resource.data.bestScore
              ? request.resource.data.lastScore : resource.data.bestScore)
          && request.resource.data.plays == resource.data.plays + 1
          && request.resource.data.updatedAt == request.time;
        allow delete: if false;
      }

'''
once(firestore,'      match /chess_games/{chessId} {',new_rules+'      match /chess_games/{chessId} {',
     "firestore Knife owned leaderboard")
gradle=root/"app/build.gradle";g=gradle.read_text(encoding="utf-8")
g,n=re.subn(r"versionCode\s+201;\s*versionName\s+'9\.9\.22[^']*'",
            "versionCode 202; versionName '9.9.23-knife-party-live-test'",g)
assert n==1,"Expected exact v9.9.22 release source and versionCode 201"
gradle.write_text(g,encoding="utf-8")
for token in ['openLiveKnife9923','openLiveChess9917','approveOrMoveMemberSeat9913',
              'whereEqualTo("uid",leavingUid9919)','muteOwnedSeat9920','chatRoom9918']:
 assert token in party.read_text(encoding="utf-8"),token
assert "videoMatchIncoming9922" in (src/"KingMultiVideoActivity.java").read_text()
assert (src/"KingLiveChessActivity.java").exists()
assert "PAID_VIP_ACTIVE = false" in (src/"KingPremiumAccess981.java").read_text()
assert "LIVE_RECHARGE = false" in (src/"KingRecharge974Activity.java").read_text()
print("PASS original Flying Knife playable screen, secured leaderboard, Game/Party routes and previous 3D/Chess/Video features")
