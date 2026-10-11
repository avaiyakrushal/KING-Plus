#!/usr/bin/env python3
"""KING Plus 9.9.0 consolidated runtime reliability upgrades.

Verified v9.8.7 signed source base. Safe enhancements in four categories:
1 Party microphone requires Android permission before server Mic-On request.
2 Live reactions discard unconfirmed/old event timestamps; FREE gift animation
  plays on sender only after server batch confirms, not on a failed send.
3 Room games verify an existing Firestore SERVER member; never manufacture
  Party membership just because user opened Games, and only delete ready if verified.
4 Recharge screen refuses to call a nonexistent/cached wallet "verified".
No changes to billing flags, Firebase security rules, private room access, or OTP.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1]);pkg=root/'app/src/main/java/com/kingplus/social'
party=pkg/'PartyActivity.java';games=pkg/'RoomGameActivity.java'
wallet=pkg/'KingRecharge974Activity.java';gradle=root/'app/build.gradle'
def change(file,old,new,label):
    s=file.read_text()
    count=s.count(old)
    if count!=1:raise SystemExit(f"FAILED {label}: expected exactly one anchor, found {count}, target {file}")
    file.write_text(s.replace(old,new,1));print("PASS",label)

# Mic privacy: cannot claim "Mic ON" without RECORD_AUDIO Android runtime grant.
change(party,
'''        final boolean desiredMic968=!micOn;
        if(db==null||!memberSeen''',
'''        final boolean desiredMic968=!micOn;
        if(desiredMic968 && android.os.Build.VERSION.SDK_INT>=23 &&
                checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
                  !=android.content.pm.PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO},992);
            toast("Allow microphone access, then tap Mic again to speak.");
            return;
        }
        if(db==null||!memberSeen''',
"Microphone permission must precede server Mic-On write")

# Timestamp can be null in pending local writes or absent from historical data.
# The sender renders after its successful batch; other phones render once from
# server timestamp only. Ignore stale cached emoji events.
change(party,
'''                if(tsV530 instanceof com.google.firebase.Timestamp){long ageV530=System.currentTimeMillis()-((com.google.firebase.Timestamp)tsV530).toDate().getTime();if(ageV530>12000)continue;}''',
'''                if(!(tsV530 instanceof com.google.firebase.Timestamp))continue;
                long ageV530=System.currentTimeMillis()-((com.google.firebase.Timestamp)tsV530).toDate().getTime();
                if(ageV530>12000||ageV530 < -10000)continue;''',
"Do not replay stale or unconfirmed live emoji animations")

# Gifts are free decorative animations. Do not claim "paid securely" if a
# Firestore batch fails and show sender effect only once after server commit.
change(party,
'''        batch971.commit().addOnFailureListener(e->{
            if(!isFinishing()&&!isDestroyed())
                toast("Gift paid securely but Party animation/history could not sync: "+msg(e));
        });''',
'''        batch971.commit().addOnSuccessListener(v->{
            if(isActivePresence967(requestedRoom971,expectedUid971,generation971)){
                showGiftEffect(actor971,recipient971,gift,icon,quantity,0);
                toast("FREE gift delivered to the Party");
            }
        }).addOnFailureListener(e->{
            if(isActivePresence967(requestedRoom971,expectedUid971,generation971))
                toast("FREE gift was not sent: "+msg(e));
        });''',
"FREE gift shows to sender after verified Room event write only")

# RoomGameActivity used to create/update Party membership just by opening Game!
# Verify pre-existing actual server membership; Game must never JOIN Room.
change(games,
'''    private final List<DocumentSnapshot> currentMoves=new ArrayList<>();''',
'''    private final List<DocumentSnapshot> currentMoves=new ArrayList<>();
    private boolean gameMemberVerified992=false;''',
"Track server-confirmed Party membership within Games")

change(games,
'''        if(me!=null&&db!=null&&!roomId.isEmpty())try{room().collection("game_ready").document(me.getUid()).delete();}catch(Exception ignored){}''',
'''        if(gameMemberVerified992&&me!=null&&db!=null&&!roomId.isEmpty())
            try{room().collection("game_ready").document(me.getUid()).delete();}
            catch(Exception ignored){}''',
"Only verified Party member can delete own Game Ready state on closing")

change(games,
'''    private void ensureMembership900(){
        Map<String,Object>m=new HashMap<>();m.put("uid",me.getUid());m.put("name",displayName.length()>80?displayName.substring(0,80):displayName);m.put("online",true);m.put("appVersion","9.0.0");m.put("lastSeenAt",FieldValue.serverTimestamp());
        room().collection("members").document(me.getUid()).set(m,com.google.firebase.firestore.SetOptions.merge()).addOnSuccessListener(v->{roleText.setText("● Multiplayer backend connected • large-room ready");loadRoleAndListen();}).addOnFailureListener(e->{roleText.setText("Membership failed: "+e.getMessage());if(syncSpinner950!=null)syncSpinner950.setVisibility(View.GONE);});
    }''',
'''    private void ensureMembership900(){
        if(me==null||db==null||roomId.isEmpty())return;
        final String checkingUid992=me.getUid();
        final String checkingRoom992=roomId;
        room().collection("members").document(checkingUid992)
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(doc->{
              if(isFinishing()||isDestroyed()||!checkingRoom992.equals(roomId)
                  ||me==null||!checkingUid992.equals(me.getUid()))return;
              if(doc==null||!doc.exists()||!checkingUid992.equals(doc.getString("uid"))){
                  gameMemberVerified992=false;
                  roleText.setText("Join this Party Room first. Games cannot create membership.");
                  connection950.setText("● Room member verification required");
                  if(syncSpinner950!=null)syncSpinner950.setVisibility(View.GONE);
                  return;
              }
              gameMemberVerified992=true;
              roleText.setText("● Party membership confirmed by Firebase SERVER");
              loadRoleAndListen();
          }).addOnFailureListener(e->{
              if(isFinishing()||isDestroyed())return;
              gameMemberVerified992=false;
              roleText.setText("Cannot verify Party membership: "+e.getMessage());
              connection950.setText("● Connection blocked • retry from Party");
              if(syncSpinner950!=null)syncSpinner950.setVisibility(View.GONE);
          });
    }''',
"Games must read existing member from Firestore SERVER without forging Party join")

change(games,
'''private void loadRoleAndListen(){room().get().addOnSuccessListener(d->{''',
'''private void loadRoleAndListen(){room().get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(d->{''',
"Host game role requires a server-confirmed Room read")

# Wallet: absence of server record or a cached snapshot is NOT 0 verified
# Diamonds. Keep all purchases disabled; no client-side minting.
change(wallet,
'''                if(error!=null){status.setText("Verified wallet unavailable. No Diamonds have been changed.");return;}
                long coins=doc==null||doc.getLong("coins")==null?0:Math.max(0,doc.getLong("coins"));''',
'''                if(error!=null){
                    balance.setText("💎 Verified Diamonds: unavailable");
                    vip.setText("👑 VIP: unavailable");
                    status.setText("Wallet server read failed; no Diamonds changed.");return;
                }
                if(doc==null||!doc.exists()||doc.getMetadata().isFromCache()||
                   doc.getMetadata().hasPendingWrites()){
                    balance.setText("💎 Verified Diamonds: not established");
                    vip.setText("👑 VIP: not established");
                    status.setText("No SERVER-confirmed wallet exists for this account. Payments remain OFF.");
                    return;
                }
                long coins=doc.getLong("coins")==null?0:Math.max(0,doc.getLong("coins"));''',
"Never display cached or nonexistent Wallet as verified zero")

change(gradle,
"versionCode 178; versionName '9.8.7-faster-new-host-party-join'",
"versionCode 179; versionName '9.9.0-room-emoji-games-wallet-reliability'",
"Upgrade v9.8.7 to consolidated v9.9.0")
print("PASS KING Plus 9.9.0 four-category batch prepared. Payments are still OFF.")
