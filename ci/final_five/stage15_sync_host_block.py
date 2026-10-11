#!/usr/bin/env python3
"""Stage 15: Party 'Block host' writes to same Firebase blocks collection and
user_ownerUid composite document ID as Public Profile. Local-only behavior
previously silently appeared to block while it never synced to any other phone.

Do NOT claim this block automatically bans the host from existing rooms/chats.
Client UI filtering/enforcement across all surfaces remains a separate release gate.
"""
from pathlib import Path
import sys
src=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social'
party=src/'PartyActivity.java';s=party.read_text()
old='''    private void blockHost(){Set<String>b=new HashSet<>(prefs.getStringSet("blocked",new HashSet<>()));b.add(ownerUid==null?ownerName:ownerUid);prefs.edit().putStringSet("blocked",b).apply();toast("Host blocked locally");}'''
new='''    /**
     * Cross-device Block Host uses the SAME document ID/schema as
     * KingPublicProfileActivity.blockId(). Requires Firebase auth and server.
     * A successful write syncs the block list; it does NOT by itself enforce
     * blocking in every chat/room screen. Never report offline fake success.
     */
    private void blockHost(){
        if(!cloudRoom||db==null||user==null||ownerUid==null||ownerUid.trim().isEmpty()){
            toast("Sign in to a real Party Room to block its host");
            return;
        }
        final String hostUid=ownerUid.trim();
        if(hostUid.equals(user.getUid())){toast("You cannot block yourself");return;}
        new AlertDialog.Builder(this)
            .setTitle("🚫 Block this host?")
            .setMessage("Save this host to your KING Plus account block list? "+
                "This will sync with Profile Block/Unblock. "+
                "Existing Party voice/chat filtering is not automatic.")
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Block",(dialog,which)->{
                final String accountUid=user.getUid();
                final DocumentReference blockedRef=db.collection("blocks")
                    .document(accountUid+"_"+hostUid);
                blockedRef.get(com.google.firebase.firestore.Source.SERVER)
                    .addOnSuccessListener(snapshot->{
                        if(isFinishing()||isDestroyed())return;
                        if(snapshot.exists()){
                            toast("Already on your account's blocked list");
                            return;
                        }
                        Map<String,Object> values=new HashMap<>();
                        values.put("ownerUid",accountUid);
                        values.put("targetUid",hostUid);
                        values.put("createdAt",FieldValue.serverTimestamp());
                        blockedRef.set(values).addOnSuccessListener(ignored->{
                            if(isFinishing()||isDestroyed())return;
                            Set<String> local=new HashSet<>(
                                prefs.getStringSet("blocked",new HashSet<>()));
                            local.add(hostUid);
                            prefs.edit().putStringSet("blocked",local).apply();
                            toast("Host added to account Block List");
                        }).addOnFailureListener(error->{
                            if(!isFinishing()&&!isDestroyed())
                                toast("Could not save host block. Check connection and retry.");
                        });
                    }).addOnFailureListener(error->{
                        if(!isFinishing()&&!isDestroyed())
                            toast("Could not verify block list from server. Retry.");
                    });
            }).show();
    }'''
assert s.count(old)==1,f'Expected single local-only Party blockHost, got {s.count(old)}'
s=s.replace(old,new,1)
assert 'private void reportHost(){' in s
assert 'private void handleRoomMenuItem810(String x){' in s
assert 'else if(x.contains("Block"))blockHost();' in s
assert 'roomMenuAll()' in s and 'KingNative3DRoom.open(' in s
party.write_text(s)
profile=(src/'KingPublicProfileActivity.java').read_text()
assert 'me.getUid()+"_"+uid' in profile
assert 'm.put("ownerUid",me.getUid())' in profile
assert 'm.put("targetUid",uid)' in profile
print('PASS: Party host blocks now use the Profile Firebase block schema, no offline false success')
print('PASS: Confirmation & signed-in identity required; cloud room/voice untouched')
print('WARNING: Blocking an account globally across every chat and room screen still needs integration/2-device test')
