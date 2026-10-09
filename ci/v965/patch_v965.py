#!/usr/bin/env python3
"""KING Plus v9.6.5 - one canonical KING ID and Follow across phones/pages.

Based on successful v9.6.4 artifact. This patch:
- centralizes display ID + directed Follow Firestore document identifiers;
- migrates old public-profile single-underscore follows safely after verifying ownership;
- updates Followers, Following and Friends counters in real time on both profile screens;
- stops all snapshot listeners when screens pause/stop to avoid memory leaks;
- makes Social Discover/Followers/Following refresh after changing a follow;
- warns when two phones share exactly the same authenticated Firebase UID.
"""
from pathlib import Path
import shutil,sys,re

root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
shutil.copy2(Path(__file__).with_name('KingSocialIdentity965.java'),pkg/'KingSocialIdentity965.java')

def once(name,old,new,label):
 p=pkg/name
 t=p.read_text()
 count=t.count(old)
 if count!=1:
  raise SystemExit(f'{label}: marker expected once, found {count}: {old[:130]!r}')
 p.write_text(t.replace(old,new,1))
 print('PASS',label)

def replace_simple_id(name):
 p=pkg/name
 s=p.read_text()
 pat=r'(?m)^    private String publicId\(String (?:firebaseUid|uid|u)\)\s*\{[^{}]*\}'
 hits=list(re.finditer(pat,s))
 if len(hits)!=1:
  raise SystemExit(f'{name}: exactly one publicId helper expected, got {len(hits)}')
 s=s[:hits[0].start()]+'    private String publicId(String uid){return KingSocialIdentity965.publicId(uid);}'+s[hits[0].end():]
 p.write_text(s)
 print('PASS',name,'canonical public KING ID from Firebase UID')

for name in ['MainActivity.java','SocialActivity.java','KingPublicProfileActivity.java']:
 replace_simple_id(name)

once('SocialActivity.java',
     'private String followId(String a,String b){return a+"__"+b;}',
     'private String followId(String a,String b){return KingSocialIdentity965.followId(a,b);}',
     'Social Follow uses shared canonical id')

once('KingPublicProfileActivity.java',
     'private String followId(){return me==null?"":me.getUid()+"_"+uid;}',
     'private String followId(){return me==null?"":KingSocialIdentity965.followId(me.getUid(),uid);}',
     'Public Profile Follow writes SAME doc as Party and Social')

# MainActivity: do not count follows just once on Profile open.
main=pkg/'MainActivity.java'
t=main.read_text()
marker='    private TextView profileFollowersNumber, profileFollowingNumber, profileFriendsNumber, profileTopBalance, profileWalletCoins, profileVisitorsChip940;'
fields='''    // Live social counters use two subscriptions only while Profile is visible.
    private com.google.firebase.firestore.ListenerRegistration followingListener965,followersListener965;
    private final java.util.Set<String> liveFollowing965=new java.util.HashSet<>();
    private final java.util.Set<String> liveFollowers965=new java.util.HashSet<>();
    private boolean followingLoaded965,followersLoaded965;
'''
if t.count(marker)!=1:raise SystemExit('Main social fields baseline mismatch')
t=t.replace(marker,marker+'\n'+fields,1)

begin='        firestore.collection("follows").whereEqualTo("followerUid",uid).get().addOnSuccessListener(out->{'
end='        loadProfileVisitors940(uid);'
a=t.find(begin);b=t.find(end,a)
if a<0 or b<0 or t.count(begin)!=1:raise SystemExit('Main one-time social count body not found')
t=t[:a]+'        startProfileFollowUpdates965(uid);\n'+t[b:]

