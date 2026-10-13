#!/usr/bin/env python3
"""v9.9.40: Actually measure KING Sheep Sprint with a Firebase start timestamp.

Prevents early score submission, stale/other-round scores and tampering with
round type, backed by Firestore emulator tests. Independent original KING code;
does not claim a client tap count is cryptographically tamper-proof.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1])
src=root/'app/src/main/java/com/kingplus/social'
java=src/'RoomGameActivity.java'
txt=java.read_text()
def replace(old,new,occ=1):
 global txt
 count=txt.count(old)
 if count!=occ: raise RuntimeError("RoomGameActivity.java expected %d match(es), saw %d for %s"%(occ,count,old[:110]))
 txt=txt.replace(old,new,occ)

replace("    private boolean sheepFinished9925=false;","    private boolean sheepFinished9925=false;\n    private boolean sheepStartPending9940=false;")
replace('''            if(!"sheep".equals(type)||!"active".equals(status)||roundId!=sheepRound9925){
                sheepStart9925=0;sheepClock9925.removeCallbacks(this);return;''',
'''            if(!gameMemberVerified992||!"sheep".equals(type)||!"active".equals(status)||roundId!=sheepRound9925){
                sheepStart9925=0;sheepClock9925.removeCallbacks(this);return;''')
replace('''            sheepRound9925=roundId;sheepStart9925=0L;sheepTaps9925=0;sheepFinished9925=false;''',
'''            sheepRound9925=roundId;sheepStart9925=0L;sheepTaps9925=0;sheepFinished9925=false;sheepStartPending9940=false;''')
replace('''        sheepTap9925.setTextSize(18);sheepTap9925.setEnabled(!sheepFinished9925);''',
'''        sheepTap9925.setTextSize(18);sheepTap9925.setEnabled(!sheepFinished9925&&!sheepStartPending9940);''')
replace('''            if(sheepStart9925==0){
                sheepStart9925=now;sheepTaps9925=0;sheepTap9925.setText("🐑 TAP • RUN FASTER!");
                sheepClock9925.removeCallbacks(sheepTick9925);sheepClock9925.post(sheepTick9925);
                return;
            }''',
'''            if(sheepStart9925==0){
                if(sheepStartPending9940||db==null||me==null)return;
                sheepStartPending9940=true;
                sheepTap9925.setEnabled(false);
                sheepLive9925.setText("⏳ Starting verified 8-second race on Party server…");
                final long startingRound9940=roundId;
                final String startUid9940=me.getUid();
                java.util.Map<String,Object> proof9940=new java.util.HashMap<>();
                proof9940.put("uid",startUid9940);
                proof9940.put("roundId",startingRound9940);
                proof9940.put("startedAt",FieldValue.serverTimestamp());
                room().collection("sheep_starts").document(startUid9940).set(proof9940)
                  .addOnSuccessListener(done->{
                    sheepStartPending9940=false;
                    if(isFinishing()||isDestroyed()||!gameMemberVerified992||
                       sheepRound9925!=startingRound9940||roundId!=startingRound9940||
                       !"sheep".equals(type)||!"active".equals(status))return;
                    sheepStart9925=android.os.SystemClock.elapsedRealtime();
                    sheepTaps9925=0;
                    if(sheepTap9925!=null){
                        sheepTap9925.setEnabled(true);
                        sheepTap9925.setText("🐑 TAP • RUN FASTER!");
                    }
                    sheepClock9925.removeCallbacks(sheepTick9925);
                    sheepClock9925.post(sheepTick9925);
                  }).addOnFailureListener(e->{
                    sheepStartPending9940=false;
                    if(isFinishing()||isDestroyed()||roundId!=startingRound9940)return;
                    if(sheepTap9925!=null){sheepTap9925.setEnabled(true);
                        sheepTap9925.setText("▶ RETRY START");}
                    if(sheepLive9925!=null)sheepLive9925.setText("Could not start room race: "+e.getMessage());
                  });
                return;
            }''')
replace('''        if(roundId==0||!"active".equals(status)||me==null||!KingNetwork.online(this)){toast("An active round and internet connection are required");return;}''',
'''        if(roundId==0||!"active".equals(status)||me==null||!gameMemberVerified992||!KingNetwork.online(this)){toast("A verified active Party member and internet are required");return;}''')
# Cancel countdown callbacks on removal. No late score after a kick.
replace('''                gameMemberVerified992=false;moderator=false;''',
'''                sheepClock9925.removeCallbacks(sheepTick9925);
                sheepStart9925=0L;
                sheepStartPending9940=false;
                gameMemberVerified992=false;moderator=false;''')
java.write_text(txt)

rules=root/'firestore.rules'
s=rules.read_text()
old="""      match /game_moves/{playerUid} {"""
new="""      // Real KING Sheep Sprint: record a SERVER timestamp for the current
      // active room round before any local eight-second race may begin.
      match /sheep_starts/{playerUid} {
        allow read: if roomAccess(roomId) && roomMember(roomId)
          && playerUid == request.auth.uid;
        function validSheepStart9940() {
          return roomAccess(roomId) && roomMember(roomId)
            && playerUid == request.auth.uid
            && request.resource.data.keys().hasOnly(['uid','roundId','startedAt'])
            && request.resource.data.uid == request.auth.uid
            && request.resource.data.roundId is int
            && request.resource.data.startedAt == request.time
            && exists(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current)
            && get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.type == 'sheep'
            && get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.status == 'active'
            && get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.roundId == request.resource.data.roundId;
        }
        allow create: if validSheepStart9940();
        allow update: if validSheepStart9940()
          && resource.data.uid == request.auth.uid
          && resource.data.roundId != request.resource.data.roundId;
        allow delete: if roomModerator(roomId);
      }

      match /game_moves/{playerUid} {"""
if s.count(old)!=1:raise RuntimeError("sheep_starts insertion anchor count changed")
s=s.replace(old,new,1)
old2="""          && playerUid == request.auth.uid
          && request.resource.data.uid == request.auth.uid
          // For an active Tic Tac Toe match"""
new2="""          && playerUid == request.auth.uid
          && request.resource.data.uid == request.auth.uid
          // Sheep cannot disguise a move as Dice/RPS to bypass the round timer.
          && (!exists(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current)
              || get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.get('type','') != 'sheep'
              || request.resource.data.get('type','') == 'sheep')
          && (request.resource.data.get('type','') != 'sheep'
              || (exists(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current)
                  && get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.type == 'sheep'
                  && get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.status == 'active'
                  && get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.roundId == request.resource.data.roundId
                  && request.resource.data.get('score',-1) is int
                  && request.resource.data.score >= 0
                  && request.resource.data.score <= 200
                  && request.resource.data.get('move','').matches('^Sheep sprint [0-9]{1,3} taps in 8s$')
                  && exists(/databases/$(database)/documents/live_rooms/$(roomId)/sheep_starts/$(playerUid))
                  && get(/databases/$(database)/documents/live_rooms/$(roomId)/sheep_starts/$(playerUid)).data.roundId == request.resource.data.roundId
                  && get(/databases/$(database)/documents/live_rooms/$(roomId)/sheep_starts/$(playerUid)).data.startedAt <= request.time - duration.value(8,'s')))
          // For an active Tic Tac Toe match"""
if s.count(old2)!=1:raise RuntimeError("game_moves anchor count changed")
s=s.replace(old2,new2,1)
rules.write_text(s)
gradle=root/'app/build.gradle'
v=gradle.read_text();base="versionCode 218; versionName '9.9.39-party-ludo-knife-membership-test'"
if v.count(base)!=1:raise RuntimeError("Expected verified base KING v9.9.39")
gradle.write_text(v.replace(base,"versionCode 219; versionName '9.9.40-verified-sheep-race-test'"))
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print("PASS server started Sheep round, eight-second wait, rejoin/cancel guard, zero-value games")
