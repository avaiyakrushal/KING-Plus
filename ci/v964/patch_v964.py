"""KING Plus v9.6.4: Party low-memory & cross-phone authentic identity / room joining.

Build from successful v9.6.3 source. No copying any third-party code or assets.
* Correctly join with the shown 6-digit room code, querying Firestore joinCode;
  choose a room explicitly if aliases collide (do not guess a wrong room).
* 6-digit user search queries server-side publicId (not unbounded .get all).
  Offer precise lookup by real Firebase UID and always navigate with snapshot.getId().
* Reduce Party memory: bounded 6MiB bitmap cache, 256px thumbnails,
  two image fetch workers, no per-room-card Firestore persistent count listener,
  and no double heavy Party UI build before a new member joins.
* Prior native crash attribution, live emoji, Ludo and security are retained.
"""
from pathlib import Path
import shutil,sys,re

root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
shutil.copy2(Path(__file__).with_name('KingIdentity964.java'),pkg/'KingIdentity964.java')

def once(filename,old,new,label):
    p=pkg/filename
    s=p.read_text(errors='replace')
    n=s.count(old)
    if n!=1:raise SystemExit(f'{label}: expected 1 marker; got {n}: {old[:140]!r}')
    p.write_text(s.replace(old,new,1))
    print('PATCH_OK',label)

once('PartyActivity.java',
     '    private final android.util.LruCache<String,Bitmap> photoCache540 = new android.util.LruCache<>(24);',
     '''    // Bitmap cache is measured in bytes, not image count. 24 large photos could exceed 100 MB.
    private final android.util.LruCache<String,Bitmap> photoCache540 =
        new android.util.LruCache<String,Bitmap>(6*1024*1024){
            @Override protected int sizeOf(String key,Bitmap value){
                return value==null?0:value.getByteCount();
            }
        };
    private final java.util.concurrent.ExecutorService roomImageExecutor964 =
        java.util.concurrent.Executors.newFixedThreadPool(2);''',
     'bounded 6MiB bitmap cache and two fetch workers')

# Fetch tiny thumbnails; release connections/bitmaps quickly. Avoid unlimited new threads.
p=pkg/'PartyActivity.java'
s=p.read_text()
first=s.find('    private void loadProfilePhoto(ImageView image,TextView fallback,String url){')
last=s.find('    private void toggleProfileFollow(',first)
if first<0 or last<0 or last<first or s.count('    private void loadProfilePhoto(')!=1:raise SystemExit('Profile photo method boundaries unexpected')
new_photo=r'''    private void loadProfilePhoto(ImageView image,TextView fallback,String url){
        if(image==null||url==null||url.isEmpty())return;
        image.setTag(url);
        Bitmap cached=photoCache540.get(url);
        if(cached!=null&&!cached.isRecycled()){
            image.setImageBitmap(cached);
            if(fallback!=null)fallback.setVisibility(View.GONE);
            return;
        }
        try{
            roomImageExecutor964.execute(()->{
                Bitmap bitmap=null;
                try{
                    URL source=new URL(url);
                    if(!"https".equalsIgnoreCase(source.getProtocol()))return;
                    // Decode only the bounds on the first stream. Never allocate a full photo.
                    BitmapFactory.Options bounds=new BitmapFactory.Options();
                    bounds.inJustDecodeBounds=true;
                    java.net.URLConnection c=source.openConnection();
                    c.setConnectTimeout(7000);c.setReadTimeout(7000);
                    try(java.io.InputStream in=c.getInputStream()){
                        BitmapFactory.decodeStream(in,null,bounds);
                    }
                    if(bounds.outWidth<=0||bounds.outHeight<=0)return;
                    int sample=1;
                    while(bounds.outWidth/sample>256||bounds.outHeight/sample>256)sample*=2;
                    BitmapFactory.Options options=new BitmapFactory.Options();
                    options.inSampleSize=sample;
                    options.inPreferredConfig=Bitmap.Config.RGB_565;
                    java.net.URLConnection small=source.openConnection();
                    small.setConnectTimeout(7000);small.setReadTimeout(7000);
                    try(java.io.InputStream in=small.getInputStream()){
                        bitmap=BitmapFactory.decodeStream(in,null,options);
                    }
                    if(bitmap==null)return;
                    if(roomImageExecutor964.isShutdown())return;
                    photoCache540.put(url,bitmap);
                    final Bitmap photo964=bitmap;
                    runOnUiThread(()->{
                        if(!isFinishing()&&!isDestroyed()&&url.equals(image.getTag())){
                            image.setImageBitmap(photo964);
                            if(fallback!=null)fallback.setVisibility(View.GONE);
                        }
                    });
                }catch(Exception ignored){}
                catch(OutOfMemoryError ignored){
                    // The operating system can be under native-memory pressure even if Java heap is small.
                }
            });
        }catch(java.util.concurrent.RejectedExecutionException ignored){}
    }

'''
s=s[:first]+new_photo+s[last:]
p.write_text(s)
print('PATCH_OK','decode max 256px profile thumbnails without uncontrolled thread creation')