anchor='    private void loadProfileVisitors940(String uid){'
helpers=r'''    private void stopProfileFollowUpdates965(){
        if(followingListener965!=null){followingListener965.remove();followingListener965=null;}
        if(followersListener965!=null){followersListener965.remove();followersListener965=null;}
        liveFollowing965.clear();liveFollowers965.clear();
        followingLoaded965=false;followersLoaded965=false;
    }
    private void renderProfileFollowUpdates965(){
        if(!"profile".equals(screen)||isFinishing()||isDestroyed())return;
        if(profileFollowingNumber!=null&&followingLoaded965)
            profileFollowingNumber.setText(String.valueOf(liveFollowing965.size()));
        if(profileFollowersNumber!=null&&followersLoaded965)
            profileFollowersNumber.setText(String.valueOf(liveFollowers965.size()));
        if(profileFriendsNumber!=null&&followingLoaded965&&followersLoaded965){
            int mutual965=0;
            for(String follower965:liveFollowers965)if(liveFollowing965.contains(follower965))mutual965++;
            profileFriendsNumber.setText(String.valueOf(mutual965));
        }
    }
    private void startProfileFollowUpdates965(String uid){
        stopProfileFollowUpdates965();
        if(firestore==null||uid==null||uid.isEmpty()||!"profile".equals(screen))return;
        followingListener965=firestore.collection("follows")
            .whereEqualTo("followerUid",uid).addSnapshotListener((snapshot,error)->{
                if(isFinishing()||isDestroyed()||!"profile".equals(screen))return;
                if(firebaseAuth==null||firebaseAuth.getCurrentUser()==null||
                   !uid.equals(firebaseAuth.getCurrentUser().getUid()))return;
                if(error!=null||snapshot==null){
                    if(profileFollowingNumber!=null)profileFollowingNumber.setText("—");
                    if(profileFriendsNumber!=null)profileFriendsNumber.setText("—");
                    return;
                }
                liveFollowing965.clear();
                for(DocumentSnapshot doc:snapshot.getDocuments()){
                    String target=doc.getString("targetUid");
                    if(target!=null&&!target.isEmpty())liveFollowing965.add(target);
                }
                followingLoaded965=true;
                renderProfileFollowUpdates965();
            });
        followersListener965=firestore.collection("follows")
            .whereEqualTo("targetUid",uid).addSnapshotListener((snapshot,error)->{
                if(isFinishing()||isDestroyed()||!"profile".equals(screen))return;
                if(firebaseAuth==null||firebaseAuth.getCurrentUser()==null||
                   !uid.equals(firebaseAuth.getCurrentUser().getUid()))return;
                if(error!=null||snapshot==null){
                    if(profileFollowersNumber!=null)profileFollowersNumber.setText("—");
                    if(profileFriendsNumber!=null)profileFriendsNumber.setText("—");
                    return;
                }
                liveFollowers965.clear();
                for(DocumentSnapshot doc:snapshot.getDocuments()){
                    String source=doc.getString("followerUid");
                    if(source!=null&&!source.isEmpty())liveFollowers965.add(source);
                }
                followersLoaded965=true;
                renderProfileFollowUpdates965();
            });
    }

'''
if t.count(anchor)!=1:raise SystemExit('Main profile helper anchor missing')
t=t.replace(anchor,helpers+anchor,1)
old='    @Override protected void onPause() { super.onPause(); stopMic(); }'
new='''    @Override protected void onResume(){
        super.onResume();
        if("profile".equals(screen) && followingListener965==null && followersListener965==null
                && firestore!=null && firebaseAuth!=null && firebaseAuth.getCurrentUser()!=null)
            startProfileFollowUpdates965(firebaseAuth.getCurrentUser().getUid());
    }
    @Override protected void onPause() { stopProfileFollowUpdates965(); super.onPause(); stopMic(); }'''
if t.count(old)!=1:raise SystemExit('Main onPause lifecycle anchor missing')
t=t.replace(old,new,1)
old='    @Override protected void onDestroy() { stopMic(); super.onDestroy(); }'
if t.count(old)!=1:raise SystemExit('Main onDestroy lifecycle anchor missing')
t=t.replace(old,'    @Override protected void onDestroy() { stopProfileFollowUpdates965(); stopMic(); super.onDestroy(); }',1)
main.write_text(t)
print('PASS Main Profile two realtime Firestore streams and safe unsubscribe')

# Public Profile: same realtime count logic for another person's profile.
profile=pkg/'KingPublicProfileActivity.java'
s=profile.read_text()
anchor='    private int dp(int n)'
if s.count(anchor)!=1:raise SystemExit('Public Profile field anchor missing')
fields=r'''    private com.google.firebase.firestore.ListenerRegistration incoming965,outgoing965,followDoc965;
    private final java.util.Set<String> publicFollowerSet965=new java.util.HashSet<>();
    private final java.util.Set<String> publicFollowingSet965=new java.util.HashSet<>();
    private boolean publicIncomingLoaded965,publicOutgoingLoaded965;

'''
s=s.replace(anchor,fields+anchor,1)

