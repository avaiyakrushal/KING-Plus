#!/usr/bin/env python3
"""KING Plus v9.9.65: Spy/Draw words must not be predictable from room+round.
The round stores NO public answer; private role/invite stores randomly chosen
word, and each recipient can recover their own secret after Activity restart.
Both real game endings and wrong-guess filtering retain server-backed results.
No copyrighted BoloHi source/assets or real-money purchase changes.
"""
from pathlib import Path
import re,sys
root=Path(sys.argv[1]);src=root/'app/src/main/java/com/kingplus/social'
path=src/'RoomGameActivity.java'
s=path.read_text()

for method in ('spyLocation866','drawWord866'):
    pattern=r'^    private String '+method+r'\(long round\)\{[^\n]+\}\n'
    s,n=re.subn(pattern,'',s,flags=re.MULTILINE)
    assert n==1,f'Old public predictable word algorithm changed: {method} {n}'

a=s.index('    private void startSocialRound866(String game){')
b=s.index('    private void startRound(String game){',a)
start=s[a:b]
old='final String secret9958="spy".equals(game)?spyLocation866(next9958):"";'
new='final String secret9958="spy".equals(game)?KingPartyHiddenWord9965.spy(new java.security.SecureRandom()):"";'
assert start.count(old)==1,'Spy random draw anchor missing'
start=start.replace(old,new,1)
anchor='''                java.util.Map<String,Object> st9958=new HashMap<>();'''
assert start.count(anchor)==1,'Draw role injection location unavailable'
extra='''                if("draw".equals(game)){
                    // Make a secret ONCE for this accepted transaction; no
                    // public roomId/roundId hash can reveal it to spectators.
                    String word9965=KingPartyHiddenWord9965.draw(
                        new java.security.SecureRandom());
                    hostRole9958="Drawer";
                    hostSecret9958=word9965;
                    // Only the actual drawer receives the secret word. The
                    // invite remains retrievable after Activity recreation.
                    Map<String,Object> privateWord9965=new HashMap<>();
                    privateWord9965.put("senderUid",hostUid9958);
                    privateWord9965.put("senderName",displayName);
                    privateWord9965.put("recipientUid",hostUid9958);
                    privateWord9965.put("recipientName",liveNames9958.get(hostUid9958));
                    privateWord9965.put("kind","game_role");
                    privateWord9965.put("gameType","draw");
                    privateWord9965.put("roundId",next9958);
                    privateWord9965.put("role","Drawer");
                    privateWord9965.put("secret",word9965);
                    privateWord9965.put("createdAt",FieldValue.serverTimestamp());
                    tx.set(room().collection("room_invites")
                         .document(roleDocId866(next9958,hostUid9958)),privateWord9965);
                }
'''
start=start.replace(anchor,extra+anchor,1)
assert 'hostSecret9958=word9965' in start
assert 'st9958.put("prompt","draw"' in start
assert 'st9958.put("secret"' not in start and 'st9958.put("word"' not in start
s=s[:a]+start+s[b:]

a=s.index('    private void fetchPrivateRole866(){')
b=s.index('    private void hiddenRoleControls866(){',a)
fetch=s[a:b]
old='''if(me==null||db==null||roundId<=0||!("werewolf".equals(type)||"spy".equals(type)))return;'''
new='''if(me==null||db==null||roundId<=0||
            !("werewolf".equals(type)||"spy".equals(type)||
               ("draw".equals(type)&&me.getUid().equals(drawerUid866))))return;
        // Ensure a previous game's cached word never appears in this game.
        if(privateRoleRound866!=roundId){
            privateRoleRound866=roundId;privateRole866="";privateSecret866="";
        }'''
assert fetch.count(old)==1,'Existing private role loader changed'
fetch=fetch.replace(old,new,1)
# The private doc ID stays (round + uid). This resumes a real drawer after kill.
assert 'roleDocId866(expected,me.getUid())' in fetch
s=s[:a]+fetch+s[b:]

a=s.index('    private void drawGuessControls866(){')
b=s.index('    private void publishDrawSketch866(',a)
draw=s[a:b]
old='''String title'''
# Method rendered as one large line; replace actual predictable text expression:
old='''("🎨 You are drawing • secret word: "+drawWord866(roundId))'''
new='''("🎨 You are drawing • secret word: "+
            (privateRoleRound866==roundId&&!privateSecret866.isEmpty()?
             privateSecret866:"Loading your private word…"))'''
assert draw.count(old)==1,'Draw secret UI anchor changed'
draw=draw.replace(old,new,1)
old='''drawPad866=new DrawPad866(drawSketch866,drawer&&moderator);'''
new='''if(drawer&&(privateRoleRound866!=roundId||privateSecret866.isEmpty()))
            fetchPrivateRole866();
        drawPad866=new DrawPad866(drawSketch866,drawer&&moderator
            &&privateRoleRound866==roundId&&!privateSecret866.isEmpty());'''
assert draw.count(old)==1,'Draw canvas editor anchor changed'
draw=draw.replace(old,new,1)
s=s[:a]+draw+s[b:]