once('PartyActivity.java',
     'if(db!=null&&id!=null&&!id.isEmpty()){ListenerRegistration countListener940=db.collection("live_rooms").document(id).collection("members").addSnapshotListener((x,e)->{if(e!=null||x==null){count.setText("LIVE");return;}count.setText(String.valueOf(x.size()));});lobbyMemberCountListeners940.add(countListener940);}',
     '''if(db!=null&&id!=null&&!id.isEmpty()){
            // One-time count per room card; dozens of persistent listeners would consume memory.
            db.collection("live_rooms").document(id).collection("members").limit(100).get()
                .addOnSuccessListener(x->{if(!isFinishing()&&!isDestroyed()&&count.isAttachedToWindow())count.setText(String.valueOf(x.size()));})
                .addOnFailureListener(e->{if(!isFinishing()&&!isDestroyed()&&count.isAttachedToWindow())count.setText("LIVE");});
        }''',
     'replace one Firestore listener per lobby card with a bounded one-time lookup')

once('PartyActivity.java',
     '''        if(roomPrivate&&!isOwner()) tryPrivateEntry();
        else {
            renderParty();
            setOnlineState900(false,"Joining • syncing account");
            joinMemberThenOpen891();
        }''',
     '''        if(roomPrivate&&!isOwner()) tryPrivateEntry();
        else {
            // Build the expensive Party UI only AFTER Firebase confirms membership.
            // Previously renderParty ran here and a second time in finishMemberJoin921().
            showJoiningRoom964();
            joinMemberThenOpen891();
        }''',
     'avoid duplicate room render before membership accepted')

once('PartyActivity.java',
     '    private void renderParty() {',
     '''    private void showJoiningRoom964(){
        KingCrashWatch958.mark(this,"party-member-sync-964");
        LinearLayout waiting964=new LinearLayout(this);
        waiting964.setOrientation(LinearLayout.VERTICAL);
        waiting964.setGravity(Gravity.CENTER);
        waiting964.setBackgroundColor(0xff172b36);
        TextView label964=tv("Connecting to Party…\\nVerifying your account and room access",17,Color.WHITE,true);
        label964.setGravity(Gravity.CENTER);
        waiting964.addView(label964,new LinearLayout.LayoutParams(-1,dp(110)));
        setSafeContentView(waiting964);
    }

    private void renderParty() {''',
     'lightweight joining screen before expensive room rendering')

# Avoid running async member success against a destroyed old Party.
once('PartyActivity.java',
     '    private void finishMemberJoin921(){\n        memberSeen=true;renderParty();',
     '    private void finishMemberJoin921(){\n        if(isFinishing()||isDestroyed()||roomId==null||!cloudRoom)return;\n        memberSeen=true;renderParty();',
     'ignore member callback after Activity exit')

# Clear pending thumbnail decoding on Activity destruction.
p=pkg/'PartyActivity.java';s=p.read_text()
marker='@Override protected void onDestroy(){'
if s.count(marker)!=1:raise SystemExit('Party onDestroy marker not unique')
p.write_text(s.replace(marker,
     '@Override protected void onDestroy(){roomImageExecutor964.shutdownNow();photoCache540.evictAll();',1))
print('PATCH_OK','release thumbnail decoder and memory on exit')

