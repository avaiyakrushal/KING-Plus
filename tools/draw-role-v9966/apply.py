#!/usr/bin/env python3
"""KING v9.9.66: actual multiplayer Draw host can resume after process recreation,
without infinite Firebase private-role GET/render loop when invite is absent/delayed.
A one-time SERVER fetch per round, strict recipient/game validation, explicit retry,
and don't enable drawing without confirmed own private word.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1]);src=root/'app/src/main/java/com/kingplus/social'
f=src/'RoomGameActivity.java'
s=f.read_text()
a=s.index('    private void fetchPrivateRole866(){')
b=s.index('    private void hiddenRoleControls866(){',a)
old=s[a:b]
for name in ('roleDocId866(expected,me.getUid())','room().collection("room_invites")',
             'privateRole866','privateSecret866'):
    assert name in old,name
assert 'Source.SERVER' not in old,'Already verified private role from server; re-audit'
new='''    /** Fetch a real recipient-only role once per active round. If missing or
     * temporarily offline, display a Retry control, never recursively render
     * into another unbounded Firebase get() call.
     */
    private long privateFetchRound9966=-1L;
    private boolean privateFetchPending9966=false;
    private boolean privateFetchFailed9966=false;
    private String privateFetchUid9966="";
    private void fetchPrivateRole866(){
        if(me==null||db==null||roundId<=0||
           !("werewolf".equals(type)||"spy".equals(type)||
             ("draw".equals(type)&&me.getUid().equals(drawerUid866))))return;
        final long expected=roundId;final String expectedType=type;
        final String uid=me.getUid();
        if(privateRoleRound866!=expected){
            privateRoleRound866=expected;
            privateRole866="";privateSecret866="";
            privateFetchRound9966=-1L;privateFetchFailed9966=false;
            privateFetchPending9966=false;privateFetchUid9966="";
        }
        if(!KingPartyPrivateRoleLoad9966.needsRequest(
             expected,privateFetchRound9966,privateFetchPending9966,
             privateRole866,privateSecret866,expectedType))return;
        privateFetchRound9966=expected;
        privateFetchPending9966=true;
        privateFetchFailed9966=false;
        privateFetchUid9966=uid;
        room().collection("room_invites").document(roleDocId866(expected,uid))
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(doc->{
              if(!uid.equals(privateFetchUid9966)||me==null||
                 !uid.equals(me.getUid())||roundId!=expected||
                 !expectedType.equals(type))return;
              privateFetchPending9966=false;
              Long stored=doc==null?null:doc.getLong("roundId");
              boolean eligible=doc!=null&&doc.exists()&&stored!=null&&
                  stored.longValue()==expected&&uid.equals(doc.getString("recipientUid"))
                  &&expectedType.equals(doc.getString("gameType"));
              if(eligible){
                  privateRole866=s(doc.getString("role"));
                  privateSecret866=s(doc.getString("secret"));
              }
              if(!eligible||privateRole866.isEmpty()||
                 ("draw".equals(expectedType)&&privateSecret866.isEmpty())){
                  privateRole866="Role unavailable";
                  privateSecret866="";
                  privateFetchFailed9966=true;
              }
              renderControls();
          }).addOnFailureListener(err->{
              if(!uid.equals(privateFetchUid9966)||me==null||
                 !uid.equals(me.getUid())||roundId!=expected||
                 !expectedType.equals(type))return;
              privateFetchPending9966=false;
              privateFetchFailed9966=true;
              privateRole866="Role unavailable";
              privateSecret866="";
              renderControls();
          });
    }
    private void retryPrivateRole9966(){
        if(privateFetchPending9966)return;
        privateFetchRound9966=-1L;privateFetchFailed9966=false;
        privateRole866="";privateSecret866="";
        fetchPrivateRole866();
    }
'''
s=s[:a]+new+s[b:]
# Update the Draw live editing guards while maintaining the existing UI.
a=s.index('    private void drawGuessControls866(){')
b=s.index('    private void publishDrawSketch866(',a)
part=s[a:b]
old='''if(drawer&&(privateRoleRound866!=roundId||privateSecret866.isEmpty()))
            fetchPrivateRole866();'''
new='''if(drawer&&(privateRoleRound866!=roundId||privateSecret866.isEmpty())
            &&!privateFetchPending9966&&!privateFetchFailed9966)
            fetchPrivateRole866();
        if(drawer&&privateFetchFailed9966){
            Button retry=button("↻ Retry private drawing word");
            retry.setOnClickListener(v->retryPrivateRole9966());
            controls.addView(retry,new LinearLayout.LayoutParams(-1,dp(48)));
        }'''
assert part.count(old)==1,'v9.9.65 drawer fetch anchor changed'
part=part.replace(old,new,1)
assert 'privateRoleRound866==roundId&&!privateSecret866.isEmpty()' in part
s=s[:a]+part+s[b:]
# Hidden Spy/Werewolf may also need a user-initiated role retry.
a=s.index('    private void hiddenRoleControls866(){')
b=s.index('    private void votePlayer866(){',a)
part=s[a:b]
old='''        controls.addView(tv("Use the Party Room voice/chat to discuss. Your role is only shown on your screen.",13,MUTED,false));'''
new='''        if(privateFetchFailed9966){
            Button retry=button("↻ Retry my private role");
            retry.setOnClickListener(v->retryPrivateRole9966());
            controls.addView(retry,new LinearLayout.LayoutParams(-1,dp(48)));
        }
        controls.addView(tv("Use the Party Room voice/chat to discuss. Your role is only shown on your screen.",13,MUTED,false));'''
assert part.count(old)==1,'v9.9.65 private role controls changed'
part=part.replace(old,new,1)
s=s[:a]+part+s[b:]
# Previous two-phone games, Gift and Mic remain.
assert 'KingPartyHiddenWord9965.draw(' in s
assert 'KingPartyHiddenWord9965.spy(' in s
assert 'KingSocialVoteRoster9964.validBallot' in s
assert 'KingTttFairRoster9960.pickPair' in s
f.write_text(s)
(src/'KingPartyPrivateRoleLoad9966.java').write_text('''package com.kingplus.social;
/** Protect authenticated multiplayer private-role fetch from Firestore/render
 * feedback loops. One request per actual round until explicit Retry.
 */
public final class KingPartyPrivateRoleLoad9966 {
 private KingPartyPrivateRoleLoad9966(){}
 public static boolean needsRequest(long current,long lastAttempt,
    boolean pending,String knownRole,String knownSecret,String type){
   if(current<=0||current==lastAttempt||pending)return false;
   if("draw".equals(type))
     return knownSecret==null||knownSecret.isEmpty();
   if("werewolf".equals(type)||"spy".equals(type))
     return knownRole==null||knownRole.isEmpty();
   return false;
 }
}
''')
gradle=root/'app/build.gradle';v=gradle.read_text()
old="versionCode 244; versionName '9.9.65-random-private-spy-draw-word-test'"
assert v.count(old)==1,'must start from genuine full v9.9.65 signed APK source'
gradle.write_text(v.replace(old,
  "versionCode 245; versionName '9.9.66-live-draw-private-role-retry-test'"))
party=(src/'PartyActivity.java').read_text()
for k in ('KingPartyVoiceReconnect9957','KingPartyMicConsent9956',
          'KingGiftReceiptDurable9962','KingGiftBurstBackpressure9963'):
 assert k in party,k
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS real Draw role read only once per round, explicit server role retry after error, no replay loops')