a=s.index('    private void finishDraw866(){')
b=s.index('    private void closeSocialRound866(String out){',a)
finish='''    /** Actual answer comes only from the private drawer invite on SERVER.
     * All recorded guesses must be current-round, signed-UID participants.
     */
    private void finishDraw866(){
        if(!moderator||me==null||db==null||!"draw".equals(type)||roundId<=0)return;
        final long expected=roundId;
        final String drawer=drawerUid866;
        if(drawer==null||drawer.isEmpty()){toast("No live drawer");return;}
        state().get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(current9965->{
            if(!KingSocialVoteRoster9964.isCurrent(current9965.exists(),
                    current9965.getLong("roundId"),expected,
                    s(current9965.getString("status")),
                    s(current9965.getString("type")),"draw"))return;
            final java.util.List<String> frozen9965=
                KingSocialVoteRoster9964.roster(current9965.get("participantUids"));
            if(!frozen9965.contains(drawer)){toast("Drawer left the round");return;}
            room().collection("room_invites").document(roleDocId866(expected,drawer))
              .get(com.google.firebase.firestore.Source.SERVER)
              .addOnSuccessListener(invite9965->{
                if(roundId!=expected||!invite9965.exists()||
                  !"draw".equals(s(invite9965.getString("gameType")))||
                  !"Drawer".equals(s(invite9965.getString("role")))||
                  !drawer.equals(s(invite9965.getString("recipientUid")))||
                  invite9965.getLong("roundId")==null||
                  invite9965.getLong("roundId").longValue()!=expected){
                    toast("Private draw word not available");return;
                }
                final String answer=s(invite9965.getString("secret"));
                if(answer.isEmpty()){toast("Private draw word is empty");return;}
                room().collection("game_votes")
                  .get(com.google.firebase.firestore.Source.SERVER)
                  .addOnSuccessListener(q->{
                    if(roundId!=expected)return;
                    List<String> winners=new ArrayList<>();
                    int total=0;
                    for(DocumentSnapshot d:q.getDocuments()){
                        Long r=d.getLong("roundId");
                        String id=d.getId(),claimed=s(d.getString("uid"));
                        if(r==null||r.longValue()!=expected||
                          !id.equals(claimed)||!frozen9965.contains(id)||
                          id.equals(drawer))continue;
                        total++;
                        if(answer.equalsIgnoreCase(s(d.getString("choice")).trim()))
                            winners.add(s(d.getString("name")));
                    }
                    final String out="🎨 Word: "+answer+"\\n"+
                      (winners.isEmpty()?"No correct guess":"🏆 Correct: "+join(winners))+
                      "\\nGuesses: "+total;
                    closeSocialRound866(out);
                  }).addOnFailureListener(e->toast("Could not read guesses"));
              }).addOnFailureListener(e->toast("Private draw word unavailable"));
          }).addOnFailureListener(e->toast("Active draw round unavailable"));
    }
'''
s=s[:a]+finish+s[b:]

# Explicitly stop the drawer from submitting guesses in normal app UI.
s=s.replace('''if(me==null||roundId<=0)return;String guess=box==null?''',
'''if(me==null||roundId<=0||("draw".equals(type)
            &&me.getUid().equals(drawerUid866)))return;String guess=box==null?''',1)

assert 'drawWord866' not in s and 'spyLocation866' not in s
assert 'KingSocialVoteRoster9964.validVoteDocument' in s
assert 'KingTttFairRoster9960.pickPair' in s
path.write_text(s)

(src/'KingPartyHiddenWord9965.java').write_text('''package com.kingplus.social;
import java.security.SecureRandom;
/** Generate private server-readable role invites; never derive a secret
 * from public roomId, roundId or a predictable hash.
 * Honest-client secrecy only: trusted backend needed for high-value games.
 */
public final class KingPartyHiddenWord9965 {
 private KingPartyHiddenWord9965(){}
 private static final String[] SPY={
   "Airport","Beach","Hospital","School","Cinema","Hotel",
   "Market","Train Station","Restaurant","Zoo","Office","Temple"};
 private static final String[] DRAW={
   "TIGER","CROWN","DIAMOND","ROCKET","HEART","CASTLE",
   "FLOWER","MOON","PHONE","LUDO","LION","STAR"};
 public static String spy(SecureRandom rng){return select(SPY,rng);}
 public static String draw(SecureRandom rng){return select(DRAW,rng);}
 private static String select(String[] choices,SecureRandom rng){
   if(rng==null)throw new IllegalArgumentException("Secure random required");
   return choices[rng.nextInt(choices.length)];
 }
}
''')
g=root/'app/build.gradle';v=g.read_text()
old="versionCode 243; versionName '9.9.64-real-social-vote-security-test'"
assert v.count(old)==1,'Expected full signed v9.9.64 Android source'
g.write_text(v.replace(old,
    "versionCode 244; versionName '9.9.65-random-private-spy-draw-word-test'"))
party=(src/'PartyActivity.java').read_text()
for token in ('KingGiftBurstBackpressure9963','KingGiftReceiptDurable9962',
              'KingPartyMicConsent9956','KingPartyVoiceReconnect9957'):
    assert token in party,token
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS genuine Party Spy and Draw answers privately randomized; rejoin recovers secret without public hash, Gifts/RTC unchanged')
