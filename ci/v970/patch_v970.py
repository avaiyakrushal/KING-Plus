#!/usr/bin/env python3
"""KING Plus v9.7.0 – cross-phone social, Follow and ID reliability.

Based on successfully built v9.6.9 source. Protect against three source-confirmed
bugs: (1) followers/following/friends had only one-time reads, (2) delayed search
and profile network responses replaced the currently selected screen, (3)
opening Social republished stale phone-local profile/level details over newer
cloud values. Additionally bound the Social thumbnail decoder, make follow
toggle an atomic Firestore transaction, and repair legacy rooms missing joinCode.
"""
from pathlib import Path
import shutil,sys

root=Path(sys.argv[1]);pkg=root/'app/src/main/java/com/kingplus/social'
sp=pkg/'SocialActivity.java'
s=sp.read_text()
shutil.copy2(Path(__file__).with_name('KingSocialRealtime970.java'),
             pkg/'KingSocialRealtime970.java')

def replace_once(old,new,label):
 global s
 n=s.count(old)
 if n!=1:
  raise SystemExit(f'{label}: expected 1 marker, found {n}: {old[:135]!r}')
 s=s.replace(old,new,1)
 print('PASS',label)

replace_once('    private boolean socialHasResumed965;',
r'''    private boolean socialHasResumed965;
    private com.google.firebase.firestore.ListenerRegistration socialOut970,socialIn970;
    private final java.util.Set<String> following970=new java.util.HashSet<>();
    private final java.util.Set<String> followers970=new java.util.HashSet<>();
    private final java.util.Set<String> pendingFollows970=new java.util.HashSet<>();
    private boolean followingLoaded970,followersLoaded970;
    private int socialViewGeneration970;
    // Bound worker threads and decoded pixels so large profile photos cannot
    // accumulate unbounded bitmap allocations while discovering many people.
    private final java.util.concurrent.ExecutorService socialPhotoWorker970=
        java.util.concurrent.Executors.newFixedThreadPool(2);
    private final android.util.LruCache<String,android.graphics.Bitmap> socialPhotoCache970=
        new android.util.LruCache<String,android.graphics.Bitmap>(4*1024*1024){
            @Override protected int sizeOf(String key,android.graphics.Bitmap bitmap){
                return bitmap==null?0:bitmap.getByteCount();
            }
        };''','add realtime follower subscriptions, bounded profile thumbnail cache and stale-view guards')

