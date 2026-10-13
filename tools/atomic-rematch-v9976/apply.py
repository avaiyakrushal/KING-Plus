#!/usr/bin/env python3
"""KING v9.9.76: restart 13 multiplayer games without deleting every historic room move.

Old RoomGameActivity.startRoundNow750() downloads ALL game_moves and tries to delete
ALL of them in one Firestore transaction. A long-lived Party with >500 stale
moves cannot start another real round (Firestore transaction write limits).
Only the locked 2-50 Ready participants need their previous per-UID move
document removed; other round/UID moves are harmless because every consumer
filters by exact game type, round ID and frozen roster.
"""
from pathlib import Path
import re,sys
root=Path(sys.argv[1]);src=root/'app/src/main/java/com/kingplus/social'
f=src/'RoomGameActivity.java';s=f.read_text()
opening='''        final long previous=roundId;
        room().collection("game_moves").get().addOnSuccessListener(old->{
            long next=Math.max(System.currentTimeMillis(),previous+1);'''
assert s.count(opening)==1,"expected full signed v9.9.75 start round"
s=s.replace(opening,'''        final long previous=roundId;
        // No unbounded collection read or hundreds of ghost-move deletions.
        // Only the verified Ready roster needs previous per-UID moves cleared.
        {
            long next=Math.max(System.currentTimeMillis(),previous+1);''',1)
old='''                // Finish every transaction read BEFORE performing a write.
                for(DocumentSnapshot move:old.getDocuments())tx.delete(move.getReference());
                tx.set(state(),value);return null;'''
new='''                // SERVER transaction reads of each participant's previous
                // move happen BEFORE ANY write. Each gets a fresh move slot
                // atomically with the new round. Ghost/spectator moves remain
                // harmless because they belong to a different round/roster.
                java.util.List<String> cleanup9976=
                    KingPartyMoveCleanup9976.roster(value.get("participantUids"));
                if(!KingPartyMoveCleanup9976.valid(cleanup9976))
                    throw new IllegalStateException("Live Ready roster unavailable for rematch");
                for(String uid9976:cleanup9976)
                    tx.get(room().collection("game_moves").document(uid9976));
                // At most 50 move deletes + one game_state write.
                for(String uid9976:cleanup9976)
                    tx.delete(room().collection("game_moves").document(uid9976));
                tx.set(state(),value);return null;'''
assert s.count(old)==1,"expected all-history delete in real transaction"
s=s.replace(old,new,1)
closing='''            }).addOnSuccessListener(v->postGameEvent750("🎮 "+displayName+" started "+game.toUpperCase()+" round"))
              .addOnFailureListener(e->toast("Start failed: "+e.getMessage()));
        }).addOnFailureListener(e->toast("Round preparation failed: "+e.getMessage()));
    }'''
assert s.count(closing)==1,"expected full signed v9.9.75 start round closing"
s=s.replace(closing,'''            }).addOnSuccessListener(v->postGameEvent750("🎮 "+displayName+" started "+game.toUpperCase()+" round"))
              .addOnFailureListener(e->toast("Start failed: "+e.getMessage()));
        }
    }''',1)
f.write_text(s)
(src/'KingPartyMoveCleanup9976.java').write_text('''package com.kingplus.social;
import java.util.*;
/** Targets only the frozen, real 2–50 Ready players, NEVER all room history. */
public final class KingPartyMoveCleanup9976 {
  private KingPartyMoveCleanup9976(){}
  public static List<String> roster(Object value){
    if(!(value instanceof List))return Collections.emptyList();
    List<?> raw=(List<?>)value;
    if(raw.size()<2||raw.size()>50)return Collections.emptyList();
    List<String> clean=new ArrayList<>();
    Set<String> unique=new HashSet<>();
    for(Object part:raw){
      if(!(part instanceof String))return Collections.emptyList();
      String uid=(String)part;
      if(uid.isEmpty()||!unique.add(uid))return Collections.emptyList();
      clean.add(uid);
    }
    return clean;
  }
  public static boolean valid(List<String> roster){return roster!=null&&roster.size()>=2&&roster.size()<=50;}
}
''')
g=root/'app/build.gradle';data=g.read_text()
data,n=re.subn(r"versionCode\s+254\s*;\s*versionName\s+'[^']+'",
               "versionCode 255; versionName '9.9.76-atomic-party-rematch-test'",data)
assert n==1,"expected signed v9.9.75 versionCode254 source"
g.write_text(data)
assert 'KingPartyLiveRoster9975.member' in s
assert 'KingPartyAllPlayed9973.complete' in s
party=(src/'PartyActivity.java').read_text()
assert 'KingGiftPlayedHistory9969' in party
assert 'KingGiftSound993.stopRoomAudio();' in party
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print("PASS full Android startRoundNow uses 2-50 verified players only; no all-moves wipe, no paid recharge")
