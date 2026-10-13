#!/usr/bin/env python3
"""KING v9.9.51: real Party Tic Tac Toe turn/start reliability.
Reject cache/stale Ready players in a SERVER transaction, select two actually
joined players, eliminate obsolete move-query race and preserve legal moves
on transient host failures. Show the completed board on BOTH players' phones.
"""
from pathlib import Path
import sys
r=Path(sys.argv[1]);src=r/'app/src/main/java/com/kingplus/social'
p=src/'RoomGameActivity.java'
s=p.read_text()
begin=s.index('    private void startTtt864(){')
end=s.index('    private void submitTtt864(int idx){',begin)
assert 'game_ready").get()' in s[begin:end]
assert 'game_moves").get()' in s[begin:end]
replacement='''    private void startTtt864(){
        if(!moderator||!gameMemberVerified992||!gameStateVerified9943
           ||!"closed".equals(status)||me==null||db==null){
            toast("Wait for a live Host and a finished previous round");return;
        }
        final long previous9951=roundId;
        room().collection("game_ready")
           .get(com.google.firebase.firestore.Source.SERVER)
           .addOnSuccessListener(snapshot9951->{
            java.util.List<DocumentSnapshot> ready9951=new ArrayList<>(snapshot9951.getDocuments());
            Collections.sort(ready9951,(a,b)->
                s(a.getString("name")).compareToIgnoreCase(s(b.getString("name"))));
            if(ready9951.size()<2){toast("Two Party players must tap Ready");return;}
            if(ready9951.size()>50){toast("Too many Ready players");return;}
            final long next9951=Math.max(System.currentTimeMillis(),previous9951+1);
            // Firestore retries this transaction if either Ready or membership
            // changes. Never start against an offline/removed ghost player.
            db.runTransaction(tx->{
                DocumentSnapshot st9951=tx.get(state());
                Long stored9951=st9951.exists()?st9951.getLong("roundId"):null;
                if(st9951.exists()&&("active".equals(st9951.getString("status"))
                    ||stored9951==null||stored9951.longValue()!=previous9951))
                    throw new IllegalStateException("Party round changed");
                java.util.ArrayList<DocumentSnapshot> active9951=new java.util.ArrayList<>();
                for(DocumentSnapshot displayedReady9951:ready9951){
                    String uid9951=s(displayedReady9951.getId());
                    if(uid9951.isEmpty())continue;
                    DocumentSnapshot freshReady9951=tx.get(
                        room().collection("game_ready").document(uid9951));
                    DocumentSnapshot freshMember9951=tx.get(
                        room().collection("members").document(uid9951));
                    if(KingTttLiveRoster9951.valid(uid9951,
                        freshReady9951.exists(),
                        s(freshReady9951.getString("uid")),
                        freshMember9951.exists(),
                        s(freshMember9951.getString("uid"))))
                        active9951.add(freshReady9951);
                    if(active9951.size()>=2)break;
                }
                if(active9951.size()<2)
                    throw new IllegalStateException(
                        "Only live Ready Party members can start; ask two members to tap Ready");
                DocumentSnapshot x9951=active9951.get(0),o9951=active9951.get(1);
                String xu9951=x9951.getId(),ou9951=o9951.getId();
                String xn9951=s(x9951.getString("name")),on9951=s(o9951.getString("name"));
                if(xn9951.isEmpty())xn9951="Player X";
                if(on9951.isEmpty())on9951="Player O";
                Map<String,Object> value9951=new HashMap<>();
                value9951.put("actorUid",me.getUid());
                value9951.put("actorName",displayName);
                value9951.put("type","ttt");
                value9951.put("prompt",xn9951+" (X) vs "+on9951+" (O)");
                value9951.put("result","");
                value9951.put("status","active");
                value9951.put("roundId",next9951);
                value9951.put("board",".........");
                value9951.put("turnUid",xu9951);
                value9951.put("xUid",xu9951);
                value9951.put("oUid",ou9951);
                value9951.put("xName",xn9951);
                value9951.put("oName",on9951);
                value9951.put("participantUids",
                    java.util.Arrays.asList(xu9951,ou9951));
                value9951.put("updatedAt",FieldValue.serverTimestamp());
                // Clear ONLY the selected players' old move slots as part of
                // the same atomic round start, not a stale cached query.
                tx.delete(room().collection("game_moves").document(xu9951));
                tx.delete(room().collection("game_moves").document(ou9951));
                tx.set(state(),value9951);
                return xn9951+" (X) vs "+on9951+" (O)";
            }).addOnSuccessListener(names9951->
                postGameEvent750("❌⭕ "+displayName+
                    " started live Tic Tac Toe: "+names9951))
              .addOnFailureListener(e->toast("Tic Tac Toe not started: "+e.getMessage()));
        }).addOnFailureListener(e->toast("Live Ready players unavailable: "+e.getMessage()));
    }
'''
s=s[:begin]+replacement+s[end:]
old='''    private void renderState(){if(roundId==0||!"active".equals(status)){stateText.setText(result.isEmpty()?"No active multiplayer round":"Last result\\n"+result);return;}'''
new='''    private void renderState(){
        if(roundId>0&&"ttt".equals(type)&&!"active".equals(status)&&!result.isEmpty()){
            // A final X/O board must remain visible, not disappear when the
            // SERVER changes the round to closed after win/draw.
            stateText.setText("❌⭕ Final board\\n"+tttBoardText864()+"\\n"+result);
            return;
        }
        if(roundId==0||!"active".equals(status)){stateText.setText(result.isEmpty()?"No active multiplayer round":"Last result\\n"+result);return;}'''
