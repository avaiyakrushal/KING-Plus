#!/usr/bin/env python3
"""KING v9.9.31 independent Party entrance implementation from user video.
Apply only to verified v9.9.30 full release source; never silently weaken security.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1])
src=root/'app/src/main/java/com/kingplus/social/PartyActivity.java'
s=src.read_text()
def r(old,new):
 global s
 assert s.count(old)==1, ('unexpected source anchor',old[:90],s.count(old))
 s=s.replace(old,new,1)
r('    private boolean roomHasPassword;', '''    private boolean roomHasPassword;
    private String roomEntranceMode9931="legacy";''')
r('roomPrivate=priv; roomHasPassword=password; memberSeen=false;', 'roomPrivate=priv; roomHasPassword=password;roomEntranceMode9931="legacy"; memberSeen=false;')
r('''        openCloudRoom(d.getId(),str(d,"name","Live Party"),d.getString("ownerUid"),str(d,"ownerName","Host"),Boolean.TRUE.equals(d.getBoolean("isPrivate")),Boolean.TRUE.equals(d.getBoolean("hasPassword")));
    }

    private void tryPrivateEntry''', '''        openCloudRoom(d.getId(),str(d,"name","Live Party"),d.getString("ownerUid"),str(d,"ownerName","Host"),Boolean.TRUE.equals(d.getBoolean("isPrivate")),Boolean.TRUE.equals(d.getBoolean("hasPassword")));
        roomEntranceMode9931=str(d,"entranceMode","legacy");
    }

    private void tryPrivateEntry''')
r('else toast("Private room • invite access required");', '''else toast("family".equals(roomEntranceMode9931)?"Verified KING Family membership required":"follow".equals(roomEntranceMode9931)?"Host follow connection required":"Private room • invite access required");''')
r('roomPrivate=Boolean.TRUE.equals(doc.getBoolean("isPrivate"));roomHasPassword=Boolean.TRUE.equals(doc.getBoolean("hasPassword"));', 'roomPrivate=Boolean.TRUE.equals(doc.getBoolean("isPrivate"));roomHasPassword=Boolean.TRUE.equals(doc.getBoolean("hasPassword"));roomEntranceMode9931=str(doc,"entranceMode","legacy");')
r('String access=roomPrivate?(roomHasPassword?"🔑 Password":"🔐 Private"):"🌐 Public";', 'String access="family".equals(roomEntranceMode9931)?"👪 Family members":"follow".equals(roomEntranceMode9931)?"💜 Host follows":roomPrivate?(roomHasPassword?"🔑 Password":"🔐 Invite only"):"🌐 Free";')
r('items.add("👑 Co-host list"); items.add("✏ Rename room");', 'items.add("🚪 Channel Entrance Settings"); items.add("👑 Co-host list"); items.add("✏ Rename room");')
r('items.add("👑 Co-host list");\n            items.add("🎙 Mic seat count");', 'items.add("👑 Co-host list");\n            items.add("🚪 Channel Entrance Settings");\n            items.add("🎙 Mic seat count");')
r('else if(x.contains("Room settings"))roomSettingsPanel();', 'else if(x.contains("Channel Entrance Settings"))channelEntranceSettings9931();\n        else if(x.contains("Room settings"))roomSettingsPanel();')
r('else if(x.contains("Co-host list"))coHostListDialog();\n            else if(x.contains("Mic seat count"))changeSeatCount();', 'else if(x.contains("Co-host list"))coHostListDialog();\n            else if(x.contains("Channel Entrance Settings"))channelEntranceSettings9931();\n            else if(x.contains("Mic seat count"))changeSeatCount();')
r('    private void setPrivateRoom(boolean value){', '''
    private void channelEntranceSettings9931(){
        if(!isOwner()||!cloudRoom||db==null||user==null){toast("Live room Host only");return;}
        String[] items={"🌐 Free entry","👪 Verified KING Family members","🔑 Password required","💜 Following / followed by HOST","🔐 Invite by UID only"};
        int checked="family".equals(roomEntranceMode9931)?1:"password".equals(roomEntranceMode9931)?2:"follow".equals(roomEntranceMode9931)?3:roomPrivate?4:0;
        new AlertDialog.Builder(this).setTitle("🚪 Channel Entrance Settings")
        .setSingleChoiceItems(items,checked,(dialog,choice)->{
            dialog.dismiss();
            if(choice==0)updateEntrance9931("free","");
            else if(choice==1)familyEntrance9931();
            else if(choice==2)setRoomPasswordDialog();
            else if(choice==3)new AlertDialog.Builder(this).setTitle("Host Follow Connections")
                 .setMessage("Only signed-in accounts following the Host or followed by the Host can enter. This does NOT yet include all room members' followers. Existing visitors may lose access.")
                 .setNegativeButton("Cancel",null)
                 .setPositiveButton("Enable",(d,w)->updateEntrance9931("follow","")).show();
            else updateEntrance9931("invite","");
        }).setNegativeButton("Cancel",null).show();
    }
    private void familyEntrance9931(){
        db.collection("users").document(user.getUid()).get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(x->{
            String code=x==null?"":str(x,"familyCode","");
            if(code.isEmpty()){toast("Create or join a KING Family first");return;}
            db.collection("families").document(code).collection("members").document(user.getUid())
              .get(com.google.firebase.firestore.Source.SERVER)
              .addOnSuccessListener(m->{
                if(!m.exists()){toast("Family membership not verified");return;}
                new AlertDialog.Builder(this).setTitle("Family Only")
                  .setMessage("Only members of Family "+code+" may join. Nonmembers lose access, even if they were already seated. Requires Firestore rules deployment.")
                  .setNegativeButton("Cancel",null)
                  .setPositiveButton("Enable",(d,w)->updateEntrance9931("family",code)).show();
              }).addOnFailureListener(e->toast("Family membership check failed"));
          }).addOnFailureListener(e->toast("Family lookup failed"));
    }
    private void updateEntrance9931(String mode,String code){
        if(!isOwner()||!cloudRoom||db==null||roomId==null)return;
        if(!java.util.Arrays.asList("free","family","follow","invite").contains(mode))return;
        Map<String,Object> change=new HashMap<>();
        change.put("isPrivate",!"free".equals(mode));
        change.put("hasPassword",false);
        change.put("entranceMode",mode);
        change.put("familyCode","family".equals(mode)?code:"");
        change.put("updatedAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).update(change)
          .addOnSuccessListener(v->{roomPrivate=!"free".equals(mode);roomHasPassword=false;roomEntranceMode9931=mode;
            refreshRoomState();toast("Entrance mode: "+mode+" • server rules required");})
          .addOnFailureListener(e->toast("Entrance settings failed: "+msg(e)));
    }
    private void setPrivateRoom(boolean value){''')
r('roomPrivate=false;setRoomValue("isPrivate",false);toast("Room is public");refreshRoomState();return;', 'updateEntrance9931("free","");return;')
r('roomPrivate=true;setRoomValue("isPrivate",true);toast("Private room enabled • current members kept access");refreshRoomState();', 'updateEntrance9931("invite","");')
r('db.collection("live_rooms").document(roomId).update("isPrivate",true,"hasPassword",true,"updatedAt",FieldValue.serverTimestamp())', 'db.collection("live_rooms").document(roomId).update("isPrivate",true,"hasPassword",true,"entranceMode","password","familyCode","","updatedAt",FieldValue.serverTimestamp())')
r('roomPrivate=true;roomHasPassword=true;toast("Password room enabled • current members kept access");refreshRoomState();', 'roomPrivate=true;roomHasPassword=true;roomEntranceMode9931="password";toast("Password room enabled • current members kept access");refreshRoomState();')
r('db.collection("live_rooms").document(roomId).update("hasPassword",false,"updatedAt",FieldValue.serverTimestamp())', 'db.collection("live_rooms").document(roomId).update("hasPassword",false,"entranceMode","invite","updatedAt",FieldValue.serverTimestamp())')
r('roomHasPassword=false;toast("Password removed • room remains private");', 'roomHasPassword=false;roomEntranceMode9931="invite";toast("Password removed • room remains invite only");')
src.write_text(s)
rules=root/'firestore.rules';text=rules.read_text()
old="""    function roomAccess(roomId) {
      return activeUser()
        && !roomBanned(roomId, request.auth.uid)
        && (roomData(roomId).isPrivate != true
            || roomModerator(roomId)
            || exists(/databases/$(database)/documents/live_rooms/$(roomId)/access/$(request.auth.uid)));
    }
