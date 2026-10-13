#!/usr/bin/env python3
"""v9.9.46 - freeze real Party participants for RPS, Dice and Coin.

The old Host Finish button allowed two submitted moves to settle a round even
when 3-50 Ready players were still waiting. Here the real SERVER Ready roster
is frozen in the round's Firestore state. The app won't publish a winner until
every frozen participant submits a matching server-committed move. Firestore
rejects outsider moves and mid-round roster edits. Previous game mechanics
and FREE Gift delivery are untouched.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1]);src=root/'app/src/main/java/com/kingplus/social'
p=src/'RoomGameActivity.java';text=p.read_text()
def rep(a,b):
 global text
 n=text.count(a)
 if n!=1:raise RuntimeError(f'Expected one anchor in RoomGameActivity, found {n}: {a[:110]}')
 text=text.replace(a,b,1)
rep('''        room().collection("game_ready").get().addOnSuccessListener(q->{
            if(q.size()<2){toast("At least two players must tap Ready");return;}
            if(q.size()>50){toast("This round supports up to 50 ready players");return;}
            startRoundNow750(game);''',
'''        room().collection("game_ready").get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(q->{
            if(q.size()<2){toast("At least two players must tap Ready");return;}
            if(q.size()>50){toast("This round supports up to 50 ready players");return;}
            java.util.ArrayList<String> frozenPlayers9946=new java.util.ArrayList<>();
            for(DocumentSnapshot ready:q.getDocuments()){
                String uid=s(ready.getString("uid"));
                if(!uid.equals(ready.getId())||uid.isEmpty()){
                    toast("Ready list contains an invalid player • ask users to rejoin");return;
                }
                frozenPlayers9946.add(uid);
            }
            if(KingRoomRoster9946.lockedGame(game)&&
               !KingRoomRoster9946.valid(frozenPlayers9946)){
               toast("At least 2 unique Ready players are required");return;
            }
            startRoundNow750(game,frozenPlayers9946);''')
rep('''    private void startRoundNow750(String game){
        if(!moderator)return;''',
'''    private void startRoundNow750(String game,java.util.List<String> frozenPlayers9946){
        if(!moderator)return;
        if(KingRoomRoster9946.lockedGame(game)
           &&!KingRoomRoster9946.valid(frozenPlayers9946)){
            toast("Ready participants changed; reopen round");return;
        }''')
rep('''value.put("status","active");value.put("roundId",next);if("memory".equals(game))''',
'''value.put("status","active");value.put("roundId",next);
            if(KingRoomRoster9946.lockedGame(game))
                value.put("participantUids",new java.util.ArrayList<>(frozenPlayers9946));
            if("memory".equals(game))''')
# Replace only the bounded finishRound method, preserve exact rest of file.
open_anchor='    private void finishRound(){'
close_anchor='    private String resultFor(List<DocumentSnapshot>a){'
if text.count(open_anchor)!=1 or text.count(close_anchor)!=1:raise RuntimeError('Round boundary changed')
start=text.index(open_anchor);end=text.index(close_anchor,start)
finish="""    private void finishRound(){
        if(!gameMemberVerified992||!gameStateVerified9943||!moderator||
           me==null||roundId==0||!"active".equals(status)){
            toast("Join a verified active Party round before finishing");return;
        }
        final long expected=roundId;
        final String expectedType=type;
        room().collection("game_moves")
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(q->{
                java.util.ArrayList<DocumentSnapshot> a=new java.util.ArrayList<>();
                java.util.ArrayList<String> submitted=new java.util.ArrayList<>();
                for(DocumentSnapshot doc:q.getDocuments()){
                    Long number=doc.getLong("roundId");
                    if(number!=null&&number.longValue()==expected
                       &&expectedType.equals(doc.getString("type"))
                       &&doc.getId().equals(doc.getString("uid"))
                       &&!doc.getMetadata().hasPendingWrites()){
                        a.add(doc);submitted.add(doc.getId());
                    }
                }
                if(a.size()<2){toast("At least 2 real players must submit");return;}
                state().get(com.google.firebase.firestore.Source.SERVER)
                  .addOnSuccessListener(serverRound->{
                    if(!serverRound.exists()||roundId!=expected||
                        !expectedType.equals(type)||!"active".equals(status)
                        ||serverRound.getLong("roundId")==null||
                        serverRound.getLong("roundId").longValue()!=expected||
                        !expectedType.equals(serverRound.getString("type"))||
                        !"active".equals(serverRound.getString("status"))){
                        toast("Game round changed, refresh Party");return;
                    }
                    final java.util.List<?> frozen=serverRound.get("participantUids")
                         instanceof java.util.List?
                         (java.util.List<?>)serverRound.get("participantUids"):null;
                    if(KingRoomRoster9946.lockedGame(expectedType)&&frozen!=null
                       &&!KingRoomRoster9946.complete(frozen,submitted)){
                        toast("Waiting for all "+frozen.size()+
                             " Ready players • received "+submitted.size()+" moves");
                        return;
                    }
                    String result=resultFor(a);
                    if(result.length()>300)result=result.substring(0,297)+"...";
                    final String finalResult=result;
                    java.util.Map<String,Object> values=new java.util.HashMap<>();
                    values.put("actorUid",me.getUid());
                    values.put("result",result);
                    values.put("status","closed");
                    values.put("updatedAt",FieldValue.serverTimestamp());
                    db.runTransaction(tx->{
                        DocumentSnapshot latest=tx.get(state());
                        if(!latest.exists()||latest.getLong("roundId")==null
                           ||latest.getLong("roundId").longValue()!=expected
                           ||!expectedType.equals(latest.getString("type"))
                           ||!"active".equals(latest.getString("status")))
                            throw new IllegalStateException("Round changed or already finished");
                        if(KingRoomRoster9946.lockedGame(expectedType)&&frozen!=null){
                            Object current=latest.get("participantUids");
                            if(!(current instanceof java.util.List)
                               ||!frozen.equals(current)
                               ||!KingRoomRoster9946.complete((java.util.List<?>)current,submitted))
                                throw new IllegalStateException("Ready roster changed; cannot publish winner");
                        }
                        tx.update(state(),values);
                        return null;
                    }).addOnSuccessListener(v->{
                        postGameEvent750("🏁 "+finalResult.replace("\\n"," • "));
                        new AlertDialog.Builder(this).setTitle("🏁 Round result")
                            .setMessage(finalResult)
                            .setPositiveButton("Rematch",(dialog,which)->rematch750())
                            .setNegativeButton("Close",null).show();
                    }).addOnFailureListener(e->toast("Finish failed: "+e.getMessage()));
                }).addOnFailureListener(e->toast("Could not verify Ready players: "+e.getMessage()));
            }).addOnFailureListener(e->toast("Could not load round moves: "+e.getMessage()));
    }
