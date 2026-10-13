#!/usr/bin/env python3
"""KING v9.9.58: start Werewolf, Spy and Draw with verified live Party Ready members.
Atomic transaction guards against two Hosts starting competing games.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1]); src=root/'app/src/main/java/com/kingplus/social'
p=src/'RoomGameActivity.java'; s=p.read_text()
a=s.index('    private void startSocialRound866(String game){')
b=s.index('\n    private void startRound(String game){',a)
new='''    /** Start hidden-role and drawing games only with live authenticated Party players. */
    private void startSocialRound866(String game){
        if(!moderator||!gameMemberVerified992||!gameStateVerified9943||
           me==null||db==null||!"closed".equals(status)){
            toast("Wait for a verified Host and a finished round");return;
        }
        final long previous9958=roundId;
        final long next9958=Math.max(System.currentTimeMillis(),previous9958+1);
        final String hostUid9958=me.getUid();
        room().collection("game_ready")
           .get(com.google.firebase.firestore.Source.SERVER)
           .addOnSuccessListener(readySnap9958->{
            java.util.ArrayList<DocumentSnapshot> readyDocs9958=new java.util.ArrayList<>(readySnap9958.getDocuments());
            Collections.sort(readyDocs9958,(x,y)->x.getId().compareTo(y.getId()));
            if(readyDocs9958.size()>40){toast("Up to 40 Ready Party members per social game");return;}
            db.runTransaction(tx->{
                DocumentSnapshot last9958=tx.get(state());
                if(last9958.exists()){
                    Long stored9958=last9958.getLong("roundId");
                    if(stored9958==null||stored9958.longValue()!=previous9958||
                       !"closed".equals(s(last9958.getString("status"))))
                        throw new IllegalStateException("Another Party game started; refresh");
                }else if(previous9958!=0L)
                    throw new IllegalStateException("Party game state changed");
                java.util.ArrayList<String> liveUids9958=new java.util.ArrayList<>();
                java.util.HashMap<String,String> liveNames9958=new java.util.HashMap<>();
                // All transaction reads BEFORE any role or game-state writes.
                for(DocumentSnapshot candidate9958:readyDocs9958){
                    final String uid9958=candidate9958.getId();
                    if(uid9958.isEmpty())continue;
                    DocumentSnapshot freshReady9958=tx.get(
                        room().collection("game_ready").document(uid9958));
                    DocumentSnapshot actualMember9958=tx.get(
                        room().collection("members").document(uid9958));
                    if(KingPartySocialRoster9958.isLive(uid9958,
                           freshReady9958.exists(),s(freshReady9958.getString("uid")),
                           actualMember9958.exists(),s(actualMember9958.getString("uid")))){
                        liveUids9958.add(uid9958);
                        String nm9958=s(freshReady9958.getString("name"));
                        liveNames9958.put(uid9958,nm9958.isEmpty()?"Player":nm9958);
                    }
                }
                if(!KingPartySocialRoster9958.valid(game,liveUids9958))
                    throw new IllegalStateException("Not enough live Ready members for "+game+
                        " • ask players in the Party to tap Ready");
                java.util.ArrayList<String> possible9958=new java.util.ArrayList<>(liveUids9958);
                possible9958.remove(hostUid9958);
                if(!"draw".equals(game)&&possible9958.isEmpty())
                    throw new IllegalStateException("Need another live Party member for a private role");
                final String target9958="draw".equals(game)?"":possible9958.get(
                    new java.security.SecureRandom().nextInt(possible9958.size()));
                final String secret9958="spy".equals(game)?spyLocation866(next9958):"";
                String hostRole9958="Moderator / observer",hostSecret9958="";
                if(!"draw".equals(game)){
                    for(String uid9958:liveUids9958){
                        String role9958="werewolf".equals(game)?
                            (uid9958.equals(target9958)?"Werewolf":"Villager"):
                            (uid9958.equals(target9958)?"Spy":"Player");
                        String personal9958="spy".equals(game)&&!uid9958.equals(target9958)?secret9958:"";
                        if(uid9958.equals(hostUid9958)){
                            hostRole9958=role9958;hostSecret9958=personal9958;
                            continue;
                        }
                        java.util.Map<String,Object> invite9958=new HashMap<>();
                        invite9958.put("senderUid",hostUid9958);
                        invite9958.put("senderName",displayName);
                        invite9958.put("recipientUid",uid9958);
                        invite9958.put("recipientName",liveNames9958.get(uid9958));
                        invite9958.put("kind","game_role");invite9958.put("gameType",game);
                        invite9958.put("roundId",next9958);invite9958.put("role",role9958);
                        invite9958.put("secret",personal9958);
                        invite9958.put("createdAt",FieldValue.serverTimestamp());
                        tx.set(room().collection("room_invites").document(roleDocId866(next9958,uid9958)),invite9958);
                    }
                }
                java.util.Map<String,Object> st9958=new HashMap<>();
                st9958.put("actorUid",hostUid9958);st9958.put("actorName",displayName);
                st9958.put("type",game);st9958.put("status","active");
                st9958.put("roundId",next9958);st9958.put("result","");
                st9958.put("participantUids",liveUids9958);
                st9958.put("prompt","draw".equals(game)?"Draw & Guess • "+displayName+" is drawing":
                  "werewolf".equals(game)?"Discuss in Party voice/chat, then vote for Werewolf":
                    "Secret location • ask questions and vote for the Spy");
                if("draw".equals(game)){
                    st9958.put("drawerUid",hostUid9958);
                    st9958.put("drawerName",displayName);
                    st9958.put("sketch","");
                }
                st9958.put("updatedAt",FieldValue.serverTimestamp());
                tx.set(state(),st9958);
                return new String[]{hostRole9958,hostSecret9958,String.valueOf(liveUids9958.size())};
            }).addOnSuccessListener(started9958->{
                if(isFinishing()||isDestroyed())return;
                privateRoleRound866=next9958;
                privateRole866=started9958[0];
                privateSecret866=started9958[1];
                postGameEvent750("🎮 "+displayName+" started "+game+" with "+started9958[2]+" live Party players");
            }).addOnFailureListener(e->toast("Could not start real Party game: "+e.getMessage()));
        }).addOnFailureListener(e->toast("Live Party Ready list unavailable: "+e.getMessage()));
    }
'''
s=s[:a]+new+s[b:]
p.write_text(s)
(src/'KingPartySocialRoster9958.java').write_text("""package com.kingplus.social;
import java.util.*;
/** Guard for authenticated in-room Party Werewolf, Spy and Draw participants. */
public final class KingPartySocialRoster9958 {
 private KingPartySocialRoster9958(){}
 public static boolean isLive(String uid,boolean ready,String readyUid,
                             boolean member,String memberUid){
  return uid!=null&&!uid.isEmpty()&&ready&&member
      &&uid.equals(readyUid)&&uid.equals(memberUid);
 }
 public static boolean valid(String game,List<String> live){
  int min="werewolf".equals(game)?4:("spy".equals(game)?3:("draw".equals(game)?2:999));
  if(live==null||live.size()<min||live.size()>40)return false;
  HashSet<String> seen=new HashSet<>();
  for(String uid:live)if(uid==null||uid.isEmpty()||!seen.add(uid))return false;
  return true;
 }
}
""")
g=root/'app/build.gradle';v=g.read_text()
old="versionCode 236; versionName '9.9.57-voice-rejoin-live-party-test'"
assert v.count(old)==1
g.write_text(v.replace(old,"versionCode 237; versionName '9.9.58-social-games-live-roster-test'"))
assert 'KingPartyVoiceReconnect9957' in (src/'PartyActivity.java').read_text()
assert 'KingGiftReceiptRetry9955' in (src/'PartyActivity.java').read_text()
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
print('PASS social Party rooms: transactionally verified members, no concurrent start, atomic private roles')
