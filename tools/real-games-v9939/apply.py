#!/usr/bin/env python3
"""Actual KING Plus v9.9.39: bind Party Ludo to live Party, revoke Knife on kick.

This changes existing functional code and authoritative Firestore rules, not
resource counts; original Bolo Hi assets/code are not included.
"""
from pathlib import Path
import sys
r=Path(sys.argv[1]);src=r/'app/src/main/java/com/kingplus/social'
def change(path,old,new,occ=1):
 t=path.read_text();found=t.count(old)
 if found!=occ:raise RuntimeError(f'Expected {occ} anchors in {path.name}; found {found}: {old[:110]}')
 path.write_text(t.replace(old,new,occ))
ludo=src/'OnlineLudoActivity.java'
old='''    private void openPartyMatch862(String c,boolean canCreate,String partyRoomId,String partyRoomName){
        if(c.length()!=6)'''
new='''    private void openPartyMatch862(String c,boolean canCreate,String partyRoomId,String partyRoomName){
        if(partyRoomId==null||partyRoomId.trim().isEmpty()){
            status("Open Party Ludo from a real KING Party Room",0xffffa6a6);return;
        }
        if(me==null||db==null){status("Sign in to join Party Ludo",0xffffa6a6);return;}
        final String uid9939=me.getUid();
        // Server membership is required before even attempting to discover
        // the Party-specific Ludo match. Security rules below enforce it too.
        db.collection("live_rooms").document(partyRoomId)
          .collection("members").document(uid9939)
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(m->{
              if(isFinishing()||isDestroyed()||me==null||!uid9939.equals(me.getUid()))return;
              if(m==null||!m.exists()||!uid9939.equals(m.getString("uid"))){
                  status("Join the live KING Party before opening this Ludo match",0xffffa6a6);
                  return;
              }
              openPartyMatchVerified9939(c,canCreate,partyRoomId,partyRoomName);
          }).addOnFailureListener(e->status("Could not verify Party membership: "+e.getMessage(),0xffffa6a6));
    }
    private void openPartyMatchVerified9939(String c,boolean canCreate,String partyRoomId,String partyRoomName){
        if(c.length()!=6)'''
change(ludo,old,new)
knife=src/'KingKnifeArena9923Activity.java'
change(knife,'  private ListenerRegistration leaderboardListener;','  private ListenerRegistration leaderboardListener;\n  private ListenerRegistration membershipListener9939;')
change(knife,'''          listenBoard();
        }).addOnFailureListener''','''          listenBoard();
          watchLiveMembership9939(currentUid,currentRoom);
        }).addOnFailureListener''')
watch='''  private void watchLiveMembership9939(String uid,String currentRoom){
    if(membershipListener9939!=null)membershipListener9939.remove();
    membershipListener9939=db.collection("live_rooms").document(currentRoom)
      .collection("members").document(uid)
      .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(member,error)->{
        if(!alive())return;
        boolean joined=error==null&&member!=null&&member.exists()
          &&!member.getMetadata().isFromCache()
          &&uid.equals(member.getString("uid"));
        if(!joined){
          verified=false;running=false;
          startButton.setEnabled(false);
          if(leaderboardListener!=null){leaderboardListener.remove();leaderboardListener=null;}
          connection.setText("● Party membership lost • game paused");
          status.setText("Rejoin the Party Room to play and submit Knife score");
        }else if(!verified){
          verified=true;
          connection.setText("● Live membership verified • Knife game ready");
          startButton.setEnabled(!busy);
          listenBoard();
        }
      });
  }
'''
change(knife,'  private void layout(){',watch+'  private void layout(){')
change(knife,'''    if(leaderboardListener!=null)leaderboardListener.remove();
    super.onDestroy();''',
'''    if(leaderboardListener!=null)leaderboardListener.remove();
    if(membershipListener9939!=null)membershipListener9939.remove();
    super.onDestroy();''')
# Existing score sends must never reactivate START after revocation.
change(knife,'busy=false;if(!alive())return;startButton.setEnabled(true);',
             'busy=false;if(!alive())return;startButton.setEnabled(verified);',occ=2)
rules=r/'firestore.rules'
change(rules,
"""      allow read: if activeUser();
      allow create: if activeUser()
        && request.resource.data.code == matchCode""",
"""      allow read: if activeUser()
        && (resource == null
          || resource.data.get('partyRoomKey','') == ''
          || (roomMember(resource.data.get('partyRoomKey',''))
              && roomAccess(resource.data.get('partyRoomKey',''))));
      allow create: if activeUser()
        // Standalone online Ludo works unchanged. A Party Ludo match may only
        // be opened by a live Party Host/Moderator who is still a member.
        && (request.resource.data.get('partyRoomKey','') == ''
            || (roomMember(request.resource.data.get('partyRoomKey',''))
                && roomAccess(request.resource.data.get('partyRoomKey',''))
                && roomModerator(request.resource.data.get('partyRoomKey',''))))
        && request.resource.data.code == matchCode""")
change(rules,
"""      allow update: if activeUser()
        && request.resource.data.code == resource.data.code""",
"""      allow update: if activeUser()
        // Binding cannot be switched to bypass a Party Room's join policy.
        && request.resource.data.get('partyRoomKey','')
             == resource.data.get('partyRoomKey','')
        && (resource.data.get('partyRoomKey','') == ''
            || (roomMember(resource.data.get('partyRoomKey',''))
                && roomAccess(resource.data.get('partyRoomKey',''))))
        && request.resource.data.code == resource.data.code""")
gradle=r/'app/build.gradle';t=gradle.read_text()
base="versionCode 217; versionName '9.9.38-live-room-gift-ttt-test'"
assert t.count(base)==1,"Not verified full v9.9.38"
gradle.write_text(t.replace(base,"versionCode 218; versionName '9.9.39-party-ludo-knife-membership-test'"))
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS original Party Ludo joins now require member authorization, and Knife live membership revokes access')