# Never let a second phone overwrite a newer profile using stale local prefs
# simply because its user opened the Social directory.
start='    private void publishOwnProfile() {'
end='    private void runSearch() {'
a=s.index(start);b=s.index(end,a)
s=s[:a]+r'''    private boolean currentAccount970(){
        if(!cloudReady()||isFinishing()||isDestroyed())return false;
        try{
            FirebaseUser account970=FirebaseAuth.getInstance().getCurrentUser();
            return KingSocialRealtime970.activeAccount(me.getUid(),
                account970==null?null:account970.getUid(),true);
        }catch(Exception error){return false;}
    }
    private boolean currentSearch970(int gen970,String query970){
        return currentAccount970()
            && KingSocialRealtime970.isCurrentView("search",activeSocialScreen965,
                gen970,socialViewGeneration970,true)
            && search!=null
            && KingSocialRealtime970.sameQuery(query970,search.getText().toString());
    }

    private void publishOwnProfile() {
        if(!currentAccount970())return;
        final String ownUid970=me.getUid();
        final SharedPreferences prefs970=getSharedPreferences("MainActivity",MODE_PRIVATE);
        final com.google.firebase.firestore.DocumentReference profile970=
            db.collection("public_profiles").document(ownUid970);
        profile970.get().addOnSuccessListener(existing970->{
            if(!currentAccount970())return;
            Map<String,Object> patch970=new HashMap<>();
            patch970.put("uid",ownUid970);
            patch970.put("publicId",publicId(ownUid970));
            String existingName970=existing970.exists()?existing970.getString("displayName"):null;
            String existingSearch970=existing970.exists()?existing970.getString("searchName"):null;
            if(existingName970==null||existingName970.trim().isEmpty()){
                String name970=prefs970.getString("name","");
                if(name970==null||name970.trim().isEmpty())name970=me.getDisplayName();
                if(name970==null||name970.trim().isEmpty())name970="KING "+shortUid(ownUid970);
                existingName970=clean(name970);
                patch970.put("displayName",existingName970);
            }
            if(existingSearch970==null||existingSearch970.trim().isEmpty())
                patch970.put("searchName",existingName970.toLowerCase(Locale.US));
            if(!existing970.exists()||existing970.getString("bio")==null)
                patch970.put("bio",clean(prefs970.getString("bio","Love music, games and new friends ✨")));
            if(!existing970.exists()||existing970.getString("tags")==null)
                patch970.put("tags",clean(prefs970.getString("tags","Music, Games")));
            if(!existing970.exists()){
                LevelSystem.Snapshot levels970=LevelSystem.read(this);
                patch970.put("level",levels970.level);
                patch970.put("xp",levels970.xp);
                patch970.put("vipLevel",levels970.vipLevel);
                patch970.put("vipPoints",levels970.vipPoints);
                patch970.put("levelTier",LevelSystem.levelTier(levels970.level));
            }
            // Merge ONLY missing/default identity fields, preserving edits saved
            // by another device (name/photo/bio/level) and the immutable Firebase UID.
            profile970.set(patch970,com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(v->{
                    if(currentAccount970()&&status!=null)
                        status.setText("Connected • KING ID "+publicId(ownUid970));
                }).addOnFailureListener(e->{
                    if(currentAccount970()&&status!=null)
                        status.setText("Profile directory unavailable");
                });
        }).addOnFailureListener(e->{
            if(currentAccount970()&&status!=null)
                status.setText("Profile sync temporarily unavailable");
        });
    }

    private void startLiveRelations970(){
        if(!currentAccount970())return;
        final String owner970=me.getUid();
        if(socialOut970==null){
            socialOut970=db.collection("follows").whereEqualTo("followerUid",owner970)
                .addSnapshotListener((snap,error)->{
                    if(!currentAccount970()||!owner970.equals(me.getUid()))return;
                    if(error!=null||snap==null){
                        followingLoaded970=false;
                        if("following".equals(activeSocialScreen965)||"friends".equals(activeSocialScreen965))
                            setLoading("Following synchronization unavailable");
                        return;
                    }
                    following970.clear();
                    for(DocumentSnapshot edge:snap.getDocuments()){
                        String peer=edge.getString("targetUid");
                        if(KingSocialRealtime970.showUid(peer,owner970))
                            following970.add(peer);
                    }
                    followingLoaded970=true;
                    renderLiveRelations970();
                });
        }
        if(socialIn970==null){
            socialIn970=db.collection("follows").whereEqualTo("targetUid",owner970)
                .addSnapshotListener((snap,error)->{
                    if(!currentAccount970()||!owner970.equals(me.getUid()))return;
                    if(error!=null||snap==null){
                        followersLoaded970=false;
                        if("followers".equals(activeSocialScreen965)||"friends".equals(activeSocialScreen965))
                            setLoading("Follower synchronization unavailable");
                        return;
                    }
                    followers970.clear();
                    for(DocumentSnapshot edge:snap.getDocuments()){
                        String peer=edge.getString("followerUid");
                        if(KingSocialRealtime970.showUid(peer,owner970))
                            followers970.add(peer);
                    }
                    followersLoaded970=true;
                    renderLiveRelations970();
                });
        }
    }

    private void stopLiveRelations970(){
        socialViewGeneration970++;
        if(socialOut970!=null){socialOut970.remove();socialOut970=null;}
        if(socialIn970!=null){socialIn970.remove();socialIn970=null;}
        following970.clear();followers970.clear();
        followingLoaded970=false;followersLoaded970=false;
    }

    private void renderLiveRelations970(){
        if(!currentAccount970())return;
        String tab970=activeSocialScreen965;
        if(!"following".equals(tab970)&&!"followers".equals(tab970)&&!"friends".equals(tab970))return;
        if(("following".equals(tab970)&&!followingLoaded970)
            ||("followers".equals(tab970)&&!followersLoaded970)
            ||("friends".equals(tab970)&&(!followingLoaded970||!followersLoaded970))){
            setLoading("Syncing real KING Plus members…");
            return;
        }
        java.util.Set<String> people970;
        String empty970;
        if("following".equals(tab970)){
            people970=new java.util.HashSet<>(following970);
            empty970="You are not following anyone yet";
        }else if("followers".equals(tab970)){
            people970=new java.util.HashSet<>(followers970);
            empty970="No followers yet";
        }else{
            people970=KingSocialRealtime970.friends(following970,followers970);
            empty970="Friends appear when you follow each other";
        }
        final int generation970=++socialViewGeneration970;
        list.removeAllViews();
        if(people970.isEmpty()){setLoading(empty970);return;}
        int shown970=0;
        for(String person970:new java.util.TreeSet<>(people970)){
            if(shown970++>=60)break; // prevent decoding/rendering hundreds of images at once.
            loadProfileCard970(person970,generation970,tab970);
        }
    }

''' + s[b:]
print('PASS', 'Social profile cloud-preserving merge, authenticated realtime followers, bounded relationship lists')