# Accurate six-digit Firestore room-code lookup (existing room documents already have joinCode).
anchor='    private boolean tryJoinRoomInput959(String input959){'
room_helper=r'''    private void joinRoomByCode964(String code964){
        if(db==null||user==null){toast("Sign in before joining Party");return;}
        if(!KingIdentity964.joinCode(code964)){toast("Enter a valid 6-digit Room Code");return;}
        KingCrashWatch958.mark(this,"party-room-code-lookup-964");
        db.collection("live_rooms").whereEqualTo("joinCode",code964).limit(10).get()
            .addOnSuccessListener(result964->{
                if(isFinishing()||isDestroyed())return;
                java.util.ArrayList<DocumentSnapshot> live964=new java.util.ArrayList<>();
                if(result964!=null)for(DocumentSnapshot d964:result964.getDocuments()){
                    if(d964.exists()&&code964.equals(d964.getString("joinCode"))
                       &&!Boolean.TRUE.equals(d964.getBoolean("closed")))live964.add(d964);
                }
                if(live964.isEmpty()){toast("No active Party matches that code. Ask the host to share the full invite link.");return;}
                if(live964.size()==1){openRoomDoc891(live964.get(0));return;}
                String[] choices964=new String[live964.size()];
                for(int k964=0;k964<live964.size();k964++){
                    DocumentSnapshot d964=live964.get(k964);
                    choices964[k964]=str(d964,"name","Live Party")+" • "+str(d964,"ownerName","Host")
                        +" • "+d964.getId().substring(0,Math.min(8,d964.getId().length()));
                }
                new AlertDialog.Builder(this).setTitle("Multiple Party rooms use this short code")
                    .setMessage("Choose the host and Party name, or ask for the exact invitation link.")
                    .setItems(choices964,(dialog,index)->openRoomDoc891(live964.get(index)))
                    .setNegativeButton("Cancel",null).show();
            })
            .addOnFailureListener(e->{if(!isFinishing()&&!isDestroyed())toast("Room lookup failed: "+msg(e));});
    }

'''
s=p.read_text()
if s.count(anchor)!=1:raise SystemExit('Party join method marker missing')
s=s.replace(anchor,room_helper+anchor,1)
old='''    private boolean tryJoinRoomInput959(String input959){
        final String id959=KingRoomInvite959.parse(input959);'''
new='''    private boolean tryJoinRoomInput959(String input959){
        final String candidate964=input959==null?"":input959.trim();
        if(KingIdentity964.joinCode(candidate964)){joinRoomByCode964(candidate964);return true;}
        final String id959=KingRoomInvite959.parse(input959);'''
if s.count(old)!=1:raise SystemExit('Party invite parser original marker unexpected')
s=s.replace(old,new,1)
p.write_text(s)
print('PATCH_OK','numeric Room Code now resolves Firestore joinCode; collisions require manual choice')

once('PartyActivity.java',
     'box959.setHint("Paste Room ID or kingplus://party/...");',
     'box959.setHint("6-digit code, full ID, or kingplus://party/...");',
     'show valid room-code input options')

once('PartyActivity.java',
     '.setMessage("Paste the shared KINGROOM code or complete room invitation.")',
     '.setMessage("Enter the 6-digit room code, full Room ID, or shared invitation.")',
     'room join prompt supports correct code')

once('PartyActivity.java',
     '.setPositiveButton("Search",(d,w)->{lobbyFilter=e.getText().toString().trim();if(!lobbyFilter.isEmpty())saveLobbySearch940(lobbyFilter);renderLobby(selected);}).show();',
     '.setPositiveButton("Search",(d,w)->{lobbyFilter=e.getText().toString().trim();if(!lobbyFilter.isEmpty())saveLobbySearch940(lobbyFilter);if(KingIdentity964.joinCode(lobbyFilter))joinRoomByCode964(lobbyFilter);else renderLobby(selected);}).show();',
     'Party lobby search resolves room numeric ID instead of filtering a short local list')

# Social follows use real Firebase user identifiers, never the six-digit alias.
p=pkg/'SocialActivity.java';s=p.read_text()
old='''        if (raw.matches("[0-9]{6}")) {
            db.collection("public_profiles").get()
                .addOnSuccessListener(s -> {
                    List<DocumentSnapshot> matches=new ArrayList<>();
                    for(DocumentSnapshot d:s.getDocuments()) if(raw.equals(publicId(d.getId()))) matches.add(d);
                    renderProfiles(matches, "No user found with that ID");
                })
                .addOnFailureListener(e -> { setLoading("ID search unavailable"); toast(safe(e.getLocalizedMessage())); });
        } else {'''
