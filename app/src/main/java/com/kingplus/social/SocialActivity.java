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

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        try { me = FirebaseAuth.getInstance().getCurrentUser(); } catch (Exception ignored) { me = null; }
        try { db = FirebaseFirestore.getInstance(); } catch (Exception ignored) { db = null; }
        render();
        if (cloudReady()) {
            publishOwnProfile();
            loadDiscover();
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
        LinearLayout title = new LinearLayout(this); title.setOrientation(LinearLayout.VERTICAL); title.addView(label("People", 28, DARK, true)); status = label("KING Plus social", 12, MUTED, false); title.addView(status); head.addView(title, new LinearLayout.LayoutParams(0, dp(60), 1));
        TextView inbox = label("✉", 27, PURPLE, true); inbox.setGravity(Gravity.CENTER); inbox.setOnClickListener(v -> startActivity(new Intent(this, InboxActivity.class))); head.addView(inbox, new LinearLayout.LayoutParams(dp(56), dp(56))); root.addView(head);

        LinearLayout find = new LinearLayout(this); find.setPadding(dp(14), dp(4), dp(14), dp(8)); find.setGravity(Gravity.CENTER_VERTICAL);
        search = new EditText(this); search.setSingleLine(true); search.setHint("Search people by name"); search.setTextColor(DARK); search.setHintTextColor(0xff9993a6); search.setPadding(dp(14), 0, dp(14), 0); search.setBackground(bg(Color.WHITE, 18)); find.addView(search, new LinearLayout.LayoutParams(0, dp(52), 1));
        TextView go = label("Search", 14, Color.WHITE, true); go.setGravity(Gravity.CENTER); go.setBackground(bg(PURPLE, 18)); LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(dp(82), dp(52)); gp.setMargins(dp(8),0,0,0); find.addView(go,gp); go.setOnClickListener(v -> runSearch()); root.addView(find);

        LinearLayout quick = new LinearLayout(this); quick.setPadding(dp(12),0,dp(12),dp(8));
        addQuick(quick,"✨\nDiscover",this::loadDiscover); addQuick(quick,"＋\nFollowing",this::loadFollowing); addQuick(quick,"👥\nFollowers",this::loadFollowers); addQuick(quick,"💜\nFriends",this::loadFriends); root.addView(quick,new LinearLayout.LayoutParams(-1,dp(70)));

        ScrollView sv = new ScrollView(this); list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); list.setPadding(dp(12),dp(4),dp(12),dp(24)); sv.addView(list); root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }

    private void addQuick(LinearLayout row, String text, Runnable action) { TextView x=label(text,12,DARK,true); x.setGravity(Gravity.CENTER); x.setBackground(bg(Color.WHITE,16)); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(62),1); p.setMargins(dp(3),0,dp(3),0); row.addView(x,p); x.setOnClickListener(v->action.run()); }
    private void setLoading(String text) { list.removeAllViews(); TextView t=label(text,14,MUTED,false); t.setGravity(Gravity.CENTER); list.addView(t,new LinearLayout.LayoutParams(-1,dp(72))); }

    private void publishOwnProfile() {
        SharedPreferences p = getSharedPreferences("MainActivity", MODE_PRIVATE);
        String name = p.getString("name", "");
        if (name == null || name.trim().isEmpty()) name = me.getDisplayName();
        if (name == null || name.trim().isEmpty()) name = "KING " + shortUid(me.getUid());
        String bio = p.getString("bio", "Love music, games and new friends ✨");
        String tags = p.getString("tags", "Music, Games");
        Map<String,Object> data = new HashMap<>();
        data.put("uid", me.getUid()); data.put("displayName", clean(name)); data.put("searchName", clean(name).toLowerCase(Locale.US)); data.put("bio", clean(bio)); data.put("tags", clean(tags)); data.put("updatedAt", FieldValue.serverTimestamp());
        db.collection("public_profiles").document(me.getUid()).set(data)
            .addOnFailureListener(e -> status.setText("Profile directory unavailable"));
    }

    private void runSearch() {
        if (!cloudReady()) { showLocalDemo(); return; }
        String q = search.getText().toString().trim().toLowerCase(Locale.US);
        if (q.isEmpty()) { loadDiscover(); return; }
        setLoading("Searching…");
        db.collection("public_profiles").orderBy("searchName").startAt(q).endAt(q + "\uf8ff").limit(30).get()
            .addOnSuccessListener(s -> renderProfiles(s.getDocuments(), "No users found"))
            .addOnFailureListener(e -> { setLoading("Search unavailable"); toast(safe(e.getLocalizedMessage())); });
    }

    private void loadDiscover() {
        if (!cloudReady()) { showLocalDemo(); return; }
        setLoading("Loading people…");
        db.collection("public_profiles").limit(30).get()
            .addOnSuccessListener(s -> renderProfiles(s.getDocuments(), "No public profiles yet"))
            .addOnFailureListener(e -> { setLoading("People directory unavailable"); toast(safe(e.getLocalizedMessage())); });
    }

    private void renderProfiles(List<DocumentSnapshot> docs, String empty) {
        list.removeAllViews(); int shown=0;
        for (DocumentSnapshot d : docs) { String uid=d.getString("uid"); if(uid==null||uid.isEmpty()||me!=null&&uid.equals(me.getUid())) continue; addPerson(uid, nameOf(d), clean(d.getString("bio"))); shown++; }
        if (shown==0) setLoading(empty);
    }

    private void loadFollowing() {
        if (!cloudReady()) { showLocalDemo(); return; }
        setLoading("Loading following…");
        db.collection("follows").whereEqualTo("followerUid",me.getUid()).get().addOnSuccessListener(s->{
            list.removeAllViews(); if(s.isEmpty()){setLoading("You are not following anyone yet");return;}
            for(DocumentSnapshot d:s.getDocuments()) loadProfileCard(d.getString("targetUid"));
        }).addOnFailureListener(e->{setLoading("Could not load following");toast(safe(e.getLocalizedMessage()));});
    }

    private void loadFollowers() {
        if (!cloudReady()) { showLocalDemo(); return; }
        setLoading("Loading followers…");
        db.collection("follows").whereEqualTo("targetUid",me.getUid()).get().addOnSuccessListener(s->{
            list.removeAllViews(); if(s.isEmpty()){setLoading("No followers yet");return;}
            for(DocumentSnapshot d:s.getDocuments()) loadProfileCard(d.getString("followerUid"));
        }).addOnFailureListener(e->{setLoading("Could not load followers");toast(safe(e.getLocalizedMessage()));});
    }

    private void loadFriends() {
        if (!cloudReady()) { showLocalDemo(); return; }
        setLoading("Loading friends…");
        db.collection("follows").whereEqualTo("followerUid",me.getUid()).get().addOnSuccessListener(out->{
            Set<String> following=new HashSet<>(); for(DocumentSnapshot d:out.getDocuments()){String u=d.getString("targetUid");if(u!=null)following.add(u);}
            db.collection("follows").whereEqualTo("targetUid",me.getUid()).get().addOnSuccessListener(in->{
                Set<String> friends=new HashSet<>(); for(DocumentSnapshot d:in.getDocuments()){String u=d.getString("followerUid");if(u!=null&&following.contains(u))friends.add(u);}
                list.removeAllViews(); if(friends.isEmpty()){setLoading("Friends appear when you follow each other");return;} for(String uid:friends)loadProfileCard(uid);
            }).addOnFailureListener(e->{setLoading("Could not load friends");toast(safe(e.getLocalizedMessage()));});
        }).addOnFailureListener(e->{setLoading("Could not load friends");toast(safe(e.getLocalizedMessage()));});
    }

    private void loadProfileCard(String uid) {
        if(uid==null||uid.isEmpty())return;
        db.collection("public_profiles").document(uid).get().addOnSuccessListener(d->{ if(d.exists())addPerson(uid,nameOf(d),clean(d.getString("bio"))); else addPerson(uid,"KING "+shortUid(uid),""); });
    }

    private void addPerson(String uid, String name, String bio) {
        LinearLayout card=new LinearLayout(this); card.setGravity(Gravity.CENTER_VERTICAL); card.setPadding(dp(14),dp(10),dp(12),dp(10)); card.setBackground(bg(Color.WHITE,18));
        TextView av=label("●",26,PURPLE,true); av.setGravity(Gravity.CENTER); av.setBackground(bg(0xffefe8ff,25)); card.addView(av,new LinearLayout.LayoutParams(dp(50),dp(50)));
        LinearLayout info=new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setPadding(dp(12),0,dp(4),0); info.addView(label(name,16,DARK,true)); TextView b=label(bio.isEmpty()?"KING Plus member":bio,12,MUTED,false); b.setMaxLines(1); info.addView(b); card.addView(info,new LinearLayout.LayoutParams(0,dp(54),1)); TextView arrow=label("›",28,0xff9993a6,false); card.addView(arrow);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(74)); lp.setMargins(0,dp(5),0,dp(5)); list.addView(card,lp); card.setOnClickListener(v->openProfile(uid,name,bio));
    }

    private void openProfile(String uid, String name, String bio) {
        if (!cloudReady() || uid.equals(me.getUid())) return;
        String ownId=followId(me.getUid(),uid), reverseId=followId(uid,me.getUid());
        db.collection("follows").document(ownId).get().addOnSuccessListener(own->db.collection("follows").document(reverseId).get().addOnSuccessListener(reverse->{
            boolean following=own.exists(), follower=reverse.exists(), friend=following&&follower;
            ArrayList<String> actions=new ArrayList<>(); actions.add(following?"✓ Unfollow":"＋ Follow"); actions.add("✉ Message"); actions.add("⚑ Report");
            String relation=friend?"💜 Friends":(following?"Following":(follower?"Follows you":"Not connected"));
            new AlertDialog.Builder(this).setTitle(name).setMessage((bio.isEmpty()?"KING Plus member":bio)+"\n\n"+relation+"\nUID: "+shortUid(uid))
                .setItems(actions.toArray(new String[0]),(d,w)->{ if(w==0)toggleFollow(uid,name,following); else if(w==1)openChat(uid,name); else report(uid,name); }).setNegativeButton("Close",null).show();
        }));
    }

    private void toggleFollow(String uid, String name, boolean following) {
        String id=followId(me.getUid(),uid);
        if(following){ db.collection("follows").document(id).delete().addOnSuccessListener(v->toast("Unfollowed "+name)).addOnFailureListener(e->toast(safe(e.getLocalizedMessage()))); return; }
        Map<String,Object> data=new HashMap<>(); data.put("followerUid",me.getUid()); data.put("targetUid",uid); data.put("createdAt",FieldValue.serverTimestamp());
        db.collection("follows").document(id).set(data).addOnSuccessListener(v->toast("Following "+name)).addOnFailureListener(e->toast(safe(e.getLocalizedMessage())));
    }

    private void openChat(String uid, String name) { Intent i=new Intent(this,ChatActivity.class); i.putExtra("peerUid",uid); i.putExtra("peerName",name); startActivity(i); }
    private void report(String uid, String name) { final EditText reason=new EditText(this); reason.setHint("Reason"); new AlertDialog.Builder(this).setTitle("Report "+name).setView(reason).setNegativeButton("Cancel",null).setPositiveButton("Submit",(d,w)->{String r=reason.getText().toString().trim();if(r.isEmpty())r="Profile safety report";CloudSync.submitReport(this,uid,r,(ok,m)->runOnUiThread(()->toast(m)));}).show(); }

    private void showLocalDemo() { list.removeAllViews(); addPerson("","Lily ✨","Music • Voice rooms"); addPerson("","Alex 🎮","Games • Friends"); addPerson("","Mia 💜","Community member"); }
    private String followId(String a,String b){return a+"__"+b;}
    private String nameOf(DocumentSnapshot d){String n=d.getString("displayName");return n==null||n.trim().isEmpty()?"KING "+shortUid(d.getId()):n.trim();}
    private String shortUid(String uid){if(uid==null)return "User";return uid.length()>8?uid.substring(0,8):uid;}
    private String clean(String s){return s==null?"":s.trim();}
    private String safe(String s){return s==null||s.trim().isEmpty()?"Please try again":s.trim();}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