# Stale search results must not replace a more recently selected query or tab.
replace_once(
 '        String raw = search.getText().toString().trim();\n        String q = raw.toLowerCase(Locale.US);',
 '        String raw = search.getText().toString().trim();\n        final int generation970=++socialViewGeneration970;\n        String q = raw.toLowerCase(Locale.US);',
 'version each Search request to reject old asynchronous results')
replace_once(
 '                .addOnSuccessListener(result->{\n                    List<DocumentSnapshot> matches=new ArrayList<>();',
 '                .addOnSuccessListener(result->{\n                    if(!currentSearch970(generation970,raw))return;\n                    List<DocumentSnapshot> matches=new ArrayList<>();',
 'guard numeric KING ID search callback')
replace_once(
 '                .addOnFailureListener(e->{setLoading("ID lookup unavailable");toast(safe(e.getLocalizedMessage()));});',
 '                .addOnFailureListener(e->{if(!currentSearch970(generation970,raw))return;setLoading("ID lookup unavailable");toast(safe(e.getLocalizedMessage()));});',
 'guard failed numeric search from outdated screen')
replace_once(
 '''                .addOnSuccessListener(d->renderProfiles(
                    d.exists()?java.util.Collections.singletonList(d):java.util.Collections.emptyList(),
                    "No user found with that verified UID"))''',
 '''                .addOnSuccessListener(d->{
                    if(!currentSearch970(generation970,raw))return;
                    renderProfiles(d.exists()?java.util.Collections.singletonList(d)
                        :java.util.Collections.emptyList(),"No user found with that verified UID");
                })''',
 'guard exact Firebase UID search callbacks')
replace_once(
 '                .addOnFailureListener(e->{setLoading("UID lookup unavailable");toast(safe(e.getLocalizedMessage()));});',
 '                .addOnFailureListener(e->{if(!currentSearch970(generation970,raw))return;setLoading("UID lookup unavailable");toast(safe(e.getLocalizedMessage()));});',
 'guard exact UID search failures')
replace_once(
 '                .addOnSuccessListener(s -> renderProfiles(s.getDocuments(), "No users found"))',
 '                .addOnSuccessListener(result970->{if(currentSearch970(generation970,raw))renderProfiles(result970.getDocuments(),"No users found");})',
 'guard name search callbacks')
replace_once(
 '                .addOnFailureListener(e -> { setLoading("Search unavailable"); toast(safe(e.getLocalizedMessage())); });',
 '                .addOnFailureListener(e -> {if(!currentSearch970(generation970,raw))return;setLoading("Search unavailable");toast(safe(e.getLocalizedMessage()));});',
 'guard name search failures')

