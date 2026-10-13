#!/usr/bin/env python3
"""v9.9.52: prevent ghost Ready players from locking real RPS/Dice/Coin games."""
from pathlib import Path
import sys
r=Path(sys.argv[1]);src=r/'app/src/main/java/com/kingplus/social'
p=src/'RoomGameActivity.java';s=p.read_text()
old='''            db.runTransaction(tx->{
                DocumentSnapshot current=tx.get(state());
                if(current.exists()){
                    Long storedRound9927=current.getLong("roundId");
                    if("active".equals(current.getString("status"))||storedRound9927==null||storedRound9927.longValue()!=previous)
                        throw new IllegalStateException("Round changed. Reopen the controls.");
                }
                for(DocumentSnapshot move:old.getDocuments())tx.delete(move.getReference());
                tx.set(state(),value);return null;
            }).addOnSuccessListener(v->postGameEvent750("🎮 "+displayName+" started "+game.toUpperCase()+" round"))
'''
new='''            db.runTransaction(tx->{
                DocumentSnapshot current=tx.get(state());
                if(current.exists()){
                    Long storedRound9927=current.getLong("roundId");
                    if("active".equals(current.getString("status"))||storedRound9927==null||storedRound9927.longValue()!=previous)
                        throw new IllegalStateException("Round changed. Reopen the controls.");
                }
                // A previous SERVER Ready list can still contain a departed user.
                // Fresh transaction reads are retried if a player leaves/unreadies.
                if(KingRoomRoster9946.lockedGame(game)){
                    java.util.ArrayList<String> activeReady9952=new java.util.ArrayList<>();
                    for(String candidate9952:frozenPlayers9946){
                        if(candidate9952==null||candidate9952.isEmpty())continue;
                        DocumentSnapshot actualReady9952=tx.get(
                            room().collection("game_ready").document(candidate9952));
                        DocumentSnapshot actualMember9952=tx.get(
                            room().collection("members").document(candidate9952));
                        if(KingLivePartyRoster9952.isActiveReady(candidate9952,
                            actualReady9952.exists(),s(actualReady9952.getString("uid")),
                            actualMember9952.exists(),s(actualMember9952.getString("uid"))))
                            activeReady9952.add(candidate9952);
                    }
                    if(!KingRoomRoster9946.valid(activeReady9952))
                        throw new IllegalStateException(
                            "Need two real Ready Party players • ask members to tap Ready");
                    value.put("participantUids",activeReady9952);
                }
                // Finish every transaction read BEFORE performing a write.
                for(DocumentSnapshot move:old.getDocuments())tx.delete(move.getReference());
                tx.set(state(),value);return null;
            }).addOnSuccessListener(v->postGameEvent750("🎮 "+displayName+" started "+game.toUpperCase()+" round"))
'''
assert s.count(old)==1,'wrong verified v9.9.51 room game source'
p.write_text(s.replace(old,new,1))
(src/'KingLivePartyRoster9952.java').write_text("""package com.kingplus.social;
/** Real SERVER-verified Room membership and Ready for a new multiplayer round. */
public final class KingLivePartyRoster9952 {
 private KingLivePartyRoster9952(){}
 public static boolean isActiveReady(String expected,boolean readyExists,String readyUid,
                                      boolean memberExists,String memberUid){
   return expected!=null&&!expected.isEmpty()&&readyExists&&memberExists
       &&expected.equals(readyUid)&&expected.equals(memberUid);
 }
}
""")
g=r/'app/build.gradle';v=g.read_text()
oldversion="versionCode 230; versionName '9.9.51-real-ttt-party-membership-test'"
assert v.count(oldversion)==1
g.write_text(v.replace(oldversion,"versionCode 231; versionName '9.9.52-live-ready-game-roster-test'"))
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS real Room RPS/Dice/Coin roster now cross-checked with live Party members')
