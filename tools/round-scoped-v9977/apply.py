#!/usr/bin/env python3
"""v9.9.77: read only the ACTUAL live Party round instead of all room game history.

A long-running 2-player room may contain hundreds or thousands of old
game_moves documents. Prior RoomGameActivity listened to ALL of them on both
phones and downloaded ALL again for every Finish/automatic result. Keeping
old records is intentional since 9.9.76; don't make Game screens unusable.
"""
from pathlib import Path
import re, sys
root=Path(sys.argv[1])
source=root/'app/src/main/java/com/kingplus/social/RoomGameActivity.java'
s=source.read_text()
old='''    private void listenMoves(){
        if(movesListener!=null)movesListener.remove();
        movesListener=room().collection("game_moves")
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(q,e)->{
                currentMoves.clear();'''
new='''    private void listenMoves(){
        if(movesListener!=null){movesListener.remove();movesListener=null;}
        currentMoves.clear();
        // The previous listener downloaded ALL historic departed players'
        // scores on EVERY phone, even after version 9.9.76 preserved those
        // scores intentionally. Keep only this SERVER round's real moves.
        if(roundId<=0){renderMoves();return;}
        final long observingRound9977=roundId;
        movesListener=room().collection("game_moves")
            .whereEqualTo("roundId",observingRound9977)
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(q,e)->{
                // A delayed callback from a removed earlier-round listener
                // must never repaint or auto-settle a newly started game.
                if(observingRound9977!=roundId||!gameMemberVerified992
                   ||isFinishing()||isDestroyed())return;
                currentMoves.clear();'''
assert s.count(old)==1,'Actual v9.9.76 live listener unexpectedly changed'
s=s.replace(old,new,1)
old='''        room().collection("game_moves")
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(q->{'''
new='''        // Finishing must fetch this round's SERVER moves only: 610+ ghost
        // history records must not stall a genuine multiplayer winner.
        room().collection("game_moves")
            .whereEqualTo("roundId",expected)
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(q->{'''
assert s.count(old)==1,'Actual v9.9.76 Finish round query unexpectedly changed'
s=s.replace(old,new,1)
# This unsafe, unused helper would delete every previous historical score
# untransactionally. Remove rather than leaving a future regression trap.
old='''    private void clearOldMoves(long keep){room().collection("game_moves").get().addOnSuccessListener(q->{for(DocumentSnapshot d:q.getDocuments()){Long r=d.getLong("roundId");if(r==null||r!=keep)d.getReference().delete();}});}
'''
assert s.count(old)==1 and s.count('clearOldMoves(')==1,'Unexpected old-move cleanup usage'
s=s.replace(old,'',1)
source.write_text(s)
gradle=root/'app/build.gradle'
g=gradle.read_text()
g,n=re.subn(r"versionCode\s+255\s*;\s*versionName\s+'[^']+'",
  "versionCode 256; versionName '9.9.77-live-round-only-moves-test'",g)
assert n==1,'Requires exact last successful signed v9.9.76'
gradle.write_text(g)
assert 'KingPartyMoveCleanup9976.roster' in s
assert 'KingPartyLiveRoster9975.member' in s
assert 'KingPartyAllPlayed9973.complete' in s
party=(root/'app/src/main/java/com/kingplus/social/PartyActivity.java').read_text()
assert 'KingGiftPlayedHistory9969' in party
assert 'KingGiftSound993.stopRoomAudio();' in party
assert 'LIVE_RECHARGE = false' in (root/'app/src/main/java/com/kingplus/social/KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (root/'app/src/main/java/com/kingplus/social/KingPremiumAccess981.java').read_text()
assert s.count('.whereEqualTo("roundId",observingRound9977)')==1
assert s.count('.whereEqualTo("roundId",expected)')==1
print("PASS live Party listener and Host winner read only current round; old scores not deleted; vCode256")