"""
new="""    function followConnection9931(roomId) {
      let host = roomData(roomId).ownerUid;
      let outgoing = /databases/$(database)/documents/follows/$(request.auth.uid + '__' + host);
      let incoming = /databases/$(database)/documents/follows/$(host + '__' + request.auth.uid);
      return (exists(outgoing) && get(outgoing).data.followerUid == request.auth.uid
              && get(outgoing).data.targetUid == host)
          || (exists(incoming) && get(incoming).data.followerUid == host
              && get(incoming).data.targetUid == request.auth.uid);
    }
    function roomAccess(roomId) {
      let room = roomData(roomId);
      let mode = room.get('entranceMode','legacy');
      let grant = exists(/databases/$(database)/documents/live_rooms/$(roomId)/access/$(request.auth.uid));
      return activeUser() && !roomBanned(roomId, request.auth.uid)
        && (roomModerator(roomId)
            || (mode == 'legacy' && (room.isPrivate != true || grant))
            || (mode == 'free' && room.isPrivate != true)
            || (mode == 'invite' && grant)
            || (mode == 'password' && grant)
            || (mode == 'family' && room.get('familyCode','') is string
                && room.get('familyCode','').size()>0 && familyMember(room.get('familyCode','')))
            || (mode == 'follow' && followConnection9931(roomId)));
    }
"""
assert text.count(old)==1
text=text.replace(old,new,1)
start=text.index('    match /follows/{followId} {')
front,tail=text[:start],text[start:]
q="""        && request.resource.data.targetUid != request.auth.uid;"""
assert tail.count(q)==1
text=front+tail.replace(q,"""        && request.resource.data.targetUid != request.auth.uid
        && followId == request.auth.uid + '__' + request.resource.data.targetUid;""",1)
rules.write_text(text)
gradle=root/'app/build.gradle';text=gradle.read_text()
v="versionCode 209; versionName '9.9.30-video-vip-preview-test'"
assert text.count(v)==1
gradle.write_text(text.replace(v,"versionCode 210; versionName '9.9.31-video-room-entrance-test'",1))
java=root/'app/src/main/java/com/kingplus/social'
assert 'LIVE_RECHARGE = false' in (java/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (java/'KingPremiumAccess981.java').read_text()
print('PASS v9.9.31 video-inspired entrance modes; payment remains disabled')
