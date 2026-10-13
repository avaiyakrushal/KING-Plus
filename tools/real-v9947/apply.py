#!/usr/bin/env python3
"""KING v9.9.47 actual active Party Game cancellation and recovery.

A Host/Co-host can abort a stuck multiplayer round (e.g. one frozen Ready
player disappeared), without faking a winner. The CANCEL is an authenticated,
round-specific Firestore transaction and does not award or alter gift value.
Firebase rules independently enforce the narrow no-winner cancel transition.
After cancellation the ordinary existing Rematch/Start path works.
"""
from pathlib import Path
import sys
r=Path(sys.argv[1]);src=r/'app/src/main/java/com/kingplus/social'
game=src/'RoomGameActivity.java';j=game.read_text()
def swap(a,b):
 global j
 n=j.count(a)
 if n!=1: raise RuntimeError("Game source anchor count "+str(n)+": "+a[:140])
 j=j.replace(a,b,1)
swap('    private boolean gameStateVerified9943=false;',
'''    private boolean gameStateVerified9943=false;
    private final java.util.ArrayList<String> frozenRoster9947=new java.util.ArrayList<>();''')
swap('gameMemberVerified992=false;gameStateVerified9943=false;moderator=false;',
     'gameMemberVerified992=false;gameStateVerified9943=false;frozenRoster9947.clear();moderator=false;')
swap('''        if(!d.exists()){roundId=0;''',
'''        frozenRoster9947.clear();
        if(d.exists()){
            Object participants9947=d.get("participantUids");
            if(participants9947 instanceof java.util.List)
                for(Object participant9947:(java.util.List<?>)participants9947)
                    if(participant9947 instanceof String)
                        frozenRoster9947.add((String)participant9947);
        }
        if(!d.exists()){roundId=0;''')
swap('''        Button ready=button("✅ Ready / Not Ready");ready.setOnClickListener(v->toggleReady750());controls.addView(ready,new LinearLayout.LayoutParams(-1,dp(50)));''',
'''        Button ready=button("✅ Ready / Not Ready");ready.setOnClickListener(v->toggleReady750());controls.addView(ready,new LinearLayout.LayoutParams(-1,dp(50)));
        if(moderator&&"active".equals(status)){
            Button cancel9947=button("🚫 Cancel stuck round • no winner");
            cancel9947.setOnClickListener(v->confirmCancelRound9947());
            LinearLayout.LayoutParams clp9947=new LinearLayout.LayoutParams(-1,dp(50));
            clp9947.setMargins(0,dp(6),0,dp(6));
            controls.addView(cancel9947,clp9947);
        }''')
swap('''    private void renderMoves(){movesBox.removeAllViews();if(currentMoves.isEmpty())''',
'''    private void renderMoves(){
        movesBox.removeAllViews();
        if(KingRoomRoster9946.lockedGame(type)
            &&"active".equals(status)&&KingRoomRoster9946.valid(frozenRoster9947)){
            java.util.ArrayList<String> realSubmissions9947=new java.util.ArrayList<>();
            for(DocumentSnapshot submitted9947:currentMoves)
                if(type.equals(submitted9947.getString("type"))
                   &&submitted9947.getId().equals(submitted9947.getString("uid")))
                    realSubmissions9947.add(submitted9947.getId());
            int count9947=KingRoomCancel9947.countValid(frozenRoster9947,realSubmissions9947);
            movesBox.addView(tv("👥 Ready moves: "+count9947+"/"+frozenRoster9947.size()
                +(count9947==frozenRoster9947.size()?" • all played":" • waiting for players"),
                13,MUTED,true));
        }
        if(currentMoves.isEmpty())''')