start='    private void loadDiscover() {'
end='    private void renderProfiles('
a0=s.index(start)
s=s[:a0]+r'''    private void loadDiscover() {
        activeSocialScreen965="discover";
        if(!currentAccount970()){showLocalDemo();return;}
        final int generation970=++socialViewGeneration970;
        setLoading("Loading people…");
        db.collection("public_profiles").limit(30).get()
            .addOnSuccessListener(snap->{
                if(currentAccount970() && KingSocialRealtime970.isCurrentView(
                    "discover",activeSocialScreen965,generation970,socialViewGeneration970,true))
                    renderProfiles(snap.getDocuments(),"No public profiles yet");
            })
            .addOnFailureListener(e->{
                if(currentAccount970() && KingSocialRealtime970.isCurrentView(
                    "discover",activeSocialScreen965,generation970,socialViewGeneration970,true)){
                    setLoading("People directory unavailable");toast(safe(e.getLocalizedMessage()));
                }
            });
    }
''' + s[s.index(end,a0):]
print('PASS', 'Discover search does not overwrite new results with out-of-order replies')

# Replace the previous three one-shot list loaders with live, UID-deduplicated
# Firestore subscriptions (not stale get() snapshots).
start='    private void loadFollowing() {'
end='    private void addPerson('
a=s.index(start);b=s.index(end,a)
s=s[:a]+r'''    private void loadFollowing() {
        activeSocialScreen965="following";
        if(!currentAccount970()){showLocalDemo();return;}
        startLiveRelations970();
        renderLiveRelations970();
    }
    private void loadFollowers() {
        activeSocialScreen965="followers";
        if(!currentAccount970()){showLocalDemo();return;}
        startLiveRelations970();
        renderLiveRelations970();
    }
    private void loadFriends() {
        activeSocialScreen965="friends";
        if(!currentAccount970()){showLocalDemo();return;}
        startLiveRelations970();
        renderLiveRelations970();
    }
    private void loadProfileCard970(String uid,int generation970,String expectedScreen970) {
        if(!KingSocialRealtime970.showUid(uid,me==null?null:me.getUid()))return;
        db.collection("public_profiles").document(uid).get()
            .addOnSuccessListener(profile970->{
                if(!currentAccount970()||!KingSocialRealtime970.isCurrentView(
                    expectedScreen970,activeSocialScreen965,generation970,socialViewGeneration970,true))return;
                if(profile970.exists())
                    addPerson(uid,nameOf(profile970),clean(profile970.getString("bio")),
                        clean(profile970.getString("photoUrl")),profile970.getLong("level"),profile970.getLong("vipLevel"));
                else addPerson(uid,"KING "+shortUid(uid),"","",null,null);
            }).addOnFailureListener(error->{
                if(currentAccount970()&&KingSocialRealtime970.isCurrentView(
                    expectedScreen970,activeSocialScreen965,generation970,socialViewGeneration970,true))
                    addPerson(uid,"KING "+shortUid(uid),"","",null,null);
            });
    }

''' + s[b:]
print('PASS', 'Following/Followers/Friends use actual Firebase live subscriptions; stale profile images and results ignored')

