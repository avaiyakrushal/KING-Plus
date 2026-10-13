#!/usr/bin/env python3
"""KING v9.9.67: real Party Draw strokes are round-scoped and drawer-only.
A delayed old network callback must never paint on a subsequent real round.
Firestore rules enforce this even for hostile clients and other admins.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1]);src=root/'app/src/main/java/com/kingplus/social'
f=src/'RoomGameActivity.java';s=f.read_text()
a=s.index('    private void publishDrawSketch866(')
b=s.index('    private class DrawPad866 extends View{',a)
previous=s[a:b]
assert 'state().update(u)' in previous,'Real Draw sketch update changed; inspect before patch'
assert 'FieldValue.serverTimestamp()' in previous
replacement='''    /** Only the current actual private-word drawer may alter a running
     * Party canvas. Transaction ensures delayed callbacks cannot overwrite
     * a different round or closed/changed game. Security rules agree.
     */
    private void publishDrawSketch866(String sketch){
        if(!moderator||!"draw".equals(type)||me==null||db==null||
           !gameMemberVerified992||!gameStateVerified9943)return;
        if(!KingLiveDrawRound9967.validSketch(sketch)){
            toast("Canvas data is invalid or full");return;
        }
        final long expected=roundId;
        final String uid=me.getUid(),encoded=sketch;
        if(!KingLiveDrawRound9967.allowed(true,expected,roundId,
               "active",type,drawerUid866,uid,true,
               privateRoleRound866==expected&&!privateSecret866.isEmpty()))return;
        db.runTransaction(tx->{
            DocumentSnapshot live=tx.get(state());
            DocumentSnapshot member=tx.get(
                room().collection("members").document(uid));
            Long actual=live.getLong("roundId");
            String owner=s(live.getString("drawerUid"));
            if(!KingLiveDrawRound9967.allowed(
                live.exists(),expected,actual==null?-1L:actual.longValue(),
                s(live.getString("status")),s(live.getString("type")),
                owner,uid,
                member.exists()&&uid.equals(s(member.getString("uid"))),
                true))
                throw new IllegalStateException(
                    "Party game/drawer changed: sketch was not sent");
            Map<String,Object> update=new HashMap<>();
            update.put("actorUid",uid);
            update.put("sketch",encoded);
            update.put("updatedAt",FieldValue.serverTimestamp());
            tx.update(state(),update);
            return null;
        }).addOnFailureListener(err->toast(
            "Drawing sync denied: "+err.getMessage()));
    }
'''
s=s[:a]+replacement+s[b:]
for k in ('KingPartyHiddenWord9965.draw(', 'KingPartyPrivateRoleLoad9966.needsRequest',
          'KingSocialVoteRoster9964.validBallot','KingTttFairRoster9960.pickPair'):
    assert k in s,k
f.write_text(s)

(src/'KingLiveDrawRound9967.java').write_text('''package com.kingplus.social;
/** Pure guards for actual signed-in Party Draw actions and serial strokes. */
public final class KingLiveDrawRound9967 {
 private KingLiveDrawRound9967(){}
 public static boolean validSketch(String sketch){
   if(sketch==null||sketch.length()>12000)return false;
   for(int i=0;i<sketch.length();i++){
     char ch=sketch.charAt(i);
     if(!((ch>='0'&&ch<='9')||ch==','||ch==';'||ch=='|'))return false;
   }
   return true;
 }
 public static boolean allowed(boolean stateExists,long expected,long actual,
         String status,String type,String drawerUid,String actorUid,
         boolean memberPresent,boolean confirmedPrivateWord){
   return stateExists&&expected>0&&expected==actual&&memberPresent
     &&confirmedPrivateWord&&"active".equals(status)&&"draw".equals(type)
     &&drawerUid!=null&&!drawerUid.isEmpty()&&drawerUid.equals(actorUid);
 }
}
''')
p=root/'firestore.rules';r=p.read_text()
# Preserve the already-enforced active roster, cancellation, and other
# production game_state rules (do not replace them with weaker generic rules).
a=r.index('      match /game_state/{stateId} {')
b=r.index('      match /game_votes/{playerUid}',a)
block=r[a:b]
oldtail="== resource.data.get('participantUids',[]));"
assert block.count(oldtail)==1,'Unexpected roster guard; inspect before merge'
newtail="""== resource.data.get('participantUids',[]))
          // Sketch changes cannot be made by another cohost/moderator, or
          // after this Draw round closes. A new round from a closed game
          // may reset the old sketch while preserving the old game gates.
          && (
             !request.resource.data.diff(resource.data).affectedKeys()
                 .hasAny(['sketch'])
             || (
                 resource.data.get('type','') == 'draw'
                 && resource.data.get('status','') == 'active'
                 && request.resource.data.get('type','') == 'draw'
                 && request.resource.data.get('status','') == 'active'
                 && request.resource.data.get('roundId',0) ==
                    resource.data.get('roundId',0)
                 && request.auth.uid == resource.data.get('drawerUid','')
                 && request.resource.data.get('actorUid','') == request.auth.uid
                 && request.resource.data.get('sketch','') is string
                 && request.resource.data.get('sketch','').size() <= 12000
                 && request.resource.data.diff(resource.data).affectedKeys()
                      .hasOnly(['sketch','actorUid','updatedAt'])
             )
             || (
                 resource.data.get('status','') == 'closed'
                 && request.resource.data.get('status','') == 'active'
                 && request.resource.data.get('roundId',0) is int
                 && resource.data.get('roundId',0) is int
                 && request.resource.data.roundId > resource.data.roundId
             )
          );"""
block=block.replace(oldtail,newtail,1)
r=r[:a]+block+r[b:]
p.write_text(r)
g=root/'app/build.gradle';v=g.read_text()
oldver="versionCode 245; versionName '9.9.66-live-draw-private-role-retry-test'"
assert v.count(oldver)==1,'Require verified full signed v9.9.66 source'
g.write_text(v.replace(oldver,
  "versionCode 246; versionName '9.9.67-live-party-draw-round-integrity-test'"))
party=(src/'PartyActivity.java').read_text()
for k in ('KingPartyMicConsent9956','KingPartyVoiceReconnect9957',
          'KingGiftReceiptDurable9962','KingGiftBurstBackpressure9963'):
    assert k in party,k
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS real Party DRAW: drawer-only, round scoped, atomic status+membership checks, Firestore sketch change policy')
