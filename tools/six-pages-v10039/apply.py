from pathlib import Path
r=Path('app/src/main/java/com/kingplus/social')
def change(file, old, new, count=1):
    p=r/file;s=p.read_text(encoding='utf8');found=s.count(old)
    if found!=count:raise SystemExit(f'v10.0.39 expected {count} matches, got {found}: {file}: {old[:80]!r}')
    p.write_text(s.replace(old,new),encoding='utf8')
# Me: do not publish the last signed-in user's device-local personal fields under a new account.
change('MainActivity.java','String name = displayName;',
    'boolean ownLocal10039=me.getUid().equals(getPreferences(0).getString("firebase_uid",""));\n'
    '        String name = ownLocal10039?displayName:me.getDisplayName();')
for field in ['bio','tags']:
    prefix=f'profile.put("{field}",'
    p=r/'MainActivity.java';s=p.read_text();old=next(line for line in s.splitlines() if line.strip().startswith(prefix))
    change('MainActivity.java',old,old.replace('profile.put(', 'if(ownLocal10039)profile.put('))
for field in ['hometown','birthday','gender']:
    p=r/'MainActivity.java';s=p.read_text();old=next(line for line in s.splitlines() if line.strip().startswith(f'profile.put("{field}",'))
    change('MainActivity.java',old,old.replace('profile.put(', 'if(ownLocal10039)profile.put('))
change('MainActivity.java','String cloudPhoto868=getPreferences(0).getString("profile_photo_cloud","");',
    'String cloudPhoto868=ownLocal10039?getPreferences(0).getString("profile_photo_cloud",""):"";')
# Home/Family: discard stale chat callbacks when the signed-in account changes.
change('CommunityHubActivity.java',
    'if(body!=familyBody956||familyStopped956||isFinishing()||isDestroyed()||!"Family".equals(selected))return;\n                chat.removeAllViews();',
    'FirebaseUser real10039=FirebaseAuth.getInstance().getCurrentUser();\n'
    '                if(body!=familyBody956||familyStopped956||isFinishing()||isDestroyed()||!"Family".equals(selected)\n'
    '                   ||me==null||real10039==null||!real10039.getUid().equals(me.getUid()))return;\n'
    '                chat.removeAllViews();')
# Party: host reconnect must verify room has not been closed or deleted.
change('PartyActivity.java','if(isOwner()){join.run();return;}\n        // Fetch room state and member capacity together',
    'if(isOwner()){verifyExistingRoomOpen10037(expectedRoom966,expectedId966,expectedUid966,token966,join);return;}\n'
    '        // Fetch room state and member capacity together')
# Game: a separate REAL Bingo 5x5 already exists; do not advertise legacy Pick 1-9 as Bingo.
change('RoomGameActivity.java','"🎱 Bingo","🦁 Crazy Zoo"','"🎯 Pick 1–9","🦁 Crazy Zoo"')
change('RoomGameActivity.java','"🎯 Number Pick 1–9","🎰 Slot Clash"','"🎯 Pick 1–9","🎰 Slot Clash"')
# Discover: use server publicId instead of a locally guessed hash and canonical document UID.
change('DiscoverActivity.java','String uid=safe(p.getString("uid"),p.getId())','String uid=p.getId()',2)
change('DiscoverActivity.java','"KING ID "+publicId(uid)','"KING ID "+issued10039(p)',2)
change('DiscoverActivity.java','private String publicId(String uid){if(uid==null||uid.isEmpty())return "000000";long v=Integer.toUnsignedLong(uid.hashCode());return String.valueOf((v%900000L)+100000L);}',
    'private String publicId(String uid){return "Not issued";}\n'
    '    private String issued10039(DocumentSnapshot d){String x=KingPublicId9996.verified(d.getString("publicId"));return x==null?"Not issued":x;}')
change('DiscoverActivity.java',
    'private void shareUser(String uid,String name){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,"Find "+name+" on KING Plus • KING ID: "+publicId(uid));startActivity(Intent.createChooser(i,"Share KING ID"));}',
    '''private void shareUser(String uid,String name){
        if(me==null||db==null||!KingDocumentId10036.valid(uid)){toast("Sign in first");return;}
        final String account10039=me.getUid();
        db.collection("public_profiles").document(uid).get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(p->{
            if(!followAccountCurrent10028(account10039))return;
            String id=p.exists()?KingPublicId9996.verified(p.getString("publicId")):null;
            if(id==null){toast("This KING ID is not issued yet");return;}
            Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT,"Find "+safe(p.getString("displayName"),name)+" on KING Plus • KING ID: "+id);
            startActivity(Intent.createChooser(i,"Share verified KING ID"));
          }).addOnFailureListener(e->{if(followAccountCurrent10028(account10039))toast("KING ID unavailable • retry online");});
    }''')
# Messages: start a true conversation from the six-digit KING ID seen in Discover.
change('InboxActivity.java','uid.setHint("Friend Firebase UID for live messages");',
    'uid.setHint("6-digit KING ID or Firebase UID");')
needle='db.collection("public_profiles").document(requestedUid10025)'
assert needle in (r/'InboxActivity.java').read_text()
insert='''if(KingIdentity964.sixDigits(requestedUid10025)){
                db.collection("public_profiles").whereEqualTo("publicId",requestedUid10025)
                  .limit(2).get(com.google.firebase.firestore.Source.SERVER)
                  .addOnSuccessListener(found->{
                    if(!inboxCurrent10023(requester10025))return;
                    if(found.size()!=1){Toast.makeText(this,"KING ID not found or duplicated",Toast.LENGTH_LONG).show();return;}
                    DocumentSnapshot peer=found.getDocuments().get(0);
                    if(!requestedUid10025.equals(KingPublicId9996.verified(peer.getString("publicId")))
                      ||!KingDocumentId10036.valid(peer.getId()))return;
                    String verified=peer.getString("displayName");
                    openChat(verified==null||verified.trim().isEmpty()?requestedName10025:verified,peer.getId());
                  }).addOnFailureListener(e->{if(inboxCurrent10023(requester10025))
                    Toast.makeText(this,"KING ID lookup unavailable • retry online",Toast.LENGTH_LONG).show();});
                return;
            }
            '''
change('InboxActivity.java',needle,insert+needle)
change('InboxActivity.java',
    'if(!KingDocumentId10036.valid(id10022)){Toast.makeText(this,"Choose a real KING friend in Discover to start live chat",Toast.LENGTH_LONG).show();return;}',
    'if(!KingIdentity964.sixDigits(id10022)&&!KingIdentity964.firebaseUid(id10022)){Toast.makeText(this,"Enter a six-digit KING ID or real Firebase UID",Toast.LENGTH_LONG).show();return;}')
p=Path('app/build.gradle');s=p.read_text();assert 'versionCode 317' in s;s=s.replace('versionCode 317','versionCode 318',1)
import re
s=re.sub(r"versionName '[^']+'","versionName '10.0.39'",s,count=1)
p.write_text(s)
print('Six-page v10.0.39 patch applied')
