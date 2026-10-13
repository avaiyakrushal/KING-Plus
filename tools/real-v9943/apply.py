#!/usr/bin/env python3
"""KING Plus v9.9.43 — actual game move integrity, not decorative resources.

Locks live Party game moves server-side to one write per user/round; prevents
wrong-game moves, stale rounds, score edits, cached-result calculation. Does not
claim original Bolo Hi game parity or eliminate client dice/tap cheating.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1]);src=root/'app/src/main/java/com/kingplus/social'
g=src/'RoomGameActivity.java';j=g.read_text()
def game_patch(old,new):
 global j
 if j.count(old)!=1:raise RuntimeError(f'RoomGameActivity expected one occurrence, saw {j.count(old)}: {old[:180]}')
 j=j.replace(old,new,1)
game_patch(
  '''private void finishRound(){if(!moderator||roundId==0||!"active".equals(status))return;final long expected=roundId;room().collection("game_moves").get().addOnSuccessListener(q->{List<DocumentSnapshot> a=new ArrayList<>();for(DocumentSnapshot d:q.getDocuments()){Long r=d.getLong("roundId");if(r!=null&&r==roundId)a.add(d);}''',
  '''private void finishRound(){
        if(!gameMemberVerified992||!moderator||roundId==0||!"active".equals(status)){
            toast("Join an active room round before finishing");return;
        }
        final long expected=roundId;
        final String expectedType=type;
        // Only SERVER-committed moves for this exact game and round are scored.
        // Cached/offline local moves are never announced as a verified winner.
        room().collection("game_moves").get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(q->{
            List<DocumentSnapshot> a=new ArrayList<>();
            for(DocumentSnapshot d:q.getDocuments()){
                Long r=d.getLong("roundId");
                if(r!=null&&r==expected&&expectedType.equals(d.getString("type"))
                   &&d.getString("uid")!=null&&d.getString("uid").equals(d.getId())
                   &&!d.getMetadata().hasPendingWrites())
                    a.add(d);
            }''')
game_patch(
 '''if(expected!=roundId||!"active".equals(status)){toast("Round changed");return;}if(a.size()<2)''',
 '''if(expected!=roundId||!expectedType.equals(type)||!"active".equals(status)){toast("Round changed; reload Game");return;}if(a.size()<2)''')
game_patch(
 '''if(!latest.exists()||latest.getLong("roundId")!=expected||!"active".equals(latest.getString("status")))throw new IllegalStateException("Round already finished");''',
 '''if(!latest.exists()||latest.getLong("roundId")!=expected
                   ||!expectedType.equals(latest.getString("type"))
                   ||!"active".equals(latest.getString("status")))
                    throw new IllegalStateException("Round changed or already finished");''')
# A client may show moves saved in local cache, but only treat server-confirmed
# game state as "connected" and enable move submissions after reconnect.
game_patch(
 '''    private boolean gameMemberVerified992=false;''',
 '''    private boolean gameMemberVerified992=false;
    private boolean gameStateVerified9943=false;''')
game_patch(
 '''private void finishRole(){roleText.setText(moderator?"👑 Host / Co-host • start rounds when players are ready":"👤 Player • tap Ready, then join the active round");if(syncSpinner950!=null)syncSpinner950.setVisibility(View.GONE);if(connection950!=null){connection950.setText("● Realtime room game connected");connection950.setTextColor(0xff55e2a4);}attach();listenReady750();renderControls();}''',
 '''private void finishRole(){roleText.setText(moderator?"👑 Host / Co-host • start rounds when players are ready":"👤 Player • tap Ready, then join the active round");gameStateVerified9943=false;
        if(connection950!=null){connection950.setText("● Checking live round on Firebase SERVER");connection950.setTextColor(0xffffd768);}
        attach();listenReady750();renderControls();}''')
game_patch(
 '''private void attach(){if(stateListener!=null)stateListener.remove();stateListener=state().addSnapshotListener((d,e)->{if(e!=null){stateText.setText("Game state error: "+e.getMessage());return;}if(d==null||!d.exists())''',
 '''private void attach(){if(stateListener!=null)stateListener.remove();stateListener=state().addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(d,e)->{
        gameStateVerified9943=e==null&&d!=null&&!d.getMetadata().isFromCache()&&!d.getMetadata().hasPendingWrites();
        if(!gameStateVerified9943){
            if(connection950!=null){connection950.setText("● Game reconnecting — waiting for SERVER");
                connection950.setTextColor(0xffffd768);}
            if(syncSpinner950!=null)syncSpinner950.setVisibility(View.VISIBLE);
            if(e!=null&&stateText!=null)stateText.setText("Game state error: "+e.getMessage());
            return;
        }
        if(connection950!=null){connection950.setText("● Game round verified by Firebase SERVER");connection950.setTextColor(0xff55e2a4);}
        if(syncSpinner950!=null)syncSpinner950.setVisibility(View.GONE);
        if(!d.exists())''')
game_patch(
 '''if(me==null||db==null||!"ttt".equals(type)||!"active".equals(status)||idx<0||idx>8){return;}''',
 '''if(me==null||db==null||!gameMemberVerified992||!gameStateVerified9943||
            !"ttt".equals(type)||!"active".equals(status)||idx<0||idx>8){
            toast("Reconnecting — wait for the live Party turn");return;}''')
# generic game submit uses transactional server checks, but must not allow new
# submissions from cached UI during reconnection.
game_patch(
 '''if(roundId==0||!"active".equals(status)||me==null||!gameMemberVerified992||!KingNetwork.online(this)){toast("A verified active Party member and internet are required");return;}''',
 '''if(roundId==0||!"active".equals(status)||me==null||!gameMemberVerified992
           ||!gameStateVerified9943||!KingNetwork.online(this)){
           toast("Party Game reconnecting — wait for verified round");return;}''')
game_patch(
 '''                gameMemberVerified992=false;moderator=false;''',
 '''                gameMemberVerified992=false;gameStateVerified9943=false;moderator=false;''')
g.write_text(j)
rules=root/'firestore.rules';text=rules.read_text()
def rules_patch(old,new):
 global text
 if text.count(old)!=1:raise RuntimeError(f'Rules anchor mismatch {text.count(old)}: {old[:140]}')
 text=text.replace(old,new,1)
rules_patch(
 '''        allow create, update: if roomAccess(roomId)
          && roomMember(roomId)
          && playerUid == request.auth.uid
          && request.resource.data.uid == request.auth.uid''',
 '''        // Immutable move = a player cannot revise a Dice/RPS/etc. choice after
        // seeing other participants' scores. Every move must match the one
        // current active shared game and round, even non-Sheep/TTT games.
        allow create: if roomAccess(roomId)
          && roomMember(roomId)
          && playerUid == request.auth.uid
          && request.resource.data.uid == request.auth.uid
          && exists(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current)
          && get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.status == 'active'
          && get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.roundId == request.resource.data.roundId
          && get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.type == request.resource.data.type''')
rules_patch(
 '''                   && request.resource.data.score <= 8));
        allow delete: if signedIn()''',
 '''                   && request.resource.data.score <= 8));
        allow update: if false; // immutable submissions; next turn after moderator DELETE
        allow delete: if signedIn()''')
rules.write_text(text)
gradle=root/'app/build.gradle';data=gradle.read_text()
prev="versionCode 221; versionName '9.9.42-gift-resume-game-sync-test'"
if data.count(prev)!=1:raise RuntimeError('Expected actual v9.9.42 full source')
gradle.write_text(data.replace(prev,"versionCode 222; versionName '9.9.43-immutable-live-game-moves-test'"))
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS authoritative current game/round, immutable move submissions, server-only result, paid recharge OFF')