# Social's original loadSocialPhoto940 started one unrestricted Thread per person
# and decoded full-resolution bitmaps. Keep small bounded thumbnails instead.
start='    private void loadSocialPhoto940('
end='    private void openProfile('
a=s.index(start);b=s.index(end,a)
s=s[:a]+r'''    private void loadSocialPhoto940(android.widget.ImageView image,TextView fallback,String url){
        if(url==null||url.trim().isEmpty())return;
        final String source=url.trim();
        image.setTag(source);
        android.graphics.Bitmap cached970=socialPhotoCache970.get(source);
        if(cached970!=null&&!cached970.isRecycled()){
            image.setImageBitmap(cached970);
            fallback.setVisibility(View.GONE);
            return;
        }
        try{
            socialPhotoWorker970.execute(()->{
                try{
                    java.net.URL url970=new java.net.URL(source);
                    if(!"https".equalsIgnoreCase(url970.getProtocol()))return;
                    android.graphics.BitmapFactory.Options bounds970=new android.graphics.BitmapFactory.Options();
                    bounds970.inJustDecodeBounds=true;
                    java.net.URLConnection head970=url970.openConnection();
                    head970.setConnectTimeout(6000);head970.setReadTimeout(6000);
                    try(java.io.InputStream in970=head970.getInputStream()){
                        android.graphics.BitmapFactory.decodeStream(in970,null,bounds970);
                    }
                    if(bounds970.outWidth<=0||bounds970.outHeight<=0)return;
                    int sample970=1;
                    while(bounds970.outWidth/sample970>192||bounds970.outHeight/sample970>192)
                        sample970*=2;
                    android.graphics.BitmapFactory.Options size970=new android.graphics.BitmapFactory.Options();
                    size970.inSampleSize=sample970;
                    size970.inPreferredConfig=android.graphics.Bitmap.Config.RGB_565;
                    java.net.URLConnection data970=url970.openConnection();
                    data970.setConnectTimeout(6000);data970.setReadTimeout(6000);
                    android.graphics.Bitmap bitmap970;
                    try(java.io.InputStream in970=data970.getInputStream()){
                        bitmap970=android.graphics.BitmapFactory.decodeStream(in970,null,size970);
                    }
                    if(bitmap970==null||socialPhotoWorker970.isShutdown())return;
                    socialPhotoCache970.put(source,bitmap970);
                    runOnUiThread(()->{
                        if(!isFinishing()&&!isDestroyed()&&source.equals(image.getTag())){
                            image.setImageBitmap(bitmap970);
                            fallback.setVisibility(View.GONE);
                        }
                    });
                }catch(Exception ignored){}
                catch(OutOfMemoryError ignored){}
            });
        }catch(java.util.concurrent.RejectedExecutionException ignored){}
    }

''' + s[b:]
print('PASS', 'bounded two-worker Social profile photo decoding, max 192px and 4 MiB bitmap cache')

# Ensure Follow state cannot be inverted by stale dialog state or double tapping
# while another phone is changing the relationship at the same time.
start='    private void toggleFollow(String uid, String name, boolean following) {'
end='    private void shareKingId('
a=s.index(start);b=s.index(end,a)
s=s[:a]+r'''    private void toggleFollow(String uid, String name, boolean following) {
        if(!currentAccount970()||!KingSocialRealtime970.showUid(uid,me.getUid()))return;
        if(!pendingFollows970.add(uid)){toast("Follow update in progress");return;}
        final String myUid970=me.getUid();
        final com.google.firebase.firestore.DocumentReference edge970=
            db.collection("follows").document(followId(myUid970,uid));
        db.runTransaction(tx970->{
            DocumentSnapshot existing970=tx970.get(edge970);
            if(existing970.exists()){
                if(!myUid970.equals(existing970.getString("followerUid"))||
                   !uid.equals(existing970.getString("targetUid")))
                    throw new IllegalStateException("Follow document belongs to another account");
                tx970.delete(edge970);
                return false;
            }
            Map<String,Object> values970=new HashMap<>();
            values970.put("followerUid",myUid970);
            values970.put("targetUid",uid);
            values970.put("createdAt",FieldValue.serverTimestamp());
            tx970.set(edge970,values970);
            return true;
        }).addOnSuccessListener(isNowFollowing970->{
            pendingFollows970.remove(uid);
            if(!currentAccount970())return;
            refreshSocialView965();
            if(Boolean.TRUE.equals(isNowFollowing970)){
                db.collection("follows").document(followId(uid,myUid970)).get()
                    .addOnSuccessListener(reverse970->{
                        if(currentAccount970())toast(reverse970.exists()
                            ?"💜 You and "+name+" are now friends"
                            :"Connected • following "+name);
                    }).addOnFailureListener(e->{
                        if(currentAccount970())toast("Connected • following "+name);
                    });
            }else toast("Unfollowed "+name);
        }).addOnFailureListener(error970->{
            pendingFollows970.remove(uid);
            if(currentAccount970())toast("Follow sync failed: "+safe(error970.getLocalizedMessage()));
        });
    }

''' + s[b:]
print('PASS', 'follow/unfollow atomic transaction and duplicate-tap guard across phones')

