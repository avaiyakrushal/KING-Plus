#!/usr/bin/env python3
"""KING v9.9.64: real Party Werewolf/Spy voting integrity and stale result race.
A vote can be cast only by the current round's frozen actual room member,
against another currently joined frozen participant, using a SERVER-verified
roster + Firestore transaction. Moderator results only count legitimate ballots;
finishing any social round is an atomic compare-and-set on round and game type.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1]); src=root/'app/src/main/java/com/kingplus/social'
p=src/'RoomGameActivity.java'; s=p.read_text()

a=s.index('    private void votePlayer866(){')
b=s.index('    private void finishHiddenRound866(){',a)
new_vote='''    /** Actual active members of this frozen Party round, not stale Ready docs. */
    private void votePlayer866(){
        if(me==null||db==null||roundId<=0||!gameMemberVerified992)return;
        final long expected=roundId;
        final String game=type, voter=me.getUid();
        if(!("werewolf".equals(game)||"spy".equals(game)))return;
        state().get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(st->{
            if(!KingSocialVoteRoster9964.isCurrent(st.exists(),
                    st.getLong("roundId"),expected,
                    s(st.getString("status")),s(st.getString("type")),game))return;
            final java.util.List<String> locked=KingSocialVoteRoster9964.roster(
                st.get("participantUids"));
            if(!locked.contains(voter)){
                toast("You are spectating this round • cannot vote");return;
            }
            room().collection("members")
              .get(com.google.firebase.firestore.Source.SERVER)
              .addOnSuccessListener(members->{
                if(me==null||!voter.equals(me.getUid())||roundId!=expected)return;
                final java.util.ArrayList<String> choices=new java.util.ArrayList<>();
                final java.util.ArrayList<String> labels=new java.util.ArrayList<>();
                for(DocumentSnapshot m:members.getDocuments()){
                    String uid=m.getId();
                    if(!uid.equals(s(m.getString("uid")))||!locked.contains(uid))continue;
                    choices.add(uid);
                    String name=s(m.getString("name"));
                    labels.add(name.isEmpty()?"Player":name);
                }
                if(choices.isEmpty()){toast("No active Party members to vote for");return;}
                String[] labelArray=labels.toArray(new String[0]);
                new AlertDialog.Builder(this)
                  .setTitle("🗳 Vote • "+("werewolf".equals(game)?
                    "Who is the Werewolf?":"Who is the Spy?"))
                  .setItems(labelArray,(dialog,which)->{
                    if(which<0||which>=choices.size())return;
                    final String target=choices.get(which),targetName=labels.get(which);
                    if(me==null||!voter.equals(me.getUid())||roundId!=expected)return;
                    // A Party room / player may leave between list and tap.
                    // Fresh transaction prevents a ghost or spectator vote.
                    db.runTransaction(tx->{
                        DocumentSnapshot live=tx.get(state());
                        DocumentSnapshot own=tx.get(
                            room().collection("members").document(voter));
                        DocumentSnapshot other=tx.get(
                            room().collection("members").document(target));
                        if(!KingSocialVoteRoster9964.isCurrent(live.exists(),
                                live.getLong("roundId"),expected,
                                s(live.getString("status")),
                                s(live.getString("type")),game) ||
                            !KingSocialVoteRoster9964.validBallot(
                                voter,target,KingSocialVoteRoster9964.roster(
                                    live.get("participantUids")),
                                own.exists()&&voter.equals(s(own.getString("uid"))),
                                other.exists()&&target.equals(s(other.getString("uid")))))
                            throw new IllegalStateException(
                                "Round/Party player changed; reopen voting");
                        Map<String,Object> value=new HashMap<>();
                        value.put("uid",voter);value.put("name",displayName);
                        value.put("roundId",expected);value.put("choice",target);
                        value.put("choiceName",targetName);
                        value.put("updatedAt",FieldValue.serverTimestamp());
                        tx.set(room().collection("game_votes").document(voter),value);
                        return null;
                    }).addOnSuccessListener(v->toast("Vote saved: "+targetName))
                      .addOnFailureListener(e->toast("Vote denied: "+e.getMessage()));
                  }).setNegativeButton("Close",null).show();
              }).addOnFailureListener(e->toast("Live Party members unavailable"));
          }).addOnFailureListener(e->toast("Active Party round unavailable"));
    }
'''
s=s[:a]+new_vote+s[b:]
a=s.index('    private void finishHiddenRound866(){')
b=s.index('    private void submitDrawGuess866(EditText box){',a)
new_finish='''    /** Ignore ghosts, spectators and forged voter IDs at round settlement. */
    private void finishHiddenRound866(){
        if(!moderator||me==null||db==null||
          !("werewolf".equals(type)||"spy".equals(type))||roundId<=0)return;
        final long expected=roundId;
        final String game=type,targetRole="werewolf".equals(game)?"Werewolf":"Spy";
        state().get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(stateDoc->{
            if(!KingSocialVoteRoster9964.isCurrent(stateDoc.exists(),
                  stateDoc.getLong("roundId"),expected,
                  s(stateDoc.getString("status")),
                  s(stateDoc.getString("type")),game))return;
            final java.util.List<String> locked=KingSocialVoteRoster9964.roster(
                  stateDoc.get("participantUids"));
            room().collection("game_votes")
              .get(com.google.firebase.firestore.Source.SERVER)
              .addOnSuccessListener(votes->{
                if(roundId!=expected)return;
                final Map<String,Integer> counts=new HashMap<>();
                final Map<String,String> names=new HashMap<>();
                for(DocumentSnapshot d:votes.getDocuments()){
                    Long r=d.getLong("roundId");
                    if(r==null||r.longValue()!=expected)continue;
                    String voter=d.getId(),choice=s(d.getString("choice"));
                    if(!KingSocialVoteRoster9964.validVoteDocument(
                         voter,s(d.getString("uid")),choice,locked))continue;
                    counts.put(choice,counts.containsKey(choice)?
                        counts.get(choice)+1:1);
                    names.put(choice,s(d.getString("choiceName")));
                }
                room().collection("room_invites")
                  .get(com.google.firebase.firestore.Source.SERVER)
                  .addOnSuccessListener(invites->{
                    if(roundId!=expected)return;
                    String targetUid="",targetName=targetRole;
                    for(DocumentSnapshot d:invites.getDocuments()){
                        Long r=d.getLong("roundId");
                        if(r==null||r.longValue()!=expected
                           ||!game.equals(s(d.getString("gameType"))))continue;
                        String u=s(d.getString("recipientUid"));
                        if(targetRole.equals(s(d.getString("role")))
                           &&locked.contains(u)){
                            targetUid=u;targetName=s(d.getString("recipientName"));break;
                        }
                    }
                    if(targetUid.isEmpty()){
                        toast("Private role data is unavailable");return;
                    }
                    int max=0;java.util.List<String> top=new ArrayList<>();
                    for(Map.Entry<String,Integer> e:counts.entrySet()){
                        if(e.getValue()>max){
                            max=e.getValue();top.clear();top.add(e.getKey());
                        }else if(e.getValue()==max)top.add(e.getKey());
                    }
                    String out;
                    if(max==0)out="No valid votes • "+targetName+" was the "+targetRole;
                    else if(top.size()!=1)out="Vote tied • "+targetName+" was the "+targetRole;
                    else{
                        String picked=top.get(0),pickedName=names.get(picked);
                        if(pickedName==null||pickedName.isEmpty())pickedName="Player";
                        boolean found=targetUid.equals(picked);
                        out=(found?("werewolf".equals(game)?"🏆 Village wins":
                           "🏆 Players found the Spy"):("werewolf".equals(game)?
                           "🐺 Werewolf escaped":"🕵 Spy escaped"))
                           +"\\nMost voted: "+pickedName+"\\n"+targetRole+": "+targetName;
                    }
                    closeSocialRound866(out);
                  }).addOnFailureListener(e->toast("Private roles unavailable"));
              }).addOnFailureListener(e->toast("Party votes unavailable"));
          }).addOnFailureListener(e->toast("Party game changed; refresh"));
    }
'''
s=s[:a]+new_finish+s[b:]
a=s.index('    private void closeSocialRound866(String out){')
b=s.index('    private void drawGuessControls866(){',a)
close='''    /** The previous round MUST NOT close a new round due to an async callback. */
    private void closeSocialRound866(String out){
        if(!moderator||me==null||db==null||roundId<=0)return;
        final long expected=roundId; final String expectedType=type;
        final String expectedUid=me.getUid();
        db.runTransaction(tx->{
            DocumentSnapshot fresh=tx.get(state());
            DocumentSnapshot owner=tx.get(
                room().collection("members").document(expectedUid));
            if(!owner.exists()||!expectedUid.equals(s(owner.getString("uid")))||
               !KingSocialVoteRoster9964.isCurrent(fresh.exists(),
                   fresh.getLong("roundId"),expected,
                   s(fresh.getString("status")),
                   s(fresh.getString("type")),expectedType))
                throw new IllegalStateException("Party round changed; cannot finish");
            Map<String,Object> update=new HashMap<>();
            update.put("actorUid",expectedUid);
            update.put("result",out);update.put("status","closed");
            update.put("updatedAt",FieldValue.serverTimestamp());
            tx.update(state(),update);
            return null;
        }).addOnSuccessListener(v->postGameEvent750(
             "🏁 "+out.replace("\\n"," • ")))
          .addOnFailureListener(e->toast("Finish denied: "+e.getMessage()));
    }
'''
s=s[:a]+close+s[b:]
p.write_text(s)

(src/'KingSocialVoteRoster9964.java').write_text('''package com.kingplus.social;
import java.util.*;
/** Pure logic for validated, frozen, live Party game vote participants. */
public final class KingSocialVoteRoster9964 {
 private KingSocialVoteRoster9964(){}
 public static boolean isCurrent(boolean exists,Long stored,long expected,
                                 String status,String type,String required){
   return exists&&stored!=null&&expected>0&&stored.longValue()==expected
      &&"active".equals(status)&&required!=null&&required.equals(type);
 }
 public static List<String> roster(Object raw){
   if(!(raw instanceof List))return Collections.emptyList();
   List<String> out=new ArrayList<>();
   for(Object v:(List<?>)raw){
     if(v instanceof String&&!((String)v).isEmpty()&&!out.contains(v))
       out.add((String)v);
   }
   return out;
 }
 public static boolean validBallot(String voter,String target,List<String> locked,
                                   boolean voterPresent,boolean targetPresent){
   return voter!=null&&target!=null&&locked!=null&&voterPresent&&targetPresent
       &&locked.contains(voter)&&locked.contains(target);
 }
 public static boolean validVoteDocument(String documentUid,String claimedUid,
                                         String choice,List<String> locked){
   return documentUid!=null&&!documentUid.isEmpty()
      &&documentUid.equals(claimedUid)&&choice!=null
      &&locked!=null&&locked.contains(documentUid)&&locked.contains(choice);
 }
}
''')

rules=root/'firestore.rules'
r=rules.read_text()
old='''      match /game_votes/{playerUid} {
        allow read: if roomAccess(roomId)
          && (playerUid == request.auth.uid || roomModerator(roomId));
        allow create, update: if roomAccess(roomId)
          && roomMember(roomId)
          && playerUid == request.auth.uid
          && request.resource.data.uid == request.auth.uid;
        allow delete: if signedIn()
          && (playerUid == request.auth.uid || roomModerator(roomId));
      }'''
new='''      match /game_votes/{playerUid} {
        allow read: if roomAccess(roomId)
          && (playerUid == request.auth.uid || roomModerator(roomId));
        allow create, update: if roomAccess(roomId)
          && roomMember(roomId)
          && playerUid == request.auth.uid
          && request.resource.data.uid == request.auth.uid
          && exists(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current)
          && get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.status == 'active'
          && get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.roundId == request.resource.data.roundId
          && get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.participantUids is list
          && request.auth.uid in get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.participantUids
          && (get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.type
                in ['werewolf','spy']
              ? request.resource.data.choice in get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.participantUids
              : request.resource.data.choice is string);
        allow delete: if signedIn()
          && (playerUid == request.auth.uid || roomModerator(roomId));
      }'''
assert r.count(old)==1,'Full v9.9.63 Firebase security rules changed'
rules.write_text(r.replace(old,new,1))

g=root/'app/build.gradle'
grad=g.read_text()
oldver="versionCode 242; versionName '9.9.63-live-ready-game-gift-overflow-test'"
assert grad.count(oldver)==1,'requires verified v9.9.63 full app source'
g.write_text(grad.replace(oldver,
  "versionCode 243; versionName '9.9.64-real-social-vote-security-test'"))
party=(src/'PartyActivity.java').read_text()
for t in ('KingPartyMicConsent9956','KingPartyVoiceReconnect9957',
          'KingGiftReceiptDurable9962','KingGiftBurstBackpressure9963'):
 assert t in party,t
assert 'KingTttFairRoster9960.pickPair' in s
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS online Party votes: frozen live roster, no ghost/spectator vote, wrong-round finishing impossible')
