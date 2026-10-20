package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SocialActivity extends Activity {
    private static final int PURPLE = 0xff7c4dff;
    private static final int DARK = 0xff181528;
    private static final int MUTED = 0xff777186;

    private FirebaseUser me;
    private FirebaseFirestore db;
    private LinearLayout list;
    private EditText search;
    private TextView status;
    private String activeSocialScreen965="discover";
    private boolean socialHasResumed965;
    private com.google.firebase.firestore.ListenerRegistration socialOut970,socialIn970;
    private final java.util.Set<String> following970=new java.util.HashSet<>();
    private final java.util.Set<String> followers970=new java.util.HashSet<>();
    private final java.util.Set<String> pendingFollows970=new java.util.HashSet<>();
    private final java.util.Map<String,String> issuedPublicIds10037=new java.util.HashMap<>();
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
        };


    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        try { me = FirebaseAuth.getInstance().getCurrentUser(); } catch (Exception ignored) { me = null; }
        try { db = FirebaseFirestore.getInstance(); } catch (Exception ignored) { db = null; }
        render();
        if (cloudReady()) {
            verifyOwnProfile996();
            String initial10018=getIntent()==null?"discover":getIntent().getStringExtra("initialSocialTab10018");
            if("followers".equals(initial10018))loadFollowers();
            else if("following".equals(initial10018))loadFollowing();
            else if("friends".equals(initial10018))loadFriends();
            else loadDiscover();
        } else {
            status.setText("Local/test mode • sign in with Firebase for live people search");
            showLocalDemo();
        }
    }

    private boolean cloudReady() { return me != null && db != null; }
    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable bg(int color, int radius) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d; }
    private TextView label(String s, int size, int color, boolean bold) { TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); if (bold) t.setTypeface(null, Typeface.BOLD); return t; }

    private void render() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(0xfff7f7fb);
        LinearLayout head = new LinearLayout(this); head.setGravity(Gravity.CENTER_VERTICAL); head.setPadding(dp(12), dp(12), dp(12), dp(6));
        TextView back = label("‹", 38, DARK, false); back.setGravity(Gravity.CENTER); back.setOnClickListener(v -> finish()); head.addView(back, new LinearLayout.LayoutParams(dp(48), dp(56)));
        LinearLayout title = new LinearLayout(this); title.setOrientation(LinearLayout.VERTICAL); title.addView(label("People", 28, DARK, true)); status = label("Discover • Follow • Chat", 12, MUTED, false); title.addView(status); head.addView(title, new LinearLayout.LayoutParams(0, dp(60), 1));
        TextView inbox = label("✉", 27, PURPLE, true); inbox.setGravity(Gravity.CENTER); inbox.setOnClickListener(v -> startActivity(new Intent(this, InboxActivity.class))); head.addView(inbox, new LinearLayout.LayoutParams(dp(56), dp(56))); root.addView(head);

        LinearLayout find = new LinearLayout(this); find.setPadding(dp(14), dp(4), dp(14), dp(8)); find.setGravity(Gravity.CENTER_VERTICAL);
        search = new EditText(this); search.setSingleLine(true); search.setHint("Search people by name or ID"); search.setTextColor(DARK); search.setHintTextColor(0xff9993a6); search.setPadding(dp(14), 0, dp(14), 0); search.setBackground(bg(Color.WHITE, 18)); find.addView(search, new LinearLayout.LayoutParams(0, dp(52), 1));
        TextView go = label("Search", 14, Color.WHITE, true); go.setGravity(Gravity.CENTER); go.setBackground(bg(PURPLE, 18)); LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(dp(82), dp(52)); gp.setMargins(dp(8),0,0,0); find.addView(go,gp); go.setOnClickListener(v -> runSearch()); root.addView(find);

        LinearLayout quick = new LinearLayout(this); quick.setPadding(dp(12),0,dp(12),dp(8));
        addQuick(quick,"✨\nDiscover",this::loadDiscover); addQuick(quick,"＋\nFollowing",this::loadFollowing); addQuick(quick,"👥\nFollowers",this::loadFollowers); addQuick(quick,"💜\nFriends",this::loadFriends); root.addView(quick,new LinearLayout.LayoutParams(-1,dp(70)));
        if(me!=null){
            final String ownerSocialUid992=me.getUid();
            final String[] serverKingId10038={null};
            LinearLayout connectBar992=new LinearLayout(this);
            connectBar992.setOrientation(LinearLayout.VERTICAL);
            connectBar992.setPadding(dp(14),dp(10),dp(14),dp(12));
            connectBar992.setBackground(bg(0xffeae4ff,18));
            TextView kingIdText992=label("👑  My KING ID: verifying…",19,PURPLE,true);
            if(cloudReady())db.collection("public_profiles").document(ownerSocialUid992)
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(doc10038->{
                    if(!currentAccount970()||!kingIdText992.isAttachedToWindow())return;
                    String found10038=doc10038.exists()?KingPublicId9996.verified(doc10038.getString("publicId")):null;
                    serverKingId10038[0]=found10038;
                    kingIdText992.setText("👑  My KING ID: "+(found10038==null?"Not issued (Test Connection)":found10038));
                }).addOnFailureListener(error10038->{
                    if(currentAccount970()&&kingIdText992.isAttachedToWindow())
                        kingIdText992.setText("👑  My KING ID: unavailable • retry online");
                });
            kingIdText992.setOnClickListener(v->{
                if(!currentAccount970()||serverKingId10038[0]==null){
                    toast("Confirm your KING ID on the server using Test Connection");return;
                }
                android.content.ClipboardManager cb=(android.content.ClipboardManager)getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                if(cb!=null)cb.setPrimaryClip(android.content.ClipData.newPlainText("My KING ID",serverKingId10038[0]));
                toast("Verified KING ID copied");
            });
            connectBar992.addView(kingIdText992,new LinearLayout.LayoutParams(-1,dp(36)));
            TextView help992=label("Ask friends to download KING Plus, sign in with another Google account, search this ID, then Follow and Message.",12,DARK,false);
            help992.setVisibility(android.view.View.GONE);
            connectBar992.addView(help992);
            LinearLayout socialActions992=new LinearLayout(this);
            socialActions992.setGravity(Gravity.CENTER);
            addQuick(socialActions992,"↗  Share\nKING ID",()->{
                if(!currentAccount970()||serverKingId10038[0]==null){
                    toast("Your KING ID is not confirmed on Firebase; run Test Connection");return;
                }
                Intent share=new Intent(Intent.ACTION_SEND);share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT,KingSocialShare992.shareKingId(
                  serverKingId10038[0],ownerSocialUid992,KingSocialShare992.APP_APK_URL));
                startActivity(Intent.createChooser(share,"Invite friends to KING Plus"));
            });
            addQuick(socialActions992,"⬇  Share\nApp APK",()->{
                Intent share=new Intent(Intent.ACTION_SEND);share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT,
                    "Download KING Plus APK and sign in with your own Google account: "+
                    KingSocialShare992.APP_APK_URL);
                startActivity(Intent.createChooser(share,"Share KING Plus APK"));
            });
            addQuick(socialActions992,"🎉  Join\nParty ID",this::socialJoinParty992);
            addQuick(socialActions992,"🛠 Test Connection",this::connectionTest996);
            connectBar992.addView(socialActions992,new LinearLayout.LayoutParams(-1,dp(70)));
            root.addView(connectBar992);
            TextView trueUid964=label("My verified account UID: "+me.getUid()+"  •  Tap to copy",11,MUTED,false);
            trueUid964.setVisibility(android.view.View.GONE);
            trueUid964.setMaxLines(2);
            trueUid964.setOnClickListener(v->{
                android.content.ClipboardManager clipboard964=
                    (android.content.ClipboardManager)getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                if(clipboard964!=null)clipboard964.setPrimaryClip(
                    android.content.ClipData.newPlainText("KING verified UID",me.getUid()));
                toast("Verified Firebase UID copied");
            });
            root.addView(trueUid964,new LinearLayout.LayoutParams(-1,dp(58)));
        }

        ScrollView sv = new ScrollView(this); list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); list.setPadding(dp(12),dp(4),dp(12),dp(24)); sv.addView(list); root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }

    private void addQuick(LinearLayout row, String text, Runnable action) { TextView x=label(text,12,DARK,true); x.setGravity(Gravity.CENTER); x.setBackground(bg(Color.WHITE,16)); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(62),1); p.setMargins(dp(3),0,dp(3),0); row.addView(x,p); x.setOnClickListener(v->KingSafe.run(this,"ui-action",action)); }
    private void setLoading(String text) { list.removeAllViews(); TextView t=label(text,14,MUTED,false); t.setGravity(Gravity.CENTER); list.addView(t,new LinearLayout.LayoutParams(-1,dp(72))); }

    private boolean currentAccount970(){
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

    private void verifyOwnProfile996(){
        if(!currentAccount970())return;
        final String uid=me.getUid();
        final String pid=publicId(uid);
        Map<String,Object> info=new HashMap<>();
        info.put("uid",uid);info.put("publicId",pid);
        // Merge-only. Do not overwrite existing name/photo or private info.
        db.collection("public_profiles").document(uid)
            .set(info,com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener(v->
                db.collection("public_profiles").document(uid)
                    .get(com.google.firebase.firestore.Source.SERVER)
                    .addOnSuccessListener(doc->{
                        if(!currentAccount970())return;
                        if(doc.exists()&&uid.equals(doc.getString("uid"))
                            &&pid.equals(doc.getString("publicId")))
                            status.setText("✅ KING ID "+pid+" confirmed on Firebase SERVER");
                        else status.setText("⚠ Profile not confirmed on SERVER");
                    })
                    .addOnFailureListener(e->{
                        if(currentAccount970())status.setText(
                            "⚠ Profile server check failed: "+safe(e.getLocalizedMessage()));
                    })
            ).addOnFailureListener(e->{
                if(currentAccount970())status.setText(
                    "⚠ Cannot register KING ID: "+safe(e.getLocalizedMessage()));
            });
    }
    private void connectionTest996(){
        if(!cloudReady()){toast("Sign in to Firebase first");return;}
        final String uid=me.getUid();
        String project="unknown";
        try{project=FirebaseAuth.getInstance().getApp().getOptions().getProjectId();}
        catch(Exception ignored){}
        final String projectId=project;
        db.collection("public_profiles").document(uid)
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnCompleteListener(done->{
              boolean found=done.isSuccessful()&&done.getResult()!=null&&done.getResult().exists();
              String detail=found?"Profile FOUND on SERVER":"Profile MISSING or denied on SERVER";
              if(!done.isSuccessful()&&done.getException()!=null)
                  detail+="\n"+safe(done.getException().getLocalizedMessage());
              String message="Firebase project: "+projectId+
                  "\nKING ID: "+publicId(uid)+"\nAccount UID: "+uid+
                  "\n"+detail+
                  "\n\nOn both phones check SAME project and DIFFERENT UIDs. "+
                  "If PERMISSION_DENIED, secure Firestore Rules need publishing in Firebase Console.";
              if(isFinishing()||isDestroyed())return;
              new AlertDialog.Builder(this).setTitle("KING Plus connection test")
                  .setMessage(message)
                  .setPositiveButton("Retry profile registration",(d,w)->verifyOwnProfile996())
                  .setNegativeButton("Close",null).show();
          });
    }

    private void publishOwnProfile() {
        if(!currentAccount970())return;
        final String ownUid970=me.getUid();
        final SharedPreferences prefs970=getSharedPreferences("MainActivity",MODE_PRIVATE);
        final com.google.firebase.firestore.DocumentReference profile970=
            db.collection("public_profiles").document(ownUid970);
        profile970.get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(existing970->{
            if(!currentAccount970())return;
            Map<String,Object> patch970=new HashMap<>();
            patch970.put("uid",ownUid970);
            // A server-issued ID is permanent. Never replace it with an
            // unsigned UID-hash approximation on a subsequent profile refresh.
            final String issued10044=existing970.exists()
                ?KingPublicId9996.verified(existing970.getString("publicId")):null;
            if(issued10044==null && (!existing970.exists()
                 ||existing970.getString("publicId")==null)){
                // Legacy accounts need server allocation, not a locally invented ID.
                // Preserve other profile fields, leave publicId untouched.
            }
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
                patch970.put("vipLevel",0);
                patch970.put("vipPoints",0L);
                patch970.put("levelTier",LevelSystem.levelTier(levels970.level));
            }
            // Merge ONLY missing/default identity fields, preserving edits saved
            // by another device (name/photo/bio/level) and the immutable Firebase UID.
            profile970.set(patch970,com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(v->{
                    if(currentAccount970()&&status!=null)
                        status.setText(issued10044==null
                            ?"Connected • KING ID awaiting server issuance"
                            :"Connected • KING ID "+issued10044);
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

    private void joinFullRoom996(String roomId){
        if(!currentAccount970())return;
        db.collection("live_rooms").document(roomId)
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(room->{
              if(!currentAccount970())return;
              if(room==null||!room.exists()||Boolean.TRUE.equals(room.getBoolean("closed"))){
                  toast("Room is closed or not found on Firebase SERVER");return;
              }
              if(Boolean.TRUE.equals(room.getBoolean("isPrivate")))
                  toast("Private room requires host permission to enter.");
              Intent join=new Intent(this,PartyActivity.class);
              join.putExtra("directRoomId",room.getId());
              join.putExtra("displayName",me.getDisplayName()==null?"KING User":me.getDisplayName());
              startActivity(join);
          }).addOnFailureListener(err->toast("Room lookup failed: "+safe(err.getLocalizedMessage())));
    }
    private void socialJoinParty992(){
        if(!currentAccount970()){toast("Sign in first");return;}
        final EditText entry=new EditText(this);
        entry.setHint("Full KINGROOM invite or six-digit Join Code");
        entry.setSingleLine(true);
        new AlertDialog.Builder(this)
          .setTitle("🎉 Join another phone's Party")
          .setMessage("Ask your friend to tap ↗ Share while inside their live Party.")
          .setView(entry).setNegativeButton("Cancel",null)
          .setPositiveButton("Find Room",(d,w)->{
              final String raw=entry.getText().toString().trim();
              final String full=KingRoomInvite959.parse(raw);
              if(!full.isEmpty()){joinFullRoom996(full);return;}
              if(!raw.matches("[0-9]{6}")){
                  toast("Paste a complete KINGROOM invitation or 6-digit Join Code");return;
              }
              db.collection("live_rooms").whereEqualTo("joinCode",raw).limit(8)
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(q->{
                    if(!currentAccount970())return;
                    List<DocumentSnapshot> rooms=new ArrayList<>();
                    for(DocumentSnapshot room:q.getDocuments()){
                        if(!Boolean.TRUE.equals(room.getBoolean("closed")))rooms.add(room);
                    }
                    if(rooms.isEmpty()){toast("No open Party matches "+raw);return;}
                    if(rooms.size()==1){joinFullRoom996(rooms.get(0).getId());return;}
                    String[] labels=new String[rooms.size()];
                    for(int k=0;k<rooms.size();k++)
                        labels[k]=safe(rooms.get(k).getString("name"))+" • "+rooms.get(k).getId();
                    new AlertDialog.Builder(this).setTitle("Choose Party")
                        .setItems(labels,(dialog,index)->joinFullRoom996(rooms.get(index).getId()))
                        .setNegativeButton("Cancel",null).show();
                }).addOnFailureListener(error->
                    toast("Room code lookup failed: "+safe(error.getLocalizedMessage())));
          }).show();
    }

    private void runSearch() {
        activeSocialScreen965="search";
        if (!cloudReady()) {
            setLoading("Sign in with Google/Firebase to search real KING Plus accounts. Offline/demo users are hidden.");
            return;
        }
        String raw = KingSocialShare992.extractAccount(search.getText().toString().trim());
        if(!raw.equals(search.getText().toString().trim()))search.setText(raw);
        final int generation970=++socialViewGeneration970;
        String q = raw.toLowerCase(Locale.US);
        if (q.isEmpty()) { loadDiscover(); return; }
        setLoading("Searching…");
        if(raw.matches("(?i)VIP[1-9][0-9]{5}")) {
            final String premium=raw.substring(3);
            db.collection("king_vip_number_registry").document(premium)
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(reservation->{
                    if(!currentSearch970(generation970,raw))return;
                    String target=reservation.getString("uid");
                    if(!KingDocumentId10036.valid(target)){setLoading("VIP ID not found");return;}
                    db.collection("king_vip_identity_grants").document(target)
                        .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(grant->{
                            if(!currentSearch970(generation970,raw))return;
                            if(!target.equals(grant.getString("uid"))||!premium.equals(grant.getString("vipNumber"))){setLoading("VIP number unavailable");return;}
                            db.collection("public_profiles").document(target).get(com.google.firebase.firestore.Source.SERVER)
                                .addOnSuccessListener(user->{if(currentSearch970(generation970,raw))renderProfiles(user.exists()?java.util.Collections.singletonList(user):java.util.Collections.emptyList(),"VIP profile not found");})
                                .addOnFailureListener(err->{if(currentSearch970(generation970,raw))setLoading("VIP search unavailable");});
                        });
                }).addOnFailureListener(err->{if(currentSearch970(generation970,raw))setLoading("VIP lookup unavailable");});
        } else if (KingIdentity964.sixDigits(raw)) {
            // A server-side indexed lookup, not an unbounded download of every user.
            db.collection("public_profiles").whereEqualTo("publicId",raw).limit(15).get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(result->{
                    if(!currentSearch970(generation970,raw))return;
                    List<DocumentSnapshot> matches=new ArrayList<>();
                    for(DocumentSnapshot d:result.getDocuments())
                        if(raw.equals(KingPublicId9996.verified(d.getString("publicId"))))matches.add(d);
                    renderProfiles(matches, "No synced user found. Ask friend to update KING Plus and open People > Test Connection");
                })
                .addOnFailureListener(e->{if(!currentSearch970(generation970,raw))return;setLoading("ID lookup unavailable");toast(safe(e.getLocalizedMessage()));});
        } else if (KingIdentity964.firebaseUid(raw)) {
            // Exact Firebase UID is globally authoritative, unlike the display alias.
            db.collection("public_profiles").document(raw).get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(d->{
                    if(!currentSearch970(generation970,raw))return;
                    renderProfiles(d.exists()?java.util.Collections.singletonList(d)
                        :java.util.Collections.emptyList(),"No user found with that verified UID");
                })
                .addOnFailureListener(e->{if(!currentSearch970(generation970,raw))return;setLoading("UID lookup unavailable");toast(safe(e.getLocalizedMessage()));});
        } else {
            db.collection("public_profiles").orderBy("searchName").startAt(q).endAt(q + "\uf8ff").limit(30).get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(result970->{if(currentSearch970(generation970,raw))renderProfiles(result970.getDocuments(),"No users found");})
                .addOnFailureListener(e -> {if(!currentSearch970(generation970,raw))return;setLoading("Search unavailable");toast(safe(e.getLocalizedMessage()));});
        }
    }

    private void loadDiscover() {
        activeSocialScreen965="discover";
        if(!currentAccount970()){
            setLoading("Sign in with your own Firebase account to see real friends. Demo profiles are disabled.");
            return;
        }
        final int generation970=++socialViewGeneration970;
        setLoading("Loading people…");
        db.collection("public_profiles").limit(30)
            .get(com.google.firebase.firestore.Source.SERVER)
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
    private void renderProfiles(List<DocumentSnapshot> docs, String empty) {
        list.removeAllViews(); int shown=0;
        for (DocumentSnapshot d : docs) { String uid=d.getId(); if(!KingDocumentId10036.valid(uid)||me!=null&&uid.equals(me.getUid())) continue;
            boolean directoryScreen="search".equals(activeSocialScreen965)||"discover".equals(activeSocialScreen965);
            if(directoryScreen&&!KingPublicMeVisibility9984.discoverable(d))continue;
            rememberIssuedId10037(d);addPerson(uid, nameOf(d), clean(d.getString("bio")),clean(d.getString("photoUrl")),d.getLong("level"),d.getLong("vipLevel")); shown++; }
        if (shown==0) setLoading(empty);
    }

    private void loadFollowing() {
        activeSocialScreen965="following";
        if(!currentAccount970()){
            setLoading("Sign in with your own Firebase account to see real friends. Demo profiles are disabled.");
            return;
        }
        startLiveRelations970();
        renderLiveRelations970();
    }
    private void loadFollowers() {
        activeSocialScreen965="followers";
        if(!currentAccount970()){
            setLoading("Sign in with your own Firebase account to see real friends. Demo profiles are disabled.");
            return;
        }
        startLiveRelations970();
        renderLiveRelations970();
    }
    private void loadFriends() {
        activeSocialScreen965="friends";
        if(!currentAccount970()){
            setLoading("Sign in with your own Firebase account to see real friends. Demo profiles are disabled.");
            return;
        }
        startLiveRelations970();
        renderLiveRelations970();
    }
    private void loadProfileCard970(String uid,int generation970,String expectedScreen970) {
        if(!KingSocialRealtime970.showUid(uid,me==null?null:me.getUid()))return;
        db.collection("public_profiles").document(uid)
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(profile970->{
                if(!currentAccount970()||!KingSocialRealtime970.isCurrentView(
                    expectedScreen970,activeSocialScreen965,generation970,socialViewGeneration970,true))return;
                if(profile970.exists()){
                    rememberIssuedId10037(profile970);
                    addPerson(uid,nameOf(profile970),clean(profile970.getString("bio")),
                        clean(profile970.getString("photoUrl")),profile970.getLong("level"),profile970.getLong("vipLevel"));}
                else addPerson(uid,"KING "+shortUid(uid),"","",null,null);
            }).addOnFailureListener(error->{
                if(currentAccount970()&&KingSocialRealtime970.isCurrentView(
                    expectedScreen970,activeSocialScreen965,generation970,socialViewGeneration970,true))
                    addPerson(uid,"KING "+shortUid(uid),"","",null,null);
            });
    }

    private void addPerson(String uid, String name, String bio,String photo,Long level,Long vip) {
        LinearLayout card=new LinearLayout(this); card.setGravity(Gravity.CENTER_VERTICAL); card.setPadding(dp(14),dp(10),dp(12),dp(10)); card.setBackground(bg(Color.WHITE,18));
        String initial=(name==null||name.trim().isEmpty())?"K":name.trim().substring(0,1).toUpperCase(Locale.US);
        android.widget.FrameLayout avatarBox=new android.widget.FrameLayout(this);TextView av=label(initial,22,Color.WHITE,true);av.setGravity(Gravity.CENTER);av.setBackground(bg(PURPLE,28));avatarBox.addView(av,new android.widget.FrameLayout.LayoutParams(-1,-1));
        android.widget.ImageView image=new android.widget.ImageView(this);image.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);avatarBox.addView(image,new android.widget.FrameLayout.LayoutParams(-1,-1));if(photo!=null&&!photo.isEmpty())loadSocialPhoto940(image,av,photo);
        card.addView(avatarBox,new LinearLayout.LayoutParams(dp(56),dp(56)));
        LinearLayout info=new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setPadding(dp(12),0,dp(4),0);
        info.addView(label(name,16,DARK,true),new LinearLayout.LayoutParams(-1,dp(23)));
        LinearLayout badges=new LinearLayout(this);badges.setGravity(Gravity.CENTER_VERTICAL);TextView id=label("ID "+displayedId10037(uid),10,PURPLE,true);badges.addView(id,new LinearLayout.LayoutParams(0,dp(18),1));TextView lv=label("VIP "+(vip==null?0:vip)+"  ·  Lv."+(level==null?1:level),10,0xff7b5c00,true);lv.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);badges.addView(lv,new LinearLayout.LayoutParams(dp(105),dp(18)));info.addView(badges,new LinearLayout.LayoutParams(-1,dp(19)));
        TextView b=label(bio.isEmpty()?"KING Plus member":bio,11,MUTED,false); b.setMaxLines(1); info.addView(b,new LinearLayout.LayoutParams(-1,dp(18)));
        card.addView(info,new LinearLayout.LayoutParams(0,dp(60),1));
        TextView arrow=label("›",28,0xff9993a6,false); card.addView(arrow);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(84)); lp.setMargins(0,dp(5),0,dp(5)); list.addView(card,lp); card.setOnClickListener(v->openProfile(uid,name,bio));
    }
    private void loadSocialPhoto940(android.widget.ImageView image,TextView fallback,String url){
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

    private void openProfile(String uid,String name,String bio){
        if(!currentAccount970()||uid==null||uid.equals(me.getUid()))return;
        final String mine=me.getUid();
        db.collection("follows").document(followId(mine,uid))
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnCompleteListener(own->{
              if(!currentAccount970())return;
              final boolean following=own.isSuccessful()
                  &&own.getResult()!=null&&own.getResult().exists();
              db.collection("follows").document(followId(uid,mine))
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnCompleteListener(reverse->{
                    if(!currentAccount970())return;
                    boolean follower=reverse.isSuccessful()
                        &&reverse.getResult()!=null&&reverse.getResult().exists();
                    boolean readsOk=own.isSuccessful()&&reverse.isSuccessful();
                    String relationship=following&&follower?"💜 Mutual friends":
                        following?"You follow this person":
                        follower?"Follows you • Follow Back to become friends":"Not following";
                    if(!readsOk)relationship+="\n⚠ Server follow status unavailable. "+
                        "You can still attempt Follow; the error will be shown.";
                    String[] actions={following?"✓ Unfollow":
                        follower?"💜 Follow Back":"＋ Follow",
                        "✉ Message","↗ Share KING ID","⚑ Report"};
                    new AlertDialog.Builder(this).setTitle(name)
                        .setMessage((bio.isEmpty()?"KING Plus member":bio)+
                            "\n\n"+relationship+"\nKING ID: "+displayedId10037(uid))
                        .setItems(actions,(d,w)->{
                            if(w==0)toggleFollow(uid,name,following);
                            else if(w==1)openChat(uid,name);
                            else if(w==2)shareKingId(uid,name);
                            else report(uid,name);
                        }).setNegativeButton("Close",null).show();
                });
          });
    }

    private void toggleFollow(String uid, String name, boolean following) {
        if(!currentAccount970()||!KingSocialRealtime970.showUid(uid,me.getUid()))return;
        if(!pendingFollows970.add(uid)){toast("Follow update in progress");return;}
        final String myUid970=me.getUid();
        SharedPreferences ownPrefs970=getSharedPreferences("MainActivity",MODE_PRIVATE);
        String followerName970=ownPrefs970.getString("name","");
        if(followerName970==null||followerName970.trim().isEmpty())followerName970=me.getDisplayName();
        if(followerName970==null||followerName970.trim().isEmpty())followerName970="KING "+shortUid(myUid970);
        followerName970=clean(followerName970);
        if(followerName970.length()>60)followerName970=followerName970.substring(0,60);
        final String boundedFollowerName970=followerName970;
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
            values970.put("followerName",boundedFollowerName970);
            values970.put("targetUid",uid);
            values970.put("createdAt",FieldValue.serverTimestamp());
            tx970.set(edge970,values970);
            return true;
        }).addOnSuccessListener(isNowFollowing970->{
            pendingFollows970.remove(uid);
            if(!currentAccount970())return;
            refreshSocialView965();
            if(Boolean.TRUE.equals(isNowFollowing970)){
                db.collection("follows").document(followId(uid,myUid970))
                    .get(com.google.firebase.firestore.Source.SERVER)
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

    private void shareKingId(String uid,String name){
        Intent send=new Intent(Intent.ACTION_SEND); send.setType("text/plain");
        String issued10037=issuedPublicIds10037.get(uid);
        if(issued10037==null){toast("KING ID not verified on server. Open the profile again when online.");return;}
        send.putExtra(Intent.EXTRA_TEXT,"Find "+name+" on KING Plus • KING ID: "+issued10037+"\nVerified Firebase UID: "+uid);
        startActivity(Intent.createChooser(send,"Share KING ID"));
    }

    private void openChat(String uid, String name) { Intent i=new Intent(this,ChatActivity.class); i.putExtra("peerUid",uid); i.putExtra("peerName",name); startActivity(i); }
    private void report(String uid, String name) { final EditText reason=new EditText(this); reason.setHint("Reason"); new AlertDialog.Builder(this).setTitle("Report "+name).setView(reason).setNegativeButton("Cancel",null).setPositiveButton("Submit",(d,w)->{String r=reason.getText().toString().trim();if(r.isEmpty())r="Profile safety report";CloudSync.submitReport(this,uid,r,(ok,m)->runOnUiThread(()->toast(m)));}).show(); }

    private void refreshSocialView965(){
        if(isFinishing()||isDestroyed()||!cloudReady())return;
        switch(activeSocialScreen965){
            case "following":loadFollowing();break;
            case "followers":loadFollowers();break;
            case "friends":loadFriends();break;
            case "search":runSearch();break;
            default:loadDiscover();break;
        }
    }
    @Override protected void onStart(){
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
    @Override protected void onResume(){
        super.onResume();
        if(socialHasResumed965)refreshSocialView965();
        socialHasResumed965=true;
    }

    private void showLocalDemo() { list.removeAllViews(); setLoading("Sign in with Google / Gmail to connect with real KING Plus people"); }
    private String publicId(String uid){return KingSocialIdentity965.publicId(uid);}
    private void rememberIssuedId10037(DocumentSnapshot doc){
        String id=KingPublicId9996.verified(doc.getString("publicId"));
        if(id==null)issuedPublicIds10037.remove(doc.getId());
        else issuedPublicIds10037.put(doc.getId(),id);
    }
    private String displayedId10037(String uid){
        String issued=issuedPublicIds10037.get(uid);
        return issued==null?"Not issued":issued;
    }
    private String followId(String a,String b){return KingSocialIdentity965.followId(a,b);}
    private String nameOf(DocumentSnapshot d){String n=d.getString("displayName");return n==null||n.trim().isEmpty()?"KING "+shortUid(d.getId()):n.trim();}
    private String shortUid(String uid){if(uid==null)return "User";return uid.length()>8?uid.substring(0,8):uid;}
    private String clean(String s){return s==null?"":s.trim();}
    private String safe(String s){return s==null||s.trim().isEmpty()?"Please try again":s.trim();}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