# Subscribe only while visible. Stop subscriptions and queued image work on
# destruction to avoid leaking realtime Firestore/network resources.
anchor='    @Override protected void onResume(){'
addition=r'''    @Override protected void onStart(){
        super.onStart();
        if(currentAccount970())startLiveRelations970();
    }
    @Override protected void onStop(){
        stopLiveRelations970();
        super.onStop();
    }
    @Override protected void onDestroy(){
        stopLiveRelations970();
        socialPhotoWorker970.shutdownNow();
        socialPhotoCache970.evictAll();
        super.onDestroy();
    }
    @Override public void onTrimMemory(int level){
        super.onTrimMemory(level);
        if(level>=10)socialPhotoCache970.evictAll();
    }
'''
replace_once(anchor,addition+anchor,'unsubscribe social Firebase listeners during lifecycle and free bitmap memory')

sp.write_text(s)
print('PASS', 'SocialActivity v9.7.0 completed')

# Room ID search is based on the room's joinCode. Legacy fallback creation
# forgot this field while still showing a numeric Room Code to the host.
party=pkg/'PartyActivity.java';p=party.read_text()
def pc(old,new,label):
 global p
 n=p.count(old)
 if n!=1:raise SystemExit(f'{label}: expected one Party marker, got {n}: {old[:110]!r}')
 p=p.replace(old,new,1);print('PASS',label)
pc(
 'retryRoomCreateLegacy921(ref,name,category,seats,priv,first,second)',
 'retryRoomCreateLegacy921(ref,name,category,seats,priv,joinCode,first,second)',
 'carry exact displayed Room Code through compact fallback')
pc(
 'private void retryRoomCreateLegacy921(DocumentReference ref,String name,String category,int seats,boolean priv,Exception first,Exception second){',
 'private void retryRoomCreateLegacy921(DocumentReference ref,String name,String category,int seats,boolean priv,String joinCode,Exception first,Exception second){',
 'retain code in final legacy room creation')
pc(
 'legacy.put("hasPassword",false);legacy.put("locked",false);',
 'legacy.put("hasPassword",false);legacy.put("joinCode",joinCode);legacy.put("locked",false);',
 'legacy room code now stored for cross-phone numeric search')

pc(
 '''    private void openRoomDoc891(DocumentSnapshot d){
        openCloudRoom(d.getId(),''',
 '''    private void openRoomDoc891(DocumentSnapshot d){
        // Older fallback room records predate joinCode. Its own host may safely
        // backfill the code, so another phone can search by the displayed ID.
        if(d!=null && user!=null && user.getUid().equals(d.getString("ownerUid"))
           && (d.getString("joinCode")==null||d.getString("joinCode").isEmpty())){
            d.getReference().update("joinCode",shortId(d.getId()))
                .addOnFailureListener(e->KingStability.nonFatal(this,"room-code-backfill-970",e));
        }
        openCloudRoom(d.getId(),''',
 'host backfills existing rooms missing their advertised numeric join code')

party.write_text(p)

gradle=root/'app/build.gradle';g=gradle.read_text()
old="versionCode 160; versionName '9.6.9-party-memory-pressure'"
if g.count(old)!=1:raise SystemExit('Expected last successful v9.6.9 source')
gradle.write_text(g.replace(old,"versionCode 161; versionName '9.7.0-realtime-social-room-id'",1))
print('PASS v9.7.0 versionCode 161 built-source patch ready')
