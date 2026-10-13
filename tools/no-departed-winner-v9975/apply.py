#!/usr/bin/env python3
"""KING v9.9.75: do not publish winner after a frozen player leaves Party."""
from pathlib import Path
import re,sys
root=Path(sys.argv[1]);base=root/'app/src/main/java/com/kingplus/social'; file=base/'RoomGameActivity.java'
s=file.read_text()
anchor='''                                DocumentSnapshot currentMove9973=tx.get(
                                  room().collection("game_moves").document(uid9973));'''
assert s.count(anchor)==1,'Expected actual v9974 game transaction'
s=s.replace(anchor,'''                                // Immutable move persists after the sender leaves:
                                // also re-read real Party membership in this SAME
                                // winner transaction or do not declare a winner.
                                DocumentSnapshot liveMember9975=tx.get(
                                  room().collection("members").document(uid9973));
                                if(!KingPartyLiveRoster9975.member(uid9973,
                                      liveMember9975.exists(),
                                      s(liveMember9975.getString("uid"))))
                                    throw new IllegalStateException(
                                      "Ready player left Party • Cancel without winner");
                                DocumentSnapshot currentMove9973=tx.get(
                                  room().collection("game_moves").document(uid9973));''',1)
anchor='''                    boolean finished9973=KingPartyAllPlayed9973.complete(type,
                        frozenRoster9947,verifiedMoves9973);
                    Button finish=button(finished9973?
                        "🏁 All players finished • show result":
                        "⏳ Waiting for every Ready player's move");
                    finish.setEnabled(finished9973&&gameStateVerified9943);'''
assert s.count(anchor)==1, 'Expected existing all-players Finish button'
s=s.replace(anchor,'''                    boolean rosterLive9975=membersSeen9971&&
                        KingPartyLiveRoster9975.allPresent(
                            frozenRoster9947,memberNames9971.keySet());
                    boolean finished9973=KingPartyAllPlayed9973.complete(type,
                        frozenRoster9947,verifiedMoves9973);
                    Button finish=button(!rosterLive9975&&membersSeen9971?
                        "🚫 Player left • Cancel round (no winner)":
                        finished9973?
                        "🏁 All players finished • show result":
                        "⏳ Waiting for every Ready player's move");
                    finish.setEnabled(finished9973&&rosterLive9975&&gameStateVerified9943);''',1)
anchor='''            renderVerifiedReady9971();
          });
    }
    /** Explicit Ready toggle:'''
assert s.count(anchor)==1,'Expected existing members listener'
s=s.replace(anchor,'''            renderVerifiedReady9971();
            if("active".equals(status)&&KingPartyRoundRoster9972.isOrdinaryGame(type))
                renderControls(); // refresh winner gate when Party member leaves
          });
    }
    /** Explicit Ready toggle:''',1)
anchor='''                    }).addOnFailureListener(e->{
                        settleFailed9954(automatic9954,expected,expectedType);
                        if(!automatic9954)toast("Finish failed: "+e.getMessage());
                    });'''
assert s.count(anchor)==1,'Expected transaction failure callback'
s=s.replace(anchor,'''                    }).addOnFailureListener(e->{
                        String error9975=e==null?"":String.valueOf(e.getMessage());
                        if(error9975.contains("Ready player left Party")){
                            autoSettlementInFlight9954=false;
                            clearScheduledSettlement9954();
                            lastAutoSettlementAttempt9953=expected;
                            toast("A player left • Host: Cancel without winner");
                            return;
                        }
                        settleFailed9954(automatic9954,expected,expectedType);
                        if(!automatic9954)toast("Finish failed: "+error9975);
                    });''',1)
file.write_text(s)
(base/'KingPartyLiveRoster9975.java').write_text('''package com.kingplus.social;
import java.util.*;
/** A participant who already submitted a score still cannot win after leaving. */
public final class KingPartyLiveRoster9975 {
 private KingPartyLiveRoster9975(){}
 public static boolean member(String uid,boolean exists,String storedUid){
   return uid!=null&&!uid.trim().isEmpty()&&exists&&uid.equals(storedUid);
 }
 public static boolean allPresent(Collection<String> frozen,Collection<String> present){
   if(frozen==null||present==null||frozen.size()<2||frozen.size()>50)return false;
   Set<String> unique=new HashSet<>();
   for(String uid:frozen){
     if(uid==null||uid.isEmpty()||!unique.add(uid)||!present.contains(uid))return false;
   }
   return true;
 }
}
''')
gradle=root/'app/build.gradle';g=gradle.read_text()
g,n=re.subn(r"versionCode\s+253\s*;\s*versionName\s+'[^']+'",
  "versionCode 254; versionName '9.9.75-no-departed-winner-test'",g)
assert n==1,'Expected signed v9.9.74 source'
gradle.write_text(g)
assert 'LIVE_RECHARGE = false' in (base/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (base/'KingPremiumAccess981.java').read_text()
print('PASS game transaction blocks missing original players; prior 13 modes and gifts unchanged')
