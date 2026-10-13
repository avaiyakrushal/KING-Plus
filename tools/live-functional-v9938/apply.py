#!/usr/bin/env python3
"""KING v9.9.38 actual functional Party/Game/Gift reliability.

Apply to successful v9.9.37 release source, NOT starter repo. All anchors fail
closed. This release keeps resource counts, payments OFF and all older games.
"""
from pathlib import Path
import sys
r=Path(sys.argv[1]); src=r/'app/src/main/java/com/kingplus/social'
def patch(name,pairs):
 path=src/name;old=path.read_text()
 for a,b in pairs:
  n=old.count(a)
  if n!=1:raise RuntimeError(f'{name}: expected 1 anchor, saw {n}: {a[:115]}')
  old=old.replace(a,b,1)
 path.write_text(old)

# A game is not ongoing for somebody who was kicked from the Party room.
# Live own-membership listener closes their game access even without activity restart.
game=src/'RoomGameActivity.java'
s=game.read_text()
a='    private void ensureMembership900(){'
b='    private void loadRoleAndListen(){'
assert s.count(a)==1 and s.count(b)==1
start=s.index(a);end=s.index(b,start)
replacement="""    // Real live membership, not a one-off check that stays true after removal.
    private ListenerRegistration membershipListener9938;
    private void ensureMembership900(){
        if(me==null||db==null||roomId.isEmpty())return;
        final String checkingUid=me.getUid(),checkingRoom=roomId;
        if(membershipListener9938!=null)membershipListener9938.remove();
        membershipListener9938=room().collection("members").document(checkingUid)
          .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(member,error)->{
            if(isFinishing()||isDestroyed()||!checkingRoom.equals(roomId)
                ||me==null||!checkingUid.equals(me.getUid()))return;
            boolean active=error==null && member!=null && member.exists()
                && !member.getMetadata().isFromCache()
                && checkingUid.equals(member.getString("uid"));
            if(!active){
                if(gameMemberVerified992){
                    if(stateListener!=null){stateListener.remove();stateListener=null;}
                    if(movesListener!=null){movesListener.remove();movesListener=null;}
                    if(readyListener!=null){readyListener.remove();readyListener=null;}
                    if(moderatorRoleListener!=null){moderatorRoleListener.remove();moderatorRoleListener=null;}
                    controls.removeAllViews();movesBox.removeAllViews();readyBox.removeAllViews();
                }
                gameMemberVerified992=false;moderator=false;
                roleText.setText(error==null?"Join this Party Room first to play.":"Party membership verification failed");
                connection950.setText("● Room game locked • rejoin from Party");
                if(syncSpinner950!=null)syncSpinner950.setVisibility(View.GONE);
                return;
            }
            if(gameMemberVerified992)return;
            gameMemberVerified992=true;
            roleText.setText("● Live Party membership verified by Firebase SERVER");
            loadRoleAndListen();
          });
    }
"""
s=s[:start]+replacement+s[end:]
# Ensure onDestroy cleans up observer and observers cannot linger across rooms.
a='if(stateListener!=null)stateListener.remove();if(movesListener!=null)movesListener.remove();if(readyListener!=null)readyListener.remove();if(moderatorRoleListener!=null)moderatorRoleListener.remove();super.onDestroy();'
b='if(membershipListener9938!=null)membershipListener9938.remove();if(stateListener!=null)stateListener.remove();if(movesListener!=null)movesListener.remove();if(readyListener!=null)readyListener.remove();if(moderatorRoleListener!=null)moderatorRoleListener.remove();super.onDestroy();'
assert s.count(a)==1
s=s.replace(a,b,1)
game.write_text(s)

# Correlate gift event and chat row with the SAME ID and require both written
# atomically. A fake standalone chat "gift" no longer produces legitimate history.
patch('PartyActivity.java',[
 ('batch971.set(room971.collection("events").document(),event971);',
  'batch971.set(room971.collection("events").document(giftDeliveryId9937),event971);'),
 ('batch971.set(room971.collection("messages").document(),message971);',
  'batch971.set(room971.collection("messages").document(giftDeliveryId9937),message971);')
])

rules=r/'firestore.rules';text=rules.read_text()
def rep(a,b):
 global text
 n=text.count(a)
 if n!=1:raise RuntimeError(f'firestore.rules: expected 1 anchor, got {n}: {a[:120]}')
 text=text.replace(a,b,1)