"""
text=text[:start]+finish+text[end:]
p.write_text(text)
(src/'KingRoomRoster9946.java').write_text("""package com.kingplus.social;
import java.util.*;
/** Pure-Java, deterministic Room party participant/settlement consistency.
 * Ready UIDs are frozen at the moment the round is started.
 */
public final class KingRoomRoster9946 {
 private KingRoomRoster9946(){}
 public static boolean lockedGame(String type){
   return "rps".equals(type)||"dice".equals(type)||"coin".equals(type);
 }
 public static boolean valid(List<?> players){
   if(players==null||players.size()<2||players.size()>50)return false;
   Set<String> uids=new HashSet<>();
   for(Object player:players){
     if(!(player instanceof String)||((String)player).trim().isEmpty()
        ||!uids.add((String)player))return false;
   }
   return true;
 }
 public static boolean complete(List<?> ready,List<String> submitted){
   if(!valid(ready)||submitted==null||submitted.size()!=ready.size())return false;
   Set<String> submittedSet=new HashSet<>();
   for(String uid:submitted)
     if(uid==null||!submittedSet.add(uid))return false;
   return submittedSet.equals(new HashSet<>(ready));
 }
}
""")
rules=root/'firestore.rules';s=rules.read_text()
old="""          && get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.type == request.resource.data.type
          // Only actual RPS choices;"""
new="""          && get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.type == request.resource.data.type
          // A member cannot enter somebody else's frozen RPS/Dice/Coin round.
          // Legacy active rounds without a roster retain the prior behavior.
          && (!(request.resource.data.type in ['rps','dice','coin'])
              || get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.get('participantUids',[]).size() == 0
              || request.auth.uid in get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.get('participantUids',[]))
          // Only actual RPS choices;"""
if s.count(old)!=1:raise RuntimeError('Missing exact game_moves rule')
s=s.replace(old,new,1)
old="""      match /game_state/{stateId} {
        allow read: if roomAccess(roomId) && roomMember(roomId);
        allow create, update, delete: if roomModerator(roomId);
      }"""
new="""      match /game_state/{stateId} {
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
if s.count(old)!=1:raise RuntimeError('Missing game_state rules')
rules.write_text(s.replace(old,new,1))
gradle=root/'app/build.gradle';v=gradle.read_text()
old="versionCode 224; versionName '9.9.45-real-rps-dice-result-test'"
if v.count(old)!=1:raise RuntimeError('Not verified full KING v9.9.45')
gradle.write_text(v.replace(old,"versionCode 225; versionName '9.9.46-frozen-ready-multiplayer-test'"))
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS original multiplayer Ready roster lock and complete settlement gating, gifting unchanged')