new='''        if (KingIdentity964.sixDigits(raw)) {
            // A server-side indexed lookup, not an unbounded download of every user.
            db.collection("public_profiles").whereEqualTo("publicId",raw).limit(15).get()
                .addOnSuccessListener(result->{
                    List<DocumentSnapshot> matches=new ArrayList<>();
                    for(DocumentSnapshot d:result.getDocuments())
                        if(raw.equals(publicId(d.getId())))matches.add(d);
                    renderProfiles(matches, "No verified user found with that ID");
                })
                .addOnFailureListener(e->{setLoading("ID lookup unavailable");toast(safe(e.getLocalizedMessage()));});
        } else if (KingIdentity964.firebaseUid(raw)) {
            // Exact Firebase UID is globally authoritative, unlike the display alias.
            db.collection("public_profiles").document(raw).get()
                .addOnSuccessListener(d->renderProfiles(
                    d.exists()?java.util.Collections.singletonList(d):java.util.Collections.emptyList(),
                    "No user found with that verified UID"))
                .addOnFailureListener(e->{setLoading("UID lookup unavailable");toast(safe(e.getLocalizedMessage()));});
        } else {'''
n=s.count(old)
if n!=1:raise SystemExit('Social numeric lookup marker count '+str(n))
s=s.replace(old,new,1)
p.write_text(s)
print('PATCH_OK','server-side numeric alias lookup plus exact Firebase UID lookup')

old='for (DocumentSnapshot d : docs) { String uid=d.getString("uid");'
new='for (DocumentSnapshot d : docs) { String uid=d.getId();'
if s.count(old)!=1:raise SystemExit('Social UID identity marker missing')
s=s.replace(old,new,1)
print('PATCH_OK','Social profile click follows authoritative Firestore document UID')

# Provide users a way to compare two phones and copy the authoritative UID.
needle='root.addView(quick,new LinearLayout.LayoutParams(-1,dp(70)));'
if s.count(needle)!=1:
    raise SystemExit('Social quick links marker count '+str(s.count(needle)))
addition='''root.addView(quick,new LinearLayout.LayoutParams(-1,dp(70)));
        if(me!=null){
            TextView trueUid964=label("My verified account UID: "+me.getUid()+"  •  Tap to copy",11,MUTED,false);
            trueUid964.setMaxLines(2);
            trueUid964.setOnClickListener(v->{
                android.content.ClipboardManager clipboard964=
                    (android.content.ClipboardManager)getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                if(clipboard964!=null)clipboard964.setPrimaryClip(
                    android.content.ClipData.newPlainText("KING verified UID",me.getUid()));
                toast("Verified Firebase UID copied");
            });
            root.addView(trueUid964,new LinearLayout.LayoutParams(-1,dp(58)));
        }'''
s=s.replace(needle,addition,1)
p.write_text(s)
print('PATCH_OK','My verified UID can be copied for multi-phone identity troubleshooting')

# All profile share messages can include exact UID for precise lookup.
p=pkg/'SocialActivity.java'
s=p.read_text()
pattern='''"Find "+name+" on KING Plus • KING ID: "+publicId(uid)'''
if s.count(pattern)!=1:raise SystemExit('Social share ID marker count '+str(s.count(pattern)))
s=s.replace(pattern, '''"Find "+name+" on KING Plus • KING ID: "+publicId(uid)+"\\nVerified Firebase UID: "+uid''',1)
p.write_text(s)
print('PATCH_OK','Social shared profiles include authentic UID')

profile=pkg/'KingPublicProfileActivity.java'
s=profile.read_text()
old='''"KING Plus • "+name+" • KING ID "+publicId(uid)'''
if s.count(old)!=1:raise SystemExit('Public profile share marker count '+str(s.count(old)))
profile.write_text(s.replace(old, '''"KING Plus • "+name+" • KING ID "+publicId(uid)+"\\nVerified Firebase UID: "+uid''',1))
print('PATCH_OK','Public profile shared ID includes authentic UID')

gradle=root/'app/build.gradle'
s=gradle.read_text()
old="versionCode 154; versionName '9.6.3-online-ludo-modes'"
if s.count(old)!=1:raise SystemExit('Not built from v9.6.3 source')
gradle.write_text(s.replace(old,"versionCode 155; versionName '9.6.4-party-memory-social-ids'",1))
print('PASS v9.6.4: limited Party memory and authentic social IDs')