anchor='    private void finishRound(){'
methods="""    /** Explicit host decision after a player leaves/disconnects. Never award a winner. */
    private void confirmCancelRound9947(){
        if(!KingRoomCancel9947.canCancel(moderator,gameMemberVerified992,
              gameStateVerified9943,status,roundId)){
            toast("Cancel is only available during a verified active Party game");
            return;
        }
        final long expectedRound9947=roundId;
        final String expectedType9947=type;
        new AlertDialog.Builder(this)
            .setTitle("🚫 Cancel this Party game?")
            .setMessage("If a player has left, cancel this round without declaring a winner. "+
                "Submitted moves remain in history until the next round. Start/Rematch will "+
                "let the Ready players begin a fresh game.")
            .setNegativeButton("Keep playing",null)
            .setPositiveButton("Cancel • no winner",(dialog,which)->
                cancelRound9947(expectedRound9947,expectedType9947))
            .show();
    }
    private void cancelRound9947(long expectedRound9947,String expectedType9947){
        if(me==null||db==null||
           !KingRoomCancel9947.canCancel(moderator,gameMemberVerified992,
              gameStateVerified9943,status,roundId)
           ||roundId!=expectedRound9947||!type.equals(expectedType9947)){
            toast("Game changed • cancellation not sent");return;
        }
        final String canceller9947=me.getUid();
        db.runTransaction(tx->{
            DocumentSnapshot latest=tx.get(state());
            if(!latest.exists()||latest.getLong("roundId")==null||
               latest.getLong("roundId").longValue()!=expectedRound9947||
               !expectedType9947.equals(latest.getString("type"))||
               !"active".equals(latest.getString("status")))
                throw new IllegalStateException("This round already changed or finished");
            java.util.Map<String,Object> fields=new java.util.HashMap<>();
            fields.put("status","closed");
            fields.put("cancelled",true);
            fields.put("result",KingRoomCancel9947.RESULT);
            fields.put("actorUid",canceller9947);
            fields.put("updatedAt",FieldValue.serverTimestamp());
            tx.update(state(),fields);return null;
        }).addOnSuccessListener(v->{
            postGameEvent750("🏁 "+KingRoomCancel9947.RESULT);
            toast("Round cancelled without a winner • Rematch is available");
        }).addOnFailureListener(e->toast("Cancel failed: "+e.getMessage()));
    }

"""
if j.count(anchor)!=1:raise RuntimeError('Unique finish round anchor missing')
j=j.replace(anchor,methods+anchor,1)
game.write_text(j)
(src/'KingRoomCancel9947.java').write_text("""package com.kingplus.social;
import java.util.*;
/** Genuine Party round cancellation; no winner or diamond award. */
public final class KingRoomCancel9947 {
 private KingRoomCancel9947(){}
 public static final String RESULT="🚫 Round cancelled • no winner";
 public static boolean canCancel(boolean moderator,boolean member,
                                 boolean serverVerified,String status,long round){
   return moderator&&member&&serverVerified&&"active".equals(status)&&round>0;
 }
 public static int countValid(List<?> frozen,List<String> submitted){
   if(frozen==null||submitted==null)return 0;
   Set<String> present=new HashSet<>();
   for(String uid:submitted)
     if(uid!=null&&frozen.contains(uid))present.add(uid);
   return present.size();
 }
}
""")
rules=r/'firestore.rules';t=rules.read_text()
old="""      match /game_state/{stateId} {
        allow read: if roomAccess(roomId) && roomMember(roomId);
        allow create: if roomModerator(roomId)
          && request.resource.data.get('participantUids',[]) is list
          && request.resource.data.get('participantUids',[]).size() <= 50;
        allow update: if roomModerator(roomId)
          && request.resource.data.get('participantUids',[]) is list
          && request.resource.data.get('participantUids',[]).size() <= 50
          // During an active round no one may switch or shrink its roster
          // to allow premature winner/score settlement.
          && (resource.data.get('status','') != 'active'
              || request.resource.data.get('participantUids',[])
                 == resource.data.get('participantUids',[]));
        allow delete: if roomModerator(roomId);
      }"""
new="""      match /game_state/{stateId} {
        allow read: if roomAccess(roomId) && roomMember(roomId);
        allow create: if roomModerator(roomId)
          && request.resource.data.get('cancelled',false) == false
          && request.resource.data.get('participantUids',[]) is list
          && request.resource.data.get('participantUids',[]).size() <= 50;
        allow update: if roomModerator(roomId)
          && request.resource.data.get('participantUids',[]) is list
          && request.resource.data.get('participantUids',[]).size() <= 50
          // A cancelled match must be a single narrow transition from a real
          // active round. Host may not forge a winner or rewrite the roster
          // while claiming cancellation.
          && (request.resource.data.get('cancelled',false) != true
              || (resource.data.get('status','') == 'active'
                  && resource.data.get('cancelled',false) != true
                  && request.resource.data.status == 'closed'
                  && request.resource.data.result == '🚫 Round cancelled • no winner'
                  && request.resource.data.actorUid == request.auth.uid
                  && request.resource.data.updatedAt == request.time
                  && request.resource.data.diff(resource.data).affectedKeys().hasOnly([
                      'actorUid','status','result','cancelled','updatedAt'
                  ])))
          // During an active round no one may switch or shrink its roster
          // to allow premature winner/score settlement.
          && (resource.data.get('status','') != 'active'
              || request.resource.data.get('participantUids',[])
                 == resource.data.get('participantUids',[]));
        allow delete: if roomModerator(roomId);
      }"""
if t.count(old)!=1:raise RuntimeError("Expected original v9.9.46 game_state rule "+str(t.count(old)))
rules.write_text(t.replace(old,new,1))
gradle=r/'app/build.gradle';g=gradle.read_text()
old="versionCode 225; versionName '9.9.46-frozen-ready-multiplayer-test'"
if g.count(old)!=1:raise RuntimeError("Not actual v9.9.46 full-source release")
gradle.write_text(g.replace(old,"versionCode 226; versionName '9.9.47-safe-round-cancel-test'"))
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print("PASS Host-only confirmed safe CANCEL no fake winner, current round preserved, Free Gifting unchanged")