start='    private void loadPublicCounts940(){'
end='    private TextView action(String s){'
a=s.index(start);b=s.index(end,a)
replacement=r'''    private void removePublicFollowListeners965(){
        if(incoming965!=null){incoming965.remove();incoming965=null;}
        if(outgoing965!=null){outgoing965.remove();outgoing965=null;}
        if(followDoc965!=null){followDoc965.remove();followDoc965=null;}
        publicFollowerSet965.clear();publicFollowingSet965.clear();
        publicIncomingLoaded965=false;publicOutgoingLoaded965=false;
    }

    private void drawPublicFollowCounts965(){
        if(isFinishing()||isDestroyed())return;
        if(publicIncomingLoaded965)
            setPublicStat940(publicFollowers940,publicFollowerSet965.size(),"Followers");
        if(publicOutgoingLoaded965)
            setPublicStat940(publicFollowing940,publicFollowingSet965.size(),"Following");
        if(publicIncomingLoaded965&&publicOutgoingLoaded965){
            int mutual965=0;
            for(String u:publicFollowerSet965)if(publicFollowingSet965.contains(u))mutual965++;
            setPublicStat940(publicFriends940,mutual965,"Friends");
        }
    }

    private void loadPublicCounts940(){
        if(db==null||uid==null||uid.isEmpty()){failPublicCounts940();return;}
        if(incoming965!=null){incoming965.remove();incoming965=null;}
        if(outgoing965!=null){outgoing965.remove();outgoing965=null;}
        publicIncomingLoaded965=false;publicOutgoingLoaded965=false;
        outgoing965=db.collection("follows").whereEqualTo("followerUid",uid)
            .addSnapshotListener((snap,e)->{
                if(isFinishing()||isDestroyed())return;
                if(e!=null||snap==null){failPublicCounts940();return;}
                publicFollowingSet965.clear();
                for(DocumentSnapshot d:snap.getDocuments()){
                    String value=d.getString("targetUid");
                    if(value!=null&&!value.isEmpty())publicFollowingSet965.add(value);
                }
                publicOutgoingLoaded965=true;
                drawPublicFollowCounts965();
            });
        incoming965=db.collection("follows").whereEqualTo("targetUid",uid)
            .addSnapshotListener((snap,e)->{
                if(isFinishing()||isDestroyed())return;
                if(e!=null||snap==null){failPublicCounts940();return;}
                publicFollowerSet965.clear();
                for(DocumentSnapshot d:snap.getDocuments()){
                    String value=d.getString("followerUid");
                    if(value!=null&&!value.isEmpty())publicFollowerSet965.add(value);
                }
                publicIncomingLoaded965=true;
                drawPublicFollowCounts965();
            });
    }

'''
s=s[:a]+replacement+s[b:]

old='    private void checkFollow(){if(me==null||db==null||me.getUid().equals(uid)){if(followBtn!=null)followBtn.setText(me!=null&&me.getUid().equals(uid)?"My Profile":"＋ Follow");return;}db.collection("follows").document(followId()).get().addOnSuccessListener(d->{if(followBtn!=null)followBtn.setText(d.exists()?"✓ Following":"＋ Follow");});}'
new=r'''    private void checkFollow(){
        if(followDoc965!=null){followDoc965.remove();followDoc965=null;}
        if(me==null||db==null||me.getUid().equals(uid)){
            if(followBtn!=null)followBtn.setText(me!=null&&me.getUid().equals(uid)?"My Profile":"＋ Follow");
            return;
        }
        // A live listener reflects changes made from other devices immediately.
        followDoc965=db.collection("follows").document(followId()).addSnapshotListener((d,e)->{
            if(!isFinishing()&&!isDestroyed()&&followBtn!=null){
                if(e!=null){followBtn.setText("Follow unavailable");return;}
                followBtn.setText(d!=null&&d.exists()?"✓ Following":"＋ Follow");
            }
        });
        migrateLegacyFollow965();
    }
    private void migrateLegacyFollow965(){
        if(me==null||db==null||uid==null||uid.isEmpty()||uid.equals(me.getUid()))return;
        // v9.6.4 Public Profile used _ while every other Social/Party page used __.
        final com.google.firebase.firestore.DocumentReference oldRef=
            db.collection("follows").document(KingSocialIdentity965.legacyFollowId(me.getUid(),uid));
        oldRef.get().addOnSuccessListener(oldDoc->{
            if(!oldDoc.exists()||!me.getUid().equals(oldDoc.getString("followerUid"))
                ||!uid.equals(oldDoc.getString("targetUid")))return;
            final com.google.firebase.firestore.DocumentReference current=
                db.collection("follows").document(followId());
            current.get().addOnSuccessListener(nowDoc->{
                if(nowDoc.exists()){
                    oldRef.delete().addOnFailureListener(e->{});
                    return;
                }
                java.util.Map<String,Object> migrated=new java.util.HashMap<>();
                migrated.put("followerUid",me.getUid());
                migrated.put("targetUid",uid);
                migrated.put("createdAt",oldDoc.get("createdAt")==null
                    ?com.google.firebase.firestore.FieldValue.serverTimestamp():oldDoc.get("createdAt"));
                current.set(migrated).addOnSuccessListener(v->
                    oldRef.delete().addOnFailureListener(e->{}))
                    .addOnFailureListener(e->toast("Old Follow will sync when network returns"));
            });
        });
    }

'''
if s.count(old)!=1:raise SystemExit('Public Profile old one-time checkFollow anchor missing')
s=s.replace(old,new,1)
# Public main publicId replaced already.
anchor='    private String publicId(String uid){return KingSocialIdentity965.publicId(uid);}'
if s.count(anchor)!=1:raise SystemExit('Public Profile canonical ID anchor missing')
lifecycle=r'''    @Override protected void onStart(){
        super.onStart();
        if(publicFollowers940!=null && incoming965==null && outgoing965==null)loadPublicCounts940();
        if(followBtn!=null && followDoc965==null)checkFollow();
    }
    @Override protected void onStop(){
        removePublicFollowListeners965();
        super.onStop();
    }
    @Override protected void onDestroy(){
        removePublicFollowListeners965();
        super.onDestroy();
    }
'''
s=s.replace(anchor,anchor+'\n'+lifecycle,1)
profile.write_text(s)
print('PASS Public Profile realtime follows + legacy follow migration + lifecycle cleanup')

