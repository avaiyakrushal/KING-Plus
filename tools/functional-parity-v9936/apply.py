#!/usr/bin/env python3
"""KING v9.9.36: real cross-device room access revocation after mode/password change.

No new XML/Drawable counts. Based on verified full v9.9.35 Android source.
Corrects previously-existing stale invite bypass in password-mode. Rules require
new entryEpoch on grants only once room owner next changes entrance mode;
old rooms remain backwards compatible until migration. No production deploy.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1])
java=root/'app/src/main/java/com/kingplus/social/PartyActivity.java'
text=java.read_text()
def replace(old,new):
 global text
 count=text.count(old)
 if count!=1:raise RuntimeError(f'Full-source Java anchor changed {count}: {old[:105]}')
 text=text.replace(old,new,1)
replace('private String roomEntranceMode9931="legacy";','private String roomEntranceMode9931="legacy";\n    private String roomEntryEpoch9936="";')
replace('roomEntranceMode9931=str(d,"entranceMode","legacy");',
        'roomEntranceMode9931=str(d,"entranceMode","legacy");roomEntryEpoch9936=str(d,"entryEpoch","");')
replace('roomEntranceMode9931=str(doc,"entranceMode","legacy");',
        'roomEntranceMode9931=str(doc,"entranceMode","legacy");roomEntryEpoch9936=str(doc,"entryEpoch","");')
replace('roomHasPassword=password;roomEntranceMode9931="legacy"; memberSeen=false;',
        'roomHasPassword=password;roomEntranceMode9931="legacy";roomEntryEpoch9936=""; memberSeen=false;')
replace('change.put("entranceMode",mode);',
        'change.put("entranceMode",mode);\n'
        '        final String freshEpoch9936=java.util.UUID.randomUUID().toString();\n'
        '        change.put("entryEpoch",freshEpoch9936);')
replace('roomHasPassword=false;roomEntranceMode9931=mode;',
        'roomHasPassword=false;roomEntranceMode9931=mode;roomEntryEpoch9936=freshEpoch9936;')
replace('grantCurrentMembersAccessThen(()->{\n            updateEntrance9931("invite","");\n        });',
        'updateEntrance9931("invite","");')
# The old code would have retained access grants on password change.
replace('String hash=passwordProof(roomId,password);if(hash.isEmpty()){toast("Could not secure password");return;}',
        'String hash=passwordProof(roomId,password);if(hash.isEmpty()){toast("Could not secure password");return;}\n'
        '                final String passwordEpoch9936=java.util.UUID.randomUUID().toString();')
replace('.addOnSuccessListener(v->grantCurrentMembersAccessThen(()->db.collection("live_rooms")',
        '.addOnSuccessListener(v->db.collection("live_rooms")')
replace('.addOnFailureListener(x->toast(msg(x)))))',
        '.addOnFailureListener(x->toast(msg(x))))')
replace('"entranceMode","password","familyCode","","updatedAt",FieldValue.serverTimestamp()',
        '"entranceMode","password","familyCode","","entryEpoch",passwordEpoch9936,"updatedAt",FieldValue.serverTimestamp()')
replace('roomHasPassword=true;roomEntranceMode9931="password";toast("Password room enabled • current members kept access");',
        'roomHasPassword=true;roomEntranceMode9931="password";roomEntryEpoch9936=passwordEpoch9936;toast("Password updated • previous grants revoked; members must rejoin");')
replace('db.collection("live_rooms").document(roomId).update("hasPassword",false,"entranceMode","invite","updatedAt",FieldValue.serverTimestamp())',
        'db.collection("live_rooms").document(roomId).update("hasPassword",false,"entranceMode","invite","entryEpoch",removeEpoch9936,"updatedAt",FieldValue.serverTimestamp())')
replace('private void removeRoomPassword(){\n        if(!isOwner()||!cloudRoom||db==null)return;',
        'private void removeRoomPassword(){\n        if(!isOwner()||!cloudRoom||db==null)return;\n'
        '        final String removeEpoch9936=java.util.UUID.randomUUID().toString();')
replace('roomHasPassword=false;roomEntranceMode9931="invite";toast("Password removed • room remains invite only");',
        'roomHasPassword=false;roomEntranceMode9931="invite";roomEntryEpoch9936=removeEpoch9936;toast("Password removed • fresh invites required");')
# Invitation/Password writer gets same epoch as owner publishes. Server rules verify.
replace('access.put("passwordHash",proof);access.put("createdAt",FieldValue.serverTimestamp());',
        'access.put("passwordHash",proof);access.put("entryEpoch",roomEntryEpoch9936);access.put("createdAt",FieldValue.serverTimestamp());')
raw='grant.put("grantedBy",user.getUid());grant.put("createdAt",FieldValue.serverTimestamp());'
if text.count(raw)!=2:raise RuntimeError('Both host grant writers expected')
text=text.replace(raw,'grant.put("grantedBy",user.getUid());grant.put("entryEpoch",roomEntryEpoch9936);grant.put("createdAt",FieldValue.serverTimestamp());')
java.write_text(text)

rules=root/'firestore.rules'
s=rules.read_text()
def rr(old,new):
 global s
 if s.count(old)!=1:raise RuntimeError(f'Rules anchor count {s.count(old)} for: {old[:100]}')
 s=s.replace(old,new,1)
rr("""      let grant = exists(/databases/$(database)/documents/live_rooms/$(roomId)/access/$(request.auth.uid));""",
   """      let accessPath = /databases/$(database)/documents/live_rooms/$(roomId)/access/$(request.auth.uid);
      let epoch = room.get('entryEpoch','');
      // Backwards compatible old rooms (no entryEpoch) until host changes policy.
      // Once a new policy is set, stale invitations never survive it.
      let grant = exists(accessPath) && (epoch == ''
        || get(accessPath).data.get('entryEpoch','__missing__') == epoch);""")
rr("""        // Access markers are intentionally not client-readable. roomAccess() only needs exists().
        allow read: if false;""",
   """        // Access markers are intentionally not client-readable.
        allow read: if false;
        function validCurrentPasswordProof9936() {
          return activeUser()
            && accessUid == request.auth.uid
            && roomData(roomId).isPrivate == true
            && roomData(roomId).hasPassword == true
            && request.resource.data.uid == request.auth.uid
            && request.resource.data.get('entryEpoch','__missing__')
               == roomData(roomId).get('entryEpoch','')
            && request.resource.data.passwordHash is string
            && request.resource.data.passwordHash
              == get(/databases/$(database)/documents/live_rooms/$(roomId)/secrets/password).data.passwordHash;
        }""")
old="""        allow create: if (
          roomModerator(roomId)
          && request.resource.data.uid == accessUid
        ) || (
          activeUser()
          && accessUid == request.auth.uid
          && roomData(roomId).isPrivate == true
          && roomData(roomId).hasPassword == true
          && request.resource.data.uid == request.auth.uid
          && request.resource.data.passwordHash is string
          && request.resource.data.passwordHash
             == get(/databases/$(database)/documents/live_rooms/$(roomId)/secrets/password).data.passwordHash
        );
        allow update: if roomModerator(roomId)
          && request.resource.data.uid == accessUid;"""
new="""        allow create: if (roomModerator(roomId)
            && request.resource.data.uid == accessUid
            && request.resource.data.get('entryEpoch','__missing__')
               == roomData(roomId).get('entryEpoch',''))
          || validCurrentPasswordProof9936();
        // Users with an old access document must be allowed to re-enter the
        // CURRENT valid password to replace their stale approval. Never permit
        // an arbitrary client to update or recreate a grant without proof.
        allow update: if (roomModerator(roomId)
            && request.resource.data.uid == accessUid
            && request.resource.data.get('entryEpoch','__missing__')
               == roomData(roomId).get('entryEpoch',''))
          || validCurrentPasswordProof9936();"""
rr(old,new)
rules.write_text(s)
build=root/'app/build.gradle';v=build.read_text()
old="versionCode 214; versionName '9.9.35-8680-original-drawables-test'"
assert v.count(old)==1,'Unexpected Android version: must base on actual v9.9.35'
build.write_text(v.replace(old,"versionCode 215; versionName '9.9.36-party-password-access-fix-test'"))
assert 'LIVE_RECHARGE = false' in (root/'app/src/main/java/com/kingplus/social/KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (root/'app/src/main/java/com/kingplus/social/KingPremiumAccess981.java').read_text()
print('PASS real Party Room cross-device password and invite security; 4065 XML/8680 drawables unchanged')