assert s.count(old)==1,'expected existing renderState() anchor'
s=s.replace(old,new,1)
old2='''        if(hasMyMove())controls.addView(tv("Move sent • waiting for room host to confirm",13,MUTED,false));
    }
    private void startTtt864(){'''
new2='''        if(hasMyMove())controls.addView(tv("Move sent • waiting for room host to confirm",13,MUTED,false));
        if(moderator&&hasPendingTttMove9951()){
            Button retry9951=button("↻ Confirm pending Tic Tac Toe turn");
            retry9951.setOnClickListener(v->processTttMove864());
            controls.addView(retry9951,new LinearLayout.LayoutParams(-1,dp(48)));
        }
    }
    private boolean hasPendingTttMove9951(){
        for(DocumentSnapshot d:currentMoves)
            if(tttTurnUid864.equals(s(d.getString("uid")))
               &&s(d.getString("move")).startsWith("cell:"))return true;
        return false;
    }
    private void startTtt864(){'''
assert s.count(old2)==1,'missing TTT controls'
s=s.replace(old2,new2,1)
old3='''}).addOnSuccessListener(v->{tttProcessing864=false;}).addOnFailureListener(e->{tttProcessing864=false;try{moveRef.delete();}catch(Exception ignored){}});
'''
new3='''}).addOnSuccessListener(v->{tttProcessing864=false;}).addOnFailureListener(e->{
            // Do NOT silently delete the real player's valid move when a Host
            // momentarily loses connection. The Host can retry in this room.
            tttProcessing864=false;
            toast("Turn not confirmed yet • Host can tap Retry");
        });
'''
assert s.count(old3)==1,'expected host failure behavior'
s=s.replace(old3,new3,1)
p.write_text(s)
(src/'KingTttLiveRoster9951.java').write_text("""package com.kingplus.social;
/** Tiny auditable helper used on fresh SERVER transaction documents. */
public final class KingTttLiveRoster9951 {
  private KingTttLiveRoster9951(){}
  public static boolean valid(String expectedUid,
      boolean readyExists,String readyUid,boolean memberExists,String memberUid){
    return expectedUid!=null&&!expectedUid.isEmpty()
       &&readyExists&&memberExists
       &&expectedUid.equals(readyUid)&&expectedUid.equals(memberUid);
  }
}
""")
g=r/'app/build.gradle';v=g.read_text()
oldver="versionCode 229; versionName '9.9.50-real-backpack-gifts-test'"
assert v.count(oldver)==1
g.write_text(v.replace(oldver,
    "versionCode 230; versionName '9.9.51-real-ttt-party-membership-test'"))
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS real Tic Tac Toe live Ready transaction + recovery and final board')