# Social cards should refresh after a follow or unfollow and upon returning from another profile.
social=pkg/'SocialActivity.java'
s=social.read_text()
marker='    private TextView status;'
if s.count(marker)!=1:raise SystemExit('Social status field marker missing')
s=s.replace(marker,marker+'\n    private String activeSocialScreen965="discover";\n    private boolean socialHasResumed965;\n',1)
for method,key in [('runSearch','search'),('loadDiscover','discover'),('loadFollowing','following'),('loadFollowers','followers'),('loadFriends','friends')]:
 anchor='    private void '+method+'() {'
 if s.count(anchor)!=1:raise SystemExit('Social method marker missing '+method)
 s=s.replace(anchor,anchor+'\n        activeSocialScreen965="'+key+'";',1)

old='                .addOnSuccessListener(v->toast("Disconnected from "+name))'
new='                .addOnSuccessListener(v->{toast("Disconnected from "+name);refreshSocialView965();})'
if s.count(old)!=1:raise SystemExit('Social unfollow success marker missing')
s=s.replace(old,new,1)
old='''        db.collection("follows").document(id).set(data).addOnSuccessListener(v->{
            String myName=me.getDisplayName();'''
new='''        db.collection("follows").document(id).set(data).addOnSuccessListener(v->{
            refreshSocialView965();
            String myName=me.getDisplayName();'''
if s.count(old)!=1:raise SystemExit('Social follow success marker missing')
s=s.replace(old,new,1)
anchor='    private void showLocalDemo() {'
add=r'''    private void refreshSocialView965(){
        if(isFinishing()||isDestroyed()||!cloudReady())return;
        switch(activeSocialScreen965){
            case "following":loadFollowing();break;
            case "followers":loadFollowers();break;
            case "friends":loadFriends();break;
            case "search":runSearch();break;
            default:loadDiscover();break;
        }
    }
    @Override protected void onResume(){
        super.onResume();
        if(socialHasResumed965)refreshSocialView965();
        socialHasResumed965=true;
    }

'''
if s.count(anchor)!=1:raise SystemExit('Social helper anchor missing')
s=s.replace(anchor,add+anchor,1)
social.write_text(s)
print('PASS Social live screen refresh after follow action and back navigation')

# Party warns when several devices use one Firebase account (one member doc per UID).
once('PartyActivity.java',
     'toast("Same KING account • syncing this phone");',
     'toast("Both phones use the same KING account: one member ID. Use separate Google accounts to appear as different people.");',
     'explicit duplicate Firebase account guidance in Party')

gradle=root/'app/build.gradle'
g=gradle.read_text()
old="versionCode 155; versionName '9.6.4-party-memory-social-ids'"
if g.count(old)!=1:raise SystemExit('Patch expects successful v9.6.4 source')
gradle.write_text(g.replace(old,"versionCode 156; versionName '9.6.5-realtime-social-identity'",1))
print('PASS: version 9.6.5; Firebase UID and Ludo/voice/Emoji unchanged')