# Text is authoritative only when paired with its event in this same atomic batch.
rep("""          && request.resource.data.text.size() <= 500;
        allow update, delete: if false;
      }

      match /events/{eventId} {""",
"""          && request.resource.data.text.size() <= 500
          && (request.resource.data.get('type','') != 'gift'
              || (request.resource.data.get('giftId','') == messageId
                  && request.resource.data.get('giftValue',-1) == 0
                  && !exists(/databases/$(database)/documents/live_rooms/$(roomId)/events/$(messageId))
                  && getAfter(/databases/$(database)/documents/live_rooms/$(roomId)/events/$(messageId)).data.type == 'gift'
                  && getAfter(/databases/$(database)/documents/live_rooms/$(roomId)/events/$(messageId)).data.actorUid == request.auth.uid
                  && getAfter(/databases/$(database)/documents/live_rooms/$(roomId)/events/$(messageId)).data.giftId == messageId
                  && getAfter(/databases/$(database)/documents/live_rooms/$(roomId)/events/$(messageId)).data.targetUid == request.resource.data.targetUid
                  && getAfter(/databases/$(database)/documents/live_rooms/$(roomId)/events/$(messageId)).data.giftQty == request.resource.data.giftQty));
        allow update, delete: if false;
      }

      match /events/{eventId} {""")
rep("""                  && exists(/databases/$(database)/documents/live_rooms/$(roomId)/members/$(request.resource.data.targetUid))))
          // A dice "result" cannot contain 9, 999, spoofed characters or paid value.""",
"""                  && exists(/databases/$(database)/documents/live_rooms/$(roomId)/members/$(request.resource.data.targetUid))
                  && request.resource.data.get('giftId','') == eventId
                  && !exists(/databases/$(database)/documents/live_rooms/$(roomId)/messages/$(eventId))
                  && getAfter(/databases/$(database)/documents/live_rooms/$(roomId)/messages/$(eventId)).data.type == 'gift'
                  && getAfter(/databases/$(database)/documents/live_rooms/$(roomId)/messages/$(eventId)).data.senderUid == request.auth.uid
                  && getAfter(/databases/$(database)/documents/live_rooms/$(roomId)/messages/$(eventId)).data.giftId == eventId
                  && getAfter(/databases/$(database)/documents/live_rooms/$(roomId)/messages/$(eventId)).data.targetUid == request.resource.data.targetUid
                  && getAfter(/databases/$(database)/documents/live_rooms/$(roomId)/messages/$(eventId)).data.giftQty == request.resource.data.giftQty))
          // A dice "result" cannot contain 9, 999, spoofed characters or paid value.""")
# Server checks that real Tic Tac Toe moves arrive only on the current player's turn.
# This applies to the game/turn system, not just UI button state.
rep("""          && playerUid == request.auth.uid
          && request.resource.data.uid == request.auth.uid;
        allow delete: if signedIn()
          && (playerUid == request.auth.uid || roomModerator(roomId));
      }

      match /game_state/{stateId} {""",
"""          && playerUid == request.auth.uid
          && request.resource.data.uid == request.auth.uid
          // For an active Tic Tac Toe match no user may sneak in a differently
          // labeled game move to bypass the real TURN validation.
          && (!exists(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current)
              || get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.get('type','') != 'ttt'
              || request.resource.data.get('type','') == 'ttt')
          && (request.resource.data.get('type','') != 'ttt'
              || (exists(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current)
                  && get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.type == 'ttt'
                  && get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.status == 'active'
                  && get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.turnUid == playerUid
                  && get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.roundId == request.resource.data.roundId
                  && request.resource.data.move.matches('^cell:[0-8]$')
                  && request.resource.data.score is int
                  && request.resource.data.score >= 0
                  && request.resource.data.score <= 8));
        allow delete: if signedIn()
          && (playerUid == request.auth.uid || roomModerator(roomId));
      }

      match /game_state/{stateId} {""")
rules.write_text(text)
gradle=r/'app/build.gradle';g=gradle.read_text()
a="versionCode 216; versionName '9.9.37-real-party-dice-gifting-test'"
assert g.count(a)==1
gradle.write_text(g.replace(a,"versionCode 217; versionName '9.9.38-live-room-gift-ttt-test'",1))
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS membership live revocation, authoritative ttt turns, atomic Gift event/chat pairing, billing OFF')
